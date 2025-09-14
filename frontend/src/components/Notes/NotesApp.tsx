import { useState, useEffect } from 'react';
import NotesList from './NotesList';
import CreateNote from './CreateNote';
import EditNote from './EditNote';
import { Note, notesApi, foldersApi } from '../../services/api';
import { useModal } from '../../hooks/useModal';
import FolderSidebar from '../Folders/FolderSidebar';
import AddToFolderModal from '../Folders/AddToFolderModal';
import { useModalContext } from '../Modal/ModalProvider';

type View = 'list' | 'create' | 'edit';

const NotesApp = () => {
  const modal = useModalContext(); // Aggiunto modal context
  const { showError, showSuccess } = useModal(); // Aggiunto useModal hook
  const [currentView, setCurrentView] = useState<View>('list');
  const [notes, setNotes] = useState<Note[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingNote, setEditingNote] = useState<Note | null>(null);

  // --- Cartelle ---
  const [selectedFolderId, setSelectedFolderId] = useState<number | null>(null);
  const [folderNotes, setFolderNotes] = useState<Note[] | null>(null); // notes of selected folder
  const [addToFolderNote, setAddToFolderNote] = useState<Note | null>(null);

  useEffect(() => { loadNotes(); }, []);

  // reload folder notes when folder selected
  useEffect(() => {
    const loadFolder = async () => {
      if (selectedFolderId == null) {
        setFolderNotes(null);
        return;
      }
      try {
        const res = await foldersApi.getFolder(selectedFolderId);
        // we only have (id,title) in response; request full details from /notes for UI consistency
        const ids = res.data.notes.map(n => n.id);
        const full = notes.filter(n => n.id && ids.includes(n.id));
        // fallback: if some notes are not in cache, reload all
        if (full.length !== ids.length) {
          await loadNotes(); // update cache
          const full2 = notes.filter(n => n.id && ids.includes(n.id!));
          setFolderNotes(full2);
        } else {
          setFolderNotes(full);
        }
      } catch (e) {
        // Error loading folder
      }
    };
    loadFolder();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedFolderId]);

  const loadNotes = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await notesApi.getAllNotes();
      setNotes(response.data);
    } catch (err: any) {
      setError('Errore nel caricamento delle note. Verifica che il backend sia avviato.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateNote = () => setCurrentView('create');
  const handleEditNote = (note: Note) => { setEditingNote(note); setCurrentView('edit'); };

  const handleNoteCreated = (note: Note) => {
    setNotes(prev => [note, ...prev]);
    setCurrentView('list');
  };

  const handleNoteUpdated = (updatedNote: Note) => {
    setNotes(prev => prev.map(n => n.id === updatedNote.id ? updatedNote : n));
    setCurrentView('list'); setEditingNote(null);
  };

  const handleBackToList = () => { setCurrentView('list'); setEditingNote(null); };

  const handleDeleteNote = async (id: number) => {
    try {
      await notesApi.deleteNote(id);
      setNotes(prev => prev.filter(n => n.id !== id));
      if (selectedFolderId && folderNotes) {
        setFolderNotes(folderNotes.filter(n => n.id !== id));
      }
    } catch (err) {
      showError('Errore', 'Errore nella cancellazione della nota');
    }
  };

  const handleCopyNote = async (id: number) => {
    try {
      const response = await notesApi.copyNote(id);
      const copiedNote = response.data;
      
      // Add copied note to list
      setNotes(prev => [copiedNote, ...prev]);
      
      // If in folder view, don't automatically add copy to folder
      // User can do it manually if desired
      
      showSuccess('Successo', 'Nota copiata con successo!');
    } catch (err) {
      showError('Errore', 'Errore nella copia della nota');
    }
  };

  const handleNotesUpdated = async () => { 
    await loadNotes(); 
  };

  const openAddToFolder = (note: Note) => setAddToFolderNote(note);
  const closeAddToFolder = () => setAddToFolderNote(null);

  const handleAddedToFolder = async (folderId: number) => {
    if (selectedFolderId === folderId) {
      // Reload current folder view
      const res = await foldersApi.getFolder(folderId);
      const ids = res.data.notes.map(n => n.id);
      setFolderNotes(notes.filter(n => n.id && ids.includes(n.id)));
    }
  };

  const handleRemoveFromFolder = async (note: Note) => {
    if (selectedFolderId && note.id) {
      await foldersApi.removeNoteFromFolder(selectedFolderId, note.id);
      setFolderNotes(prev => (prev ? prev.filter(n => n.id !== note.id) : prev));
    }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: '50px' }}><p>Caricamento note...</p></div>;
  if (error) return (
    <div style={{ textAlign: 'center', padding: '50px' }}>
      <p style={{ color: 'red' }}>{error}</p>
      <button onClick={loadNotes} style={{ marginTop: '10px' }}>Riprova</button>
    </div>
  );

  if (currentView === 'create') {
    return (
      <div style={{ display:'flex' }}>
        <FolderSidebar
          selectedFolderId={selectedFolderId}
          onSelectFolder={setSelectedFolderId}
        />
        <div style={{ flex:1 }}>
          <CreateNote onNoteCreated={handleNoteCreated} onCancel={handleBackToList} />
        </div>
      </div>
    );
  }

  if (currentView === 'edit' && editingNote) {
    return (
      <div style={{ display:'flex' }}>
        <FolderSidebar
          selectedFolderId={selectedFolderId}
          onSelectFolder={setSelectedFolderId}
        />
        <div style={{ flex:1 }}>
          <EditNote note={editingNote} onNoteUpdated={handleNoteUpdated} onCancel={handleBackToList} />
        </div>
      </div>
    );
  }

  const visibleNotes = selectedFolderId ? (folderNotes ?? []) : notes;

  return (
    <div style={{ display:'flex' }}>
      <FolderSidebar
        selectedFolderId={selectedFolderId}
        onSelectFolder={setSelectedFolderId}
      />
      <div style={{ flex:1 }}>
        <NotesList 
          onCreateNote={handleCreateNote}
          onEditNote={handleEditNote}
          notes={visibleNotes}
          onDeleteNote={handleDeleteNote}
          onCopyNote={handleCopyNote}
          onNotesUpdated={handleNotesUpdated}
          // --- nuove props:
          onAddToFolder={openAddToFolder}
          onRemoveFromFolder={selectedFolderId ? handleRemoveFromFolder : undefined}
          selectedFolderId={selectedFolderId}
        />
      </div>

      {addToFolderNote?.id && (
        <AddToFolderModal
          noteId={addToFolderNote.id}
          onClose={closeAddToFolder}
          onAdded={handleAddedToFolder}
        />
      )}
    </div>
  );
};

export default NotesApp;
