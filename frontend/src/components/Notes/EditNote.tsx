import React, { useState, useEffect, FormEvent } from 'react';
import { Note, notesApi, TagDTO } from '../../services/api';
import TagSelector from '../Tags/TagSelector';
import CreateTagModal from '../Tags/CreateTagModal';
import '../Tags/TagSelector.css';
import '../Tags/CreateTagModal.css';
import './EditNote.css';

interface EditNoteProps {
  note: Note;
  onNoteUpdated: (note: Note) => void;
  onCancel: () => void;
}

const EditNote: React.FC<EditNoteProps> = ({ note, onNoteUpdated, onCancel }) => {
  const [updatedNote, setUpdatedNote] = useState({
    title: note.title,
    content: note.content
  });
  const [tags, setTags] = useState<TagDTO[]>(note.tags ?? []);
  const [openTagModal, setOpenTagModal] = useState(false);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // If note changes in props, realign local state (useful when returning from another page)
  useEffect(() => {
    setUpdatedNote({ title: note.title, content: note.content });
    setTags(note.tags ?? []);
  }, [note]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!updatedNote.title.trim() || !updatedNote.content.trim()) {
      setError('Titolo e contenuto sono obbligatori');
      return;
    }
    if (updatedNote.content.length > 280) {
      setError('Il contenuto non può superare i 280 caratteri');
      return;
    }
    if (updatedNote.title.length > 255) {
      setError('Il titolo non può superare i 255 caratteri');
      return;
    }

    if (!note.id) {
      setError('ID nota non trovato');
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await notesApi.updateNote(note.id, {
        title: updatedNote.title,
        content: updatedNote.content,
        tagIds: tags.map(t => t.id), // Send selected tags
      });
      onNoteUpdated(response.data);
    } catch (err: any) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.data?.errors) {
        setError(err.response.data.errors.join(', '));
      } else {
        setError('Errore nella modifica della nota. Verifica i permessi.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="edit-note-container">
      <div className="edit-note-header">
        <h1>Modifica Nota</h1>
        <button onClick={onCancel} className="back-btn">
          ← Annulla
        </button>
      </div>

      <form
        onSubmit={handleSubmit}
        className="edit-note-form"
        onKeyDownCapture={(e) => {
        if (e.key === 'Enter') {
          const el = e.target as HTMLElement;
          if (el.closest('.tag-selector')) {
            e.preventDefault();
            e.stopPropagation();
          }
        }
        }}
      >
        {error && <div className="error-message">{error}</div>}

        <div className="form-group">
          <label htmlFor="title">Titolo della nota</label>
          <input
            id="title"
            type="text"
            placeholder="Scrivi il titolo della tua nota..."
            value={updatedNote.title}
            onChange={(e) => setUpdatedNote({ ...updatedNote, title: e.target.value })}
            maxLength={255}
            className="title-input"
            disabled={isSubmitting}
          />
          <div className="char-count">{updatedNote.title.length}/255</div>
        </div>

        <div className="form-group">
          <label htmlFor="content">Contenuto</label>
          <textarea
            id="content"
            placeholder="Scrivi il contenuto della tua nota..."
            value={updatedNote.content}
            onChange={(e) => setUpdatedNote({ ...updatedNote, content: e.target.value })}
            maxLength={280}
            className="content-textarea"
            rows={8}
            disabled={isSubmitting}
          />
          <div className="char-count">{updatedNote.content.length}/280</div>
        </div>

        {/* ---- SEZIONE TAG ---- */}
        <div className="tag-tools">
          <label>Tag</label>
          <TagSelector value={tags} onChange={setTags} placeholder="Aggiungi tag…" />
          <div style={{ marginTop: '0.5rem' }}>
            <button
              type="button"
              className="back-button"
              onClick={() => setOpenTagModal(true)}
            >
              + Nuovo tag
            </button>
          </div>
        </div>

        <div className="preview-section">
          <h3>Anteprima</h3>
          <div className="note-preview">
            <div className="preview-header">
              <h4>{updatedNote.title || 'Titolo della nota'}</h4>
            </div>
            <p className="preview-content">
              {updatedNote.content || 'Il contenuto della nota apparirà qui...'}
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
            disabled={isSubmitting || !updatedNote.title.trim() || !updatedNote.content.trim()}
          >
            {isSubmitting ? 'Salvataggio...' : 'Salva Modifiche'}
          </button>
        </div>
      </form>
      <CreateTagModal
          isOpen={openTagModal}
          onClose={() => setOpenTagModal(false)}
          onCreated={(t) =>
            setTags((prev) => (prev.some((p) => p.id === t.id) ? prev : [...prev, t]))
          }
        />
    </div>
  );
};

export default EditNote;

