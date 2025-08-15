import React, { useState, useEffect } from 'react';
import { Note, NotePermissions as Permissions, notesApi, AddPermissionRequest } from '../../services/api';
import './NotePermissions.css';

interface NotePermissionsProps {
  note: Note;
  onClose: () => void;
  onPermissionsUpdated: () => void;
}

const NotePermissions: React.FC<NotePermissionsProps> = ({ note, onClose, onPermissionsUpdated }) => {
  const [permissions, setPermissions] = useState<Permissions | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [addingUser, setAddingUser] = useState('');
  const [permissionType, setPermissionType] = useState<'readers' | 'writers'>('readers');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    loadPermissions();
  }, [note.id]); // eslint-disable-line react-hooks/exhaustive-deps

  const loadPermissions = async () => {
    if (!note.id) return;
    
    try {
      setLoading(true);
      setError(null);
      const response = await notesApi.getNotePermissions(note.id);
      setPermissions(response.data);
    } catch (err: any) {
      console.error('Error loading permissions:', err);
      setError('Errore nel caricamento dei permessi');
    } finally {
      setLoading(false);
    }
  };

  const handleAddPermission = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!note.id || !addingUser.trim()) return;

    const request: AddPermissionRequest = { username: addingUser.trim() };
    
    try {
      setIsSubmitting(true);
      setError(null);
      
      if (permissionType === 'readers') {
        await notesApi.addReaderPermission(note.id, request);
      } else {
        await notesApi.addWriterPermission(note.id, request);
      }
      
      setAddingUser('');
      await loadPermissions();
      onPermissionsUpdated();
    } catch (err: any) {
      console.error('Error adding permission:', err);
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Errore nell\'aggiunta del permesso');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleRemovePermission = async (username: string, type: 'readers' | 'writers') => {
    if (!note.id) return;

    try {
      setError(null);
      
      if (type === 'readers') {
        await notesApi.removeReaderPermission(note.id, username);
      } else {
        await notesApi.removeWriterPermission(note.id, username);
      }
      
      await loadPermissions();
      onPermissionsUpdated();
    } catch (err: any) {
      console.error('Error removing permission:', err);
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Errore nella rimozione del permesso');
      }
    }
  };

  if (loading) {
    return (
      <div className="permissions-modal">
        <div className="permissions-content">
          <div className="loading">Caricamento permessi...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="permissions-modal">
      <div className="permissions-content">
        <div className="permissions-header">
          <h2>Gestisci Permessi</h2>
          <button onClick={onClose} className="close-btn">×</button>
        </div>

        <div className="note-info">
          <h3>{note.title}</h3>
          <p className="creator-info">
            Creatore: <strong>{permissions?.creator}</strong>
          </p>
        </div>

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        <div className="permissions-section">
          <h4>Lettori ({permissions?.readers?.length || 0})</h4>
          <div className="users-list">
            {permissions?.readers?.length === 0 ? (
              <p className="no-users">Nessun lettore aggiunto</p>
            ) : (
              permissions?.readers?.map((username) => (
                <div key={`reader-${username}`} className="user-item">
                  <span>{username}</span>
                  <button
                    onClick={() => handleRemovePermission(username, 'readers')}
                    className="remove-btn"
                    title="Rimuovi permesso di lettura"
                  >
                    Rimuovi
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="permissions-section">
          <h4>Scrittori ({permissions?.writers?.length || 0})</h4>
          <div className="users-list">
            {permissions?.writers?.length === 0 ? (
              <p className="no-users">Nessuno scrittore aggiunto</p>
            ) : (
              permissions?.writers?.map((username) => (
                <div key={`writer-${username}`} className="user-item">
                  <span>{username}</span>
                  <button
                    onClick={() => handleRemovePermission(username, 'writers')}
                    className="remove-btn"
                    title="Rimuovi permesso di scrittura"
                  >
                    Rimuovi
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="add-permission-section">
          <h4>Aggiungi Permesso</h4>
          <form onSubmit={handleAddPermission} className="add-permission-form">
            <div className="form-row">
              <input
                type="text"
                value={addingUser}
                onChange={(e) => setAddingUser(e.target.value)}
                placeholder="Nome utente"
                className="username-input"
                disabled={isSubmitting}
              />
              <select
                value={permissionType}
                onChange={(e) => setPermissionType(e.target.value as 'readers' | 'writers')}
                className="permission-select"
                disabled={isSubmitting}
              >
                <option value="readers">Lettura</option>
                <option value="writers">Scrittura</option>
              </select>
              <button
                type="submit"
                disabled={isSubmitting || !addingUser.trim()}
                className="add-btn"
              >
                {isSubmitting ? 'Aggiunta...' : 'Aggiungi'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default NotePermissions;
