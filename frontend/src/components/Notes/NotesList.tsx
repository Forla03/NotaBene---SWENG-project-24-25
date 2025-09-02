import { useState } from 'react';
import { Note, notesApi, SearchNotesRequest, foldersApi } from '../../services/api';
import NotePermissions from './NotePermissions';
import NoteVersions from './NoteVersions';
import SearchBar from './SearchBar';
import AdvancedSearch from './AdvancedSearch';
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
  const [selectedNoteForVersions, setSelectedNoteForVersions] = useState<Note | null>(null);
  const [showAdvancedSearch, setShowAdvancedSearch] = useState(false);
  const [searchResults, setSearchResults] = useState<Note[] | null>(null);
  const [isSearchActive, setIsSearchActive] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);

  const handleDelete = async (id: number) => {
    if (window.confirm('Sei sicuro di voler eliminare questa nota?')) {
      try { onDeleteNote(id); }
      catch (err) { console.error('Error deleting note:', err); alert('Errore nell\'eliminazione della nota'); }
    }
  };

  const handleLeaveSharedNote = async (id: number) => {
    if (window.confirm('Sei sicuro di voler rimuoverti da questa nota condivisa? Non la vedrai più nella tua lista.')) {
      try {
        await notesApi.leaveSharedNote(id);
        onNotesUpdated(); // Ricarica la lista delle note
      } catch (err) {
        console.error('Error leaving shared note:', err);
        alert('Errore nel lasciare la nota condivisa');
      }
    }
  };

  const handleManagePermissions = (note: Note) => setSelectedNoteForPermissions(note);

  const handleViewVersions = (note: Note) => setSelectedNoteForVersions(note);
  
  const handleVersionRestored = (restoredNote: Note) => {
    console.log('📥 handleVersionRestored chiamato con:', restoredNote);
    
    // Aggiorna la lista delle note ricaricando i dati dal server
    console.log('🔄 Chiamando onNotesUpdated per ricaricare la lista...');
    onNotesUpdated();
    
    // La modale si chiude automaticamente, non serve gestirla qui
    // setSelectedNoteForVersions(null);
    
    console.log('✅ Nota ripristinata con successo:', restoredNote.title);
  };
  const handleClosePermissions = () => setSelectedNoteForPermissions(null);
  const handlePermissionsUpdated = () => onNotesUpdated();

  // --- Gestione ricerca ---
  const handleSimpleSearch = async (query: string) => {
    try {
      setSearchError(null);
      
      let response;
      if (selectedFolderId) {
        // Se siamo in una cartella, usa la ricerca nella cartella con solo il query
        const folderSearchRequest: SearchNotesRequest = { query: query };
        response = await foldersApi.searchNotesInFolder(selectedFolderId, folderSearchRequest);
      } else {
        // Altrimenti usa la ricerca globale semplice
        response = await notesApi.searchNotes(query);
      }
      
      setSearchResults(response.data);
      setIsSearchActive(true);
    } catch (error) {
      console.error('Error during search:', error);
      setSearchError('Errore durante la ricerca. Riprova più tardi.');
    }
  };

  const handleAdvancedSearch = async (searchRequest: SearchNotesRequest) => {
    try {
      setSearchError(null);
      
      let response;
      if (selectedFolderId) {
        // Ricerca nella cartella specifica
        response = await foldersApi.searchNotesInFolder(selectedFolderId, searchRequest);
      } else {
        // Ricerca globale
        response = await notesApi.advancedSearch(searchRequest);
      }
      
      setSearchResults(response.data);
      setIsSearchActive(true);
      setShowAdvancedSearch(false);
    } catch (error) {
      console.error('Error during advanced search:', error);
      setSearchError('Errore durante la ricerca avanzata. Riprova più tardi.');
    }
  };

  const handleClearSearch = () => {
    setSearchResults(null);
    setIsSearchActive(false);
    setSearchError(null);
  };

  const openAdvancedSearch = () => setShowAdvancedSearch(true);
  const closeAdvancedSearch = () => setShowAdvancedSearch(false);

  const ownedNotes = notes.filter(note => note.isOwner);
  const sharedNotes = notes.filter(note => !note.isOwner);

  // Note da mostrare: se c'è una ricerca attiva, mostra i risultati, altrimenti le note normali
  const displayNotes = searchResults || notes;
  const displayOwnedNotes = searchResults ? displayNotes.filter(note => note.isOwner) : ownedNotes;
  const displaySharedNotes = searchResults ? displayNotes.filter(note => !note.isOwner) : sharedNotes;

  const renderActions = (note: Note, isOwnedBlock: boolean) => (
    <div className="note-actions">
      {note.canEdit && (
        <button onClick={() => onEditNote(note)} className="edit-btn" title="Modifica nota">
          ✏️ Modifica
        </button>
      )}

      {/* Visualizza versioni */}
      <button onClick={() => handleViewVersions(note)} className="versions-btn" title="Visualizza versioni">
        📝 Versioni
      </button>

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

      {/* Elimina nota (solo se è il proprietario) */}
      {note.canDelete && note.isOwner && (
        <button onClick={() => note.id && handleDelete(note.id)} className="delete-btn">
          🗑️ Elimina
        </button>
      )}

      {/* Rimuoviti dalla nota condivisa (solo se NON è il proprietario) */}
      {!note.isOwner && note.id && (
        <button 
          onClick={() => handleLeaveSharedNote(note.id!)} 
          className="leave-shared-btn"
          title="Rimuoviti da questa nota condivisa"
        >
          🚪 Esci dalla condivisione
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

      {/* Barra di ricerca */}
      <SearchBar
        onSearch={handleSimpleSearch}
        onAdvancedSearch={openAdvancedSearch}
        onClearSearch={handleClearSearch}
        isSearchActive={isSearchActive}
        placeholder={selectedFolderId ? "Cerca in questa cartella..." : "Cerca nelle note..."}
      />

      {/* Messaggio di errore ricerca */}
      {searchError && (
        <div className="search-error">
          <p style={{ color: 'red', textAlign: 'center', padding: '10px' }}>
            {searchError}
          </p>
        </div>
      )}

      {/* Indicatore risultati ricerca */}
      {isSearchActive && searchResults && (
        <div className="search-results-info">
          <p style={{ color: '#666', fontStyle: 'italic', textAlign: 'center', padding: '10px' }}>
            {searchResults.length === 0 
              ? 'Nessun risultato trovato per la ricerca'
              : `Trovati ${searchResults.length} risultati`
            }
          </p>
        </div>
      )}

      {/* Owned Notes Section */}
      <div className="notes-list">
        <h2>
          {selectedFolderId 
            ? 'Note' 
            : isSearchActive 
              ? `Risultati Ricerca - Le tue Note (${displayOwnedNotes.length})`
              : `Le tue Note (${displayOwnedNotes.length})`
          }
        </h2>
        {(selectedFolderId ? (searchResults || notes) : displayOwnedNotes).length === 0 ? (
          <div className="no-notes">
            <p>{isSearchActive ? 'Nessun risultato trovato' : 'Nessuna nota disponibile'}</p>
            {!selectedFolderId && !isSearchActive && <p>Clicca su "Crea Nuova Nota" per iniziare!</p>}
          </div>
        ) : (
          (selectedFolderId ? (searchResults || notes) : displayOwnedNotes).map((note) => (
            <div key={note.id} className={`note-card ${note.isOwner ? 'owned-note' : 'shared-note'}`}>
              <div className="note-header">
                <div className="note-title-section">
                  <h3 className="note-title">
                    {note.title}
                    {note.currentVersion && (
                      <span className="version-indicator">v{note.currentVersion}</span>
                    )}
                  </h3>
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
      {!selectedFolderId && displaySharedNotes.length > 0 && (
        <div className="notes-list">
          <h2>
            {isSearchActive 
              ? `Risultati Ricerca - Note Condivise con Te (${displaySharedNotes.length})`
              : `Note Condivise con Te (${displaySharedNotes.length})`
            }
          </h2>
          {displaySharedNotes.map((note) => (
            <div key={note.id} className="note-card shared-note">
              <div className="note-header">
                <div className="note-title-section">
                  <h3 className="note-title">
                    {note.title}
                    {note.currentVersion && (
                      <span className="version-indicator">v{note.currentVersion}</span>
                    )}
                  </h3>
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

      {selectedNoteForVersions && (
        <NoteVersions
          noteId={selectedNoteForVersions.id!}
          currentNote={selectedNoteForVersions}
          onVersionRestored={handleVersionRestored}
          onClose={() => setSelectedNoteForVersions(null)}
        />
      )}

      {/* Modale ricerca avanzata */}
      <AdvancedSearch
        isVisible={showAdvancedSearch}
        onSearch={handleAdvancedSearch}
        onClose={closeAdvancedSearch}
        currentFolderId={selectedFolderId}
      />
    </div>
  );
};

export default NotesList;
