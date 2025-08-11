import React, { useState, useEffect } from 'react';
import { Note, notesApi } from '../../services/api';
import './NotesList.css';

interface NotesListProps {
  onCreateNote: () => void;
  notes: Note[];
  onDeleteNote: (id: number) => void;
}

const NotesList: React.FC<NotesListProps> = ({ onCreateNote, notes, onDeleteNote }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleDelete = async (id: number) => {
    if (window.confirm('Sei sicuro di voler eliminare questa nota?')) {
      try {
        onDeleteNote(id);
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
      <div className="header-section">
        <h1>NotaBene</h1>
        <p className="subtitle">Le tue note personali</p>
        <button onClick={onCreateNote} className="create-note-btn">
          + Crea Nuova Nota
        </button>
      </div>

      <div className="notes-list">
        <h2>Le tue Note ({notes.length})</h2>
        {notes.length === 0 ? (
          <div className="no-notes">
            <p>Nessuna nota disponibile</p>
            <p>Clicca su "Crea Nuova Nota" per iniziare!</p>
          </div>
        ) : (
          notes.map((note) => (
            <div key={note.id} className="note-card">
              <div className="note-header">
                <div className="note-title-section">
                  <h3 className="note-title">{note.title}</h3>
                </div>
                <div className="note-dates">
                  <span className="note-date">
                    Creata: {note.createdAt ? new Date(note.createdAt).toLocaleString('it-IT') : ''}
                  </span>
                  {note.updatedAt && note.updatedAt !== note.createdAt && (
                    <span className="note-date modified">
                      Modificata: {new Date(note.updatedAt).toLocaleString('it-IT')}
                    </span>
                  )}
                </div>
              </div>
              <p className="note-content">{note.content}</p>
              <div className="note-actions">
                <button 
                  onClick={() => note.id && handleDelete(note.id)}
                  className="delete-btn"
                >
                  Elimina
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default NotesList;
