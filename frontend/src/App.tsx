import React, { useState } from 'react';
import HomePage from './components/HomePage';
import NotesApp from './components/Notes/NotesApp';
import './App.css';

function App() {
  const [page, setPage] = useState<'home' | 'notes'>('home');

  return (
    <div className="App">
      {page === 'home' ? (
        <HomePage goToNotes={() => setPage('notes')} />
      ) : (
        <NotesApp />
      )}
    </div>
  );
};

export default App;
