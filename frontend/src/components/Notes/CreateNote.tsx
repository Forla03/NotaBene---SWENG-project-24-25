import React, { useState } from 'react';
import { Note, notesApi, CreateNoteRequest } from '../../services/api';
import './CreateNote.css';

interface CreateNoteProps {
  onNoteCreated: (note: Note) => void;
  onCancel: () => void;
}

const CreateNote: React.FC<CreateNoteProps> = ({ onNoteCreated, onCancel }) => {
  const [newNote, setNewNote] = useState<CreateNoteRequest>({ 
    title: '', 
    content: ''
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    
    if (!newNote.title.trim() || !newNote.content.trim()) {
      setError('Titolo e contenuto sono obbligatori');
      return;
    }
    if (newNote.content.length > 280) {
      setError('Il contenuto non può superare i 280 caratteri');
      return;
    }
    if (newNote.title.length > 255) {
      setError('Il titolo non può superare i 255 caratteri');
      return;
    }

    setIsSubmitting(true);
    try {
      // Chiama il backend per creare la nota
      const response = await notesApi.createNote(newNote);
      onNoteCreated(response.data);
      setNewNote({ title: '', content: '' });
    } catch (err: any) {
      console.error('Error creating note:', err);
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.data?.errors) {
        setError(err.response.data.errors.join(', '));
      } else {
        setError('Errore nella creazione della nota. Verifica che il backend sia avviato.');
      }
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
        {error && (
          <div className="error-message" style={{
            backgroundColor: '#fee',
            color: '#c33',
            padding: '10px',
            borderRadius: '4px',
            marginBottom: '20px',
            border: '1px solid #fcc'
          }}>
            {error}
          </div>
        )}
        
        <div className="form-group">
          <label htmlFor="title">Titolo della nota</label>
          <input
            id="title"
            type="text"
            placeholder="Scrivi il titolo della tua nota..."
            value={newNote.title}
            onChange={(e) => setNewNote({ ...newNote, title: e.target.value })}
            maxLength={255}
            className="title-input"
            disabled={isSubmitting}
          />
          <div className="char-count">{newNote.title.length}/255</div>
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
            disabled={isSubmitting || !newNote.title.trim() || !newNote.content.trim()}
          >
            {isSubmitting ? 'Creazione...' : 'Crea Nota'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CreateNote;