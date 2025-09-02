import { useEffect, useMemo, useState } from 'react';
import { Folder, foldersApi } from '../../services/api';
import './FolderSidebar.css';

type Props = {
  selectedFolderId: number | null;
  onSelectFolder: (folderId: number | null) => void;
};

const FolderSidebar = ({ selectedFolderId, onSelectFolder }: Props) => {
  const [folders, setFolders] = useState<Folder[]>([]);
  const [creating, setCreating] = useState(false);
  const [name, setName] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await foldersApi.getFolders();
      setFolders(res.data || []);
    } catch (e: any) {
      setError(e?.response?.data?.message || 'Errore nel caricamento cartelle');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleCreate = async () => {
  if (!name.trim()) return;
  try {
    setCreating(true);
    setError(null);

    await foldersApi.createFolder({ name: name.trim() }); // ← solo name

    setName('');
    await load();
  } catch (e: any) {
    setError(e?.response?.data?.message || 'Errore nella creazione');
  } finally {
    setCreating(false);
  }
};


  return (
  <aside className="folder-sidebar">
    <div className="fs-header">
      <h4 className="fs-title">Cartelle</h4>
    </div>

    <div className="fs-list">
      {loading ? (
        <div className="fs-loading">Caricamento…</div>
      ) : folders.length === 0 ? (
        <div className="fs-empty">Nessuna cartella</div>
      ) : (
        <ul className="fs-list-ul">
          <li>
            <div className="fs-row">
              <button
                type="button"
                className={`fs-item-btn ${selectedFolderId === null ? 'active' : ''}`}
                onClick={() => onSelectFolder(null)}
                onKeyDown={(e) => (e.key === 'Enter' || e.key === ' ') && onSelectFolder(null)}
                aria-current={selectedFolderId === null ? 'page' : undefined}
                title="Mostra tutte le note"
              >
              <span className="fs-item-icon" aria-hidden>🗂️</span>
              <span className="fs-item-label">Tutte le note</span>
              <span className="fs-item-arrow" aria-hidden>›</span>
              </button>
            </div>
          </li>

          {folders.map((f) => (
          <li key={f.id}>
            <div className="fs-row">
              <button
                type="button"
                className={`fs-item-btn ${selectedFolderId === f.id ? 'active' : ''}`}
                onClick={() => onSelectFolder(f.id)}
                onKeyDown={(e) => (e.key === 'Enter' || e.key === ' ') && onSelectFolder(f.id)}
                aria-current={selectedFolderId === f.id ? 'page' : undefined}
                title={`Apri cartella "${f.name}"`}
              >
              <span className="fs-item-icon" aria-hidden>📁</span>
              <span className="fs-item-label">{f.name}</span>
              <span className="fs-item-arrow" aria-hidden>›</span>
            </button>
            <button
            type="button"
            className="fs-del"
            title={`Elimina "${f.name}"`}
            onClick={async (e) => {
              e.stopPropagation();
              const ok = window.confirm(`Eliminare la cartella "${f.name}"?`);
              if (!ok) return;
              try {
                await foldersApi.deleteFolder(f.id);
              if (selectedFolderId === f.id) onSelectFolder(null);
                await load();
              } catch (err: any) {
                const msg = err?.response?.data?.message || 'Errore durante l’eliminazione';
                setError(msg);
              }
            }}
            >
              Elimina
            </button>
            </div>
          </li>
        ))}
    </ul>
      )}
    </div>

    {error && <div className="fs-error">{error}</div>}

    <div className="fs-create">
      <input
        type="text"
        placeholder="Nuova cartella…"
        value={name}
        onChange={(e) => setName(e.target.value)}
        disabled={creating}
      />
      <button
        className="btn-primary"
        onClick={handleCreate}
        disabled={creating || !name.trim()}
      >
        Crea
      </button>
    </div>
  </aside>
);

}
export default FolderSidebar;

