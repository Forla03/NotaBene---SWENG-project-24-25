import { useState } from "react";
import { createPortal } from 'react-dom';
import { createTag } from "../../services/api";
import "./CreateTagModal.css";

type Props = { isOpen: boolean; onClose: () => void; onCreated: (t: {id:number; name:string}) => void };

function toErrorMessage(err: any, fallback = "Errore creazione tag"): string {
  const data = err?.response?.data;

  if (typeof data === "string") return data;
  if (data && typeof data.message === "string") return data.message;
  if (Array.isArray(data?.errors)) return data.errors.join(", ");
  if (typeof data?.error === "string") return data.error;
  if (err?.message) return err.message;

  // come ultima spiaggia serializza l’oggetto
  try { return JSON.stringify(data ?? err) } catch { return fallback; }
}

export default function CreateTagModal({ isOpen, onClose, onCreated }: Props) {
  const [name, setName] = useState("");
  const [error, setError] = useState<string|null>(null);
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    const trimmed = name.trim();
    if (!trimmed) { setError("Inserisci un nome."); return; }
    setLoading(true);
    try {
      const tag = await createTag(trimmed);
      onCreated(tag);
      setName("");
      onClose();
    } catch (err: any) {
      setError(toErrorMessage(err));   // sempre stringa
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  const modal = (
    <div className="modal-backdrop" onClick={onClose} role="dialog" aria-modal="true">
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3 className="modal-title">Crea nuovo tag</h3>

        <form className="register-form" onSubmit={onSubmit}>
          <div className="form-group">
            <label>Nome tag</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="es. Lavoro, Università…"
              className={error ? "input-error" : ""}
              disabled={loading}
            />
            {error && <div className="error-text">{error}</div>}
          </div>

          <div className="modal-actions">
            <button
              type="button"
              className="back-button"
              onClick={onClose}
              disabled={loading}
            >
              Annulla
            </button>
            <button
              type="submit"
              className="submit-button"
              disabled={loading}
            >
              Crea
            </button>
          </div>
        </form>
      </div>
    </div>
  );

  return createPortal(modal, document.body);

}
