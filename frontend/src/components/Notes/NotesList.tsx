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

  // --- NUOVE props per cartelle:
  onAddToFolder?: (note: Note) => void;
  onRemoveFromFolder?: (note: Note) => void; // attiva solo in vista cartella
  selectedFolderId?: number | null;
}

const NotesList = ({
  onCreateNote, onEditNote, notes, onDeleteNote, onNotesUpdated,
  onAddToFolder, onRemoveFromFolder, selectedFolderId
}: NotesListProps) => {
  const [selectedNoteForPermissions, setSelectedNoteForPermissions] = useState<Note | null>(null);

  const handleDelete = async (id: number) => {
    if (window.confirm('Sei sicuro di voler eliminare questa nota?')) {
      try { onDeleteNote(id); }
      catch (err) { console.error('Error deleting note:', err); alert('Errore nell\'eliminazione della nota'); }
    }
  };

  const handleManagePermissions = (note: Note) => setSelectedNoteForPermissions(note);
  const handleClosePermissions = () => setSelectedNoteForPermissions(null);
  const handlePermissionsUpdated = () => onNotesUpdated();

  const ownedNotes = notes.filter(note => note.isOwner);
  const sharedNotes = notes.filter(note => !note.isOwner);

  const renderActions = (note: Note, isOwnedBlock: boolean) => (
    <div className="note-actions">
      {note.canEdit && (
        <button onClick={() => onEditNote(note)} className="edit-btn" title="Modifica nota">
          ✏️ Modifica
        </button>
      )}

      {/* Aggiungi a cartella */}
      {(note.canEdit || note.isOwner) && onAddToFolder && (
        <button onClick={() => onAddToFolder(note)} className="permissions-btn" title="Aggiungi alla cartella">
          📁 Aggiungi a Cartella
        </button>
      )}

      {/* Rimuovi dalla cartella (solo se stai guardando una cartella) */}
      {selectedFolderId && onRemoveFromFolder && (
        <button onClick={() => onRemoveFromFolder(note)} className="delete-btn" title="Rimuovi da questa cartella">
          ➖ Rimuovi da Cartella
        </button>
      )}

      {/* Condivisione (solo se owner) */}
      {isOwnedBlock && note.canShare && (
        <button onClick={() => handleManagePermissions(note)} className="permissions-btn" title="Gestisci permessi">
          👥 Condividi
        </button>
      )}

      {note.canDelete && (
        <button onClick={() => note.id && handleDelete(note.id)} className="delete-btn">
          Elimina
        </button>
      )}
    </div>
  );

  return (
    <div className="notes-container">
      <div className="header-section">
        <h1>NotaBene</h1>
        <p className="subtitle">
          {selectedFolderId ? 'Note nella cartella selezionata' : 'Le tue note personali e condivise'}
        </p>
        {!selectedFolderId && (
          <button onClick={onCreateNote} className="create-note-btn">
            + Crea Nuova Nota
          </button>
        )}
      </div>

      {/* Owned Notes Section */}
      <div className="notes-list">
        <h2>{selectedFolderId ? 'Note' : `Le tue Note (${ownedNotes.length})`}</h2>
        {(selectedFolderId ? notes : ownedNotes).length === 0 ? (
          <div className="no-notes">
            <p>Nessuna nota disponibile</p>
            {!selectedFolderId && <p>Clicca su "Crea Nuova Nota" per iniziare!</p>}
          </div>
        ) : (
          (selectedFolderId ? notes : ownedNotes).map((note) => (
            <div key={note.id} className={`note-card ${note.isOwner ? 'owned-note' : 'shared-note'}`}>
              <div className="note-header">
                <div className="note-title-section">
                  <h3 className="note-title">{note.title}</h3>
                  {note.isOwner ? (
                    <span className="note-badge owner-badge">Proprietario</span>
                  ) : (
                    <span className="note-badge shared-badge">Condivisa</span>
                  )}
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
              {renderActions(note, true)}
              <div className="tag-pill-list">
              {(note.tags?.length ?? 0) === 0 ? (
              <span className="tag-pill tag-pill-empty">Nessun tag</span>
              ) : (
              note.tags!.map(t => (
              <span key={t.id} className="tag-pill">{t.name}</span>
              ))
              )}
            </div>
            </div>
          ))
        )}
      </div>

      {/* Shared Notes Section (fuori dalla vista cartella) */}
      {!selectedFolderId && sharedNotes.length > 0 && (
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
              {renderActions(note, false)}
              {/* Tag (read-only) */}
                <div className="tag-pill-list">
                {(note.tags?.length ?? 0) === 0 ? (
                <span className="tag-pill tag-pill-empty">Nessun tag</span>
                ) : (
                note.tags!.map(t => (
                <span key={t.id} className="tag-pill">{t.name}</span>
                ))
                )}
              </div>
            </div>
          ))}
        </div>
      )}

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
