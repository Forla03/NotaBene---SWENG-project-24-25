import { useEffect, useState } from 'react';
import { Folder, foldersApi } from '../../services/api';
import './AddToFolderModal.css';

type Props = {
  noteId: number;
  onClose: () => void;
  onAdded: (folderId: number) => void;
};

const AddToFolderModal = ({ noteId, onClose, onAdded }: Props) => {
  const [folders, setFolders] = useState<Folder[]>([]);
  const [selected, setSelected] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await foldersApi.getFolders();
      setFolders(res.data || []);
      setSelected((res.data && res.data.length > 0) ? res.data[0].id : null);
    } catch (e: any) {
      setError(e?.response?.data?.message || 'Errore nel caricamento cartelle');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleConfirm = async () => {
    if (!selected) return;
    try {
      setSaving(true);
      setError(null);
      await foldersApi.addNoteToFolder(selected, noteId);
      onAdded(selected);
      onClose();
    } catch (e: any) {
      setError(e?.response?.data?.message || 'Errore durante l’aggiunta');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="afm-backdrop">
      <div className="afm-modal">
        <div className="afm-header">
          <h3>Aggiungi nota a cartella</h3>
          <button className="afm-close" onClick={onClose} aria-label="Chiudi">×</button>
        </div>

        <div className="afm-body">
          {loading ? (
            <div className="afm-loading">Caricamento…</div>
          ) : folders.length === 0 ? (
            <div className="afm-empty">
              Non hai ancora cartelle. Creane una dalla sidebar.
            </div>
          ) : (
            <div className="afm-field">
              <label htmlFor="afm-folder">Cartella</label>
              <select
                id="afm-folder"
                value={selected ?? ''}
                onChange={(e) => setSelected(Number(e.target.value))}
              >
                {folders.map((f) => (
                  <option key={f.id} value={f.id}>{f.name}</option>
                ))}
              </select>
            </div>
          )}

          {error && <div className="afm-error">{error}</div>}
        </div>

        <div className="afm-actions">
          <button className="btn-secondary" onClick={onClose}>Annulla</button>
          <button
            className="btn-primary"
            onClick={handleConfirm}
            disabled={!selected || saving || folders.length === 0}
          >
            {saving ? 'Aggiunta…' : 'Aggiungi'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default AddToFolderModal;

