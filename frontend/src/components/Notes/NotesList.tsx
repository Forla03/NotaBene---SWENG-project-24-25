import { useState } from 'react';
import { Note } from '../../services/api';
import NotePermissions from './NotePermissions';
import './NotesList.css';

interface NotesListProps {
  onCreateNote: () => void;
  onEditNote: (note: Note) => void;
  notes: Note[];
  onDeleteNote: (id: number) => void;
  onNotesUpdated: () => void;
}

const NotesList = ({ onCreateNote, onEditNote, notes, onDeleteNote, onNotesUpdated }: NotesListProps) => {
  const [selectedNoteForPermissions, setSelectedNoteForPermissions] = useState<Note | null>(null);

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

  const handleManagePermissions = (note: Note) => {
    setSelectedNoteForPermissions(note);
  };

  const handleClosePermissions = () => {
    setSelectedNoteForPermissions(null);
  };

  const handlePermissionsUpdated = () => {
    onNotesUpdated();
  };

  // Separate notes into owned and shared
  const ownedNotes = notes.filter(note => note.isOwner);
  const sharedNotes = notes.filter(note => !note.isOwner);

  return (
    <div className="notes-container">
      <div className="header-section">
        <h1>NotaBene</h1>
        <p className="subtitle">Le tue note personali e condivise</p>
        <button onClick={onCreateNote} className="create-note-btn">
          + Crea Nuova Nota
        </button>
      </div>

      {/* Owned Notes Section */}
      <div className="notes-list">
        <h2>Le tue Note ({ownedNotes.length})</h2>
        {ownedNotes.length === 0 ? (
          <div className="no-notes">
            <p>Nessuna nota disponibile</p>
            <p>Clicca su "Crea Nuova Nota" per iniziare!</p>
          </div>
        ) : (
          ownedNotes.map((note) => (
            <div key={note.id} className="note-card owned-note">
              <div className="note-header">
                <div className="note-title-section">
                  <h3 className="note-title">{note.title}</h3>
                  <span className="note-badge owner-badge">Proprietario</span>
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
                {note.canEdit && (
                  <button 
                    onClick={() => onEditNote(note)}
                    className="edit-btn"
                    title="Modifica nota"
                  >
                    ✏️ Modifica
                  </button>
                )}
                {note.canShare && (
                  <button 
                    onClick={() => handleManagePermissions(note)}
                    className="permissions-btn"
                    title="Gestisci permessi"
                  >
                    👥 Condividi
                  </button>
                )}
                {note.canDelete && (
                  <button 
                    onClick={() => note.id && handleDelete(note.id)}
                    className="delete-btn"
                  >
                    Elimina
                  </button>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      {/* Shared Notes Section */}
      {sharedNotes.length > 0 && (
        <div className="notes-list">
          <h2>Note Condivise con Te ({sharedNotes.length})</h2>
          {sharedNotes.map((note) => (
            <div key={note.id} className="note-card shared-note">
              <div className="note-header">
                <div className="note-title-section">
                  <h3 className="note-title">{note.title}</h3>
                  <span className="note-badge shared-badge">Condivisa</span>
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
                {note.canEdit && (
                  <button 
                    onClick={() => onEditNote(note)}
                    className="edit-btn"
                    title="Modifica nota"
                  >
                    ✏️ Modifica
                  </button>
                )}
                <span className="shared-info">
                  {note.canEdit ? '✏️ Puoi modificare' : '👁️ Solo lettura'}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Permissions Modal */}
      {selectedNoteForPermissions && (
        <NotePermissions
          note={selectedNoteForPermissions}
          onClose={handleClosePermissions}
          onPermissionsUpdated={handlePermissionsUpdated}
        />
      )}
    </div>
  );
};

export default NotesList;
