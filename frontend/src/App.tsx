import React, { useState } from 'react';
import HomePage from './components/HomePage';
import NotesList from './components/NotesList';
import './App.css';

function App() {
  const [page, setPage] = useState<'home' | 'notes'>('home');

  return (
    <div className="App">
      {page === 'home' ? (
        <HomePage goToNotes={() => setPage('notes')} />
      ) : (
        <NotesList />
      )}
    </div>
  );
}

export default App;