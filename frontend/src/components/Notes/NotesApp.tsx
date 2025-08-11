import { useState, useEffect } from 'react';
import NotesList from './NotesList';
import CreateNote from './CreateNote';
import { Note, notesApi } from '../../services/api';

type View = 'list' | 'create';

const NotesApp = () => {
  const [currentView, setCurrentView] = useState<View>('list');
  const [notes, setNotes] = useState<Note[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Carica le note all'avvio del componente
  useEffect(() => {
    loadNotes();
  }, []);

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

  const handleCreateNote = () => {
    setCurrentView('create');
  };

  const handleNoteCreated = (note: Note) => {
    setNotes(prevNotes => [note, ...prevNotes]);
    setCurrentView('list');
  };

  const handleBackToList = () => {
    setCurrentView('list');
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

  return (
    <NotesList 
      onCreateNote={handleCreateNote}
      notes={notes}
      onDeleteNote={handleDeleteNote}
    />
  );
};

export default NotesApp;