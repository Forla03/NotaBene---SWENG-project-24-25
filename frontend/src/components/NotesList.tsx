import React, { useState, useEffect } from 'react';
import { Note, notesApi } from '../services/api';
import './NotesList.css';

const NotesList: React.FC = () => {
  const [notes, setNotes] = useState<Note[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [newNote, setNewNote] = useState({ content: '', author: '' });

  useEffect(() => {
    fetchNotes();
  }, []);

  const fetchNotes = async () => {
    try {
      setLoading(true);
      const response = await notesApi.getAllNotes();
      setNotes(response.data);
      setError(null);
    } catch (err) {
      setError('Errore nel caricamento delle note');
      console.error('Error fetching notes:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newNote.content.trim() || !newNote.author.trim()) {
      alert('Compilare tutti i campi');
      return;
    }
    if (newNote.content.length > 280) {
      alert('La nota non può superare i 280 caratteri');
      return;
    }

    try {
      await notesApi.createNote(newNote);
      setNewNote({ content: '', author: '' });
      fetchNotes(); // Ricarica le note
    } catch (err) {
      console.error('Error creating note:', err);
      alert('Errore nella creazione della nota');
    }
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Sei sicuro di voler eliminare questa nota?')) {
      try {
        await notesApi.deleteNote(id);
        fetchNotes(); // Ricarica le note
      } catch (err) {
        console.error('Error deleting note:', err);
        alert('Errore nell\'eliminazione della nota');
      }
    }
  };

  if (loading) return <div className="loading">Caricamento...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="notes-container">
      <h1>NotaBene</h1>
      <p className="subtitle">Condividi le tue note con altri utenti</p>

      <form onSubmit={handleSubmit} className="note-form">
        <div className="form-group">
          <input
            type="text"
            placeholder="Il tuo nome"
            value={newNote.author}
            onChange={(e) => setNewNote({ ...newNote, author: e.target.value })}
            className="author-input"
          />
        </div>
        <div className="form-group">
          <textarea
            placeholder="Scrivi la tua nota (max 280 caratteri)..."
            value={newNote.content}
            onChange={(e) => setNewNote({ ...newNote, content: e.target.value })}
            maxLength={280}
            className="content-textarea"
            rows={3}
          />
          <div className="char-count">{newNote.content.length}/280</div>
        </div>
        <button type="submit" className="submit-btn">
          Condividi Nota
        </button>
      </form>

      <div className="notes-list">
        <h2>Note Condivise</h2>
        {notes.length === 0 ? (
          <p className="no-notes">Nessuna nota disponibile</p>
        ) : (
          notes.map((note) => (
            <div key={note.id} className="note-card">
              <div className="note-header">
                <span className="note-author">@{note.author}</span>
                <span className="note-date">
                  {note.createdAt ? new Date(note.createdAt).toLocaleString('it-IT') : ''}
                </span>
              </div>
              <p className="note-content">{note.content}</p>
              <button 
                onClick={() => note.id && handleDelete(note.id)}
                className="delete-btn"
              >
                Elimina
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default NotesList;
