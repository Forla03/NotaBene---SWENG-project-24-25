import React, { useState, FormEvent } from 'react';
import { Note, notesApi, TagDTO } from '../../services/api';
import TagSelector from '../Tags/TagSelector';
import CreateTagModal from '../Tags/CreateTagModal';
import '../Tags/TagSelector.css';
import '../Tags/CreateTagModal.css';
import './CreateNote.css';

interface CreateNoteProps {
  onNoteCreated: (note: Note) => void;
  onCancel: () => void;
}

const CreateNote: React.FC<CreateNoteProps> = ({ onNoteCreated, onCancel }) => {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [tags, setTags] = useState<TagDTO[]>([]);
  const [openTagModal, setOpenTagModal] = useState(false);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!title.trim() || !content.trim()) {
      setError('Titolo e contenuto sono obbligatori');
      return;
    }
    if (content.length > 280) {
      setError('Il contenuto non può superare i 280 caratteri');
      return;
    }
    if (title.length > 255) {
      setError('Il titolo non può superare i 255 caratteri');
      return;
    }

    setIsSubmitting(true);
    try {
      const res = await notesApi.createNote({
        title,
        content,
        tagIds: tags.map(t => t.id), // Send selected tags
      });
      onNoteCreated(res.data);
    } catch (err: any) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.data?.errors) {
        setError(err.response.data.errors.join(', '));
      } else {
        setError('Errore nella creazione della nota.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="create-note-container">
      <div className="create-note-header">
        <h1>Crea Nuova Nota</h1>
        <button onClick={onCancel} className="back-btn">← Annulla</button>
      </div>

      <form
        onSubmit={handleSubmit}
        className="create-note-form"
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
          <label htmlFor="title">Titolo</label>
          <input
            id="title"
            type="text"
            placeholder="Scrivi il titolo della tua nota..."
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            maxLength={255}
            className="title-input"
            disabled={isSubmitting}
          />
          <div className="char-count">{title.length}/255</div>
        </div>

        <div className="form-group">
          <label htmlFor="content">Contenuto</label>
          <textarea
            id="content"
            placeholder="Scrivi il contenuto della tua nota..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
            maxLength={280}
            className="content-textarea"
            rows={8}
            disabled={isSubmitting}
          />
          <div className="char-count">{content.length}/280</div>
        </div>

        {/* ---- SEZIONE TAG ---- */}
        <div className="form-group">
          <label>Tag</label>
          <TagSelector value={tags} onChange={setTags} placeholder="Aggiungi tag…" />

          <div className="tag-tools" style={{ marginTop: '0.5rem' }}>
            <button
              type="button"
              className="back-button"
              onClick={() => setOpenTagModal(true)}
              disabled={isSubmitting}
            >
              + Nuovo tag
            </button>
          </div>
        </div>
        <div className="form-actions">
          <button type="button" onClick={onCancel} className="cancel-btn">
            Annulla
          </button>
          <button
            type="submit"
            className="submit-btn"
            disabled={isSubmitting || !title.trim() || !content.trim()}
          >
            {isSubmitting ? 'Salvataggio…' : 'Crea Nota'}
          </button>
        </div>
      </form>
      {/* Modal fuori dal form (usa portal) → nessun nesting form-in-form */}
        <CreateTagModal
          isOpen={openTagModal}
          onClose={() => setOpenTagModal(false)}
          onCreated={(t) =>
            setTags(prev => prev.some(p => p.id === t.id) ? prev : [...prev, t])
          }
        />
    </div>
  );
};

export default CreateNote;
