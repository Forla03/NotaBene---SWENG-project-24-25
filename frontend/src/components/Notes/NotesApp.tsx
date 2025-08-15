import { useState, useEffect } from 'react';
import NotesList from './NotesList';
import CreateNote from './CreateNote';
import EditNote from './EditNote';
import { Note, notesApi } from '../../services/api';

type View = 'list' | 'create' | 'edit';

const NotesApp = () => {
  const [currentView, setCurrentView] = useState<View>('list');
  const [notes, setNotes] = useState<Note[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingNote, setEditingNote] = useState<Note | null>(null);

  // Carica le note all'avvio del componente
  useEffect(() => {
    loadNotes();
  }, []);

  const loadNotes = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await notesApi.getAllNotes();
      
      // The backend now returns notes with permission flags already set
      setNotes(response.data);
    } catch (err: any) {
      console.error('Error loading notes:', err);
      setError('Errore nel caricamento delle note. Verifica che il backend sia avviato.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateNote = () => {
    setCurrentView('create');
  };

  const handleEditNote = (note: Note) => {
    setEditingNote(note);
    setCurrentView('edit');
  };

  const handleNoteCreated = (note: Note) => {
    // New notes come with permission flags from backend
    setNotes(prevNotes => [note, ...prevNotes]);
    setCurrentView('list');
  };

  const handleNoteUpdated = (updatedNote: Note) => {
    setNotes(prevNotes => 
      prevNotes.map(note => 
        note.id === updatedNote.id ? updatedNote : note
      )
    );
    setCurrentView('list');
    setEditingNote(null);
  };

  const handleBackToList = () => {
    setCurrentView('list');
    setEditingNote(null);
  };

  const handleDeleteNote = async (id: number) => {
    try {
      await notesApi.deleteNote(id);
      setNotes(prevNotes => prevNotes.filter(note => note.id !== id));
    } catch (err: any) {
      console.error('Error deleting note:', err);
      alert('Errore nella cancellazione della nota');
    }
  };

  const handleNotesUpdated = () => {
    loadNotes(); // Reload notes when permissions are updated
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <p>Caricamento note...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <p style={{ color: 'red' }}>{error}</p>
        <button onClick={loadNotes} style={{ marginTop: '10px' }}>
          Riprova
        </button>
      </div>
    );
  }

  if (currentView === 'create') {
    return (
      <CreateNote 
        onNoteCreated={handleNoteCreated}
        onCancel={handleBackToList}
      />
    );
  }

  if (currentView === 'edit' && editingNote) {
    return (
      <EditNote 
        note={editingNote}
        onNoteUpdated={handleNoteUpdated}
        onCancel={handleBackToList}
      />
    );
  }

  return (
    <NotesList 
      onCreateNote={handleCreateNote}
      onEditNote={handleEditNote}
      notes={notes}
      onDeleteNote={handleDeleteNote}
      onNotesUpdated={handleNotesUpdated}
    />
  );
};

export default NotesApp;