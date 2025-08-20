import { useState, useEffect } from 'react';
import NotesList from './NotesList';
import CreateNote from './CreateNote';
import EditNote from './EditNote';
import { Note, notesApi, Folder, foldersApi } from '../../services/api';
import FolderSidebar from '../Folders/FolderSidebar';
import AddToFolderModal from '../Folders/AddToFolderModal';

type View = 'list' | 'create' | 'edit';

const NotesApp = () => {
  const [currentView, setCurrentView] = useState<View>('list');
  const [notes, setNotes] = useState<Note[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingNote, setEditingNote] = useState<Note | null>(null);

  // --- Cartelle ---
  const [selectedFolderId, setSelectedFolderId] = useState<number | null>(null);
  const [folderNotes, setFolderNotes] = useState<Note[] | null>(null); // note della cartella selezionata
  const [addToFolderNote, setAddToFolderNote] = useState<Note | null>(null);

  useEffect(() => { loadNotes(); }, []);

  // ricarica note cartella quando selezionata
  useEffect(() => {
    const loadFolder = async () => {
      if (selectedFolderId == null) {
        setFolderNotes(null);
        return;
      }
      try {
        const res = await foldersApi.getFolder(selectedFolderId);
        // abbiamo solo (id,title) nella risposta; chiediamo i dettagli completi dal tuo /notes per coerenza UI
        const ids = res.data.notes.map(n => n.id);
        const full = notes.filter(n => n.id && ids.includes(n.id));
        // fallback: se qualche nota non è in cache, ricarico tutte
        if (full.length !== ids.length) {
          await loadNotes(); // aggiorna cache
          const full2 = notes.filter(n => n.id && ids.includes(n.id!));
          setFolderNotes(full2);
        } else {
          setFolderNotes(full);
        }
      } catch (e) {
        console.error(e);
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
      console.error('Error loading notes:', err);
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
    // se sono dentro una cartella, non aggiungo automaticamente: l’utente decide col pulsante
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
      console.error('Error deleting note:', err);
      alert('Errore nella cancellazione della nota');
    }
  };

  const handleNotesUpdated = () => { loadNotes(); };

  // --- Cartelle: aggiungi / rimuovi
  const openAddToFolder = (note: Note) => setAddToFolderNote(note);
  const closeAddToFolder = () => setAddToFolderNote(null);

  const handleAddedToFolder = async (folderId: number) => {
    if (selectedFolderId === folderId) {
      // ricarica vista cartella corrente
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
