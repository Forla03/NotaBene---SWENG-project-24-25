import React, { useState } from 'react';
import { Note } from '../../services/api';
import './CreateNote.css';

interface CreateNoteProps {
  onNoteCreated: (note: Note) => void;
  onCancel: () => void;
}

const CreateNote: React.FC<CreateNoteProps> = ({ onNoteCreated, onCancel }) => {
  const [newNote, setNewNote] = useState({ title: '', content: '', author: '' });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newNote.title.trim() || !newNote.content.trim() || !newNote.author.trim()) {
      alert('Compilare tutti i campi');
      return;
    }
    if (newNote.content.length > 280) {
      alert('Il contenuto non può superare i 280 caratteri');
      return;
    }
    if (newNote.title.length > 100) {
      alert('Il titolo non può superare i 100 caratteri');
      return;
    }

    setIsSubmitting(true);
    try {
      // Per ora simula la creazione
      const tempNote: Note = {
        id: Date.now(),
        title: newNote.title,
        content: newNote.content,
        author: newNote.author,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      
      onNoteCreated(tempNote);
      setNewNote({ title: '', content: '', author: '' });
      
      // Quando avrai il backend, sostituisci con:
      // await notesApi.createNote(newNote);
    } catch (err) {
      console.error('Error creating note:', err);
      alert('Errore nella creazione della nota');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="create-note-container">
      <div className="create-note-header">
        <h1>Crea una Nuova Nota</h1>
        <button onClick={onCancel} className="back-btn">
          ← Torna alle Note
        </button>
      </div>

      <form onSubmit={handleSubmit} className="create-note-form">
        <div className="form-group">
          <label htmlFor="author">Il tuo nome</label>
          <input
            id="author"
            type="text"
            placeholder="Inserisci il tuo nome"
            value={newNote.author}
            onChange={(e) => setNewNote({ ...newNote, author: e.target.value })}
            className="author-input"
            disabled={isSubmitting}
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="title">Titolo della nota</label>
          <input
            id="title"
            type="text"
            placeholder="Scrivi il titolo della tua nota..."
            value={newNote.title}
            onChange={(e) => setNewNote({ ...newNote, title: e.target.value })}
            maxLength={100}
            className="title-input"
            disabled={isSubmitting}
          />
          <div className="char-count">{newNote.title.length}/100</div>
        </div>
        
        <div className="form-group">
          <label htmlFor="content">Contenuto</label>
          <textarea
            id="content"
            placeholder="Scrivi il contenuto della tua nota..."
            value={newNote.content}
            onChange={(e) => setNewNote({ ...newNote, content: e.target.value })}
            maxLength={280}
            className="content-textarea"
            rows={8}
            disabled={isSubmitting}
          />
          <div className="char-count">{newNote.content.length}/280</div>
        </div>

        <div className="preview-section">
          <h3>Anteprima</h3>
          <div className="note-preview">
            <div className="preview-header">
              <h4>{newNote.title || 'Titolo della nota'}</h4>
              <span className="preview-author">@{newNote.author || 'nomeutente'}</span>
            </div>
            <p className="preview-content">
              {newNote.content || 'Il contenuto della nota apparirà qui...'}
            </p>
          </div>
        </div>
        
        <div className="form-actions">
          <button type="button" onClick={onCancel} className="cancel-btn">
            Annulla
          </button>
          <button 
            type="submit" 
            className="submit-btn"
            disabled={isSubmitting || !newNote.title.trim() || !newNote.content.trim() || !newNote.author.trim()}
          >
            {isSubmitting ? 'Creazione...' : 'Crea Nota'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CreateNote;