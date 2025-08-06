import React, { useState } from 'react';
import NotesList from './NotesList';
import CreateNote from './CreateNote';
import { Note } from '../../services/api';

type View = 'list' | 'create';

const NotesApp: React.FC = () => {
  const [currentView, setCurrentView] = useState<View>('list');
  const [notes, setNotes] = useState<Note[]>([]);

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

  const handleDeleteNote = (id: number) => {
    setNotes(prevNotes => prevNotes.filter(note => note.id !== id));
  };

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