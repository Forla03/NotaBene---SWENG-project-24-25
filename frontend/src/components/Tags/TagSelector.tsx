import { useEffect, useMemo, useRef, useState } from "react";
import { searchTags, TagDTO } from "../../services/api";
import './TagSelector.css';

type Props = {
  value: TagDTO[];                        // tag selezionati
  onChange: (tags: TagDTO[]) => void;     // callback
  placeholder?: string;
};

export default function TagSelector({ value, onChange, placeholder }: Props) {
  const [query, setQuery] = useState("");
  const [options, setOptions] = useState<TagDTO[]>([]);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const timer = useRef<number | null>(null);

  const fetchOptions = async (q: string) => {
    setLoading(true);
    try {
      const res = await searchTags(q);
      setOptions(res);
    } finally {
      setLoading(false);
    }
  };

  // Mostra dropdown e, se vuoto, carica lista completa
  const handleFocus = async () => {
    setOpen(true);
    if (options.length === 0) {
      await fetchOptions(""); // top elenco
    }
  };

  // Pulsante "Mostra tag disponibili": forza elenco completo
  const fetchAll = async () => {
    setQuery("");     // reset ricerca
    setOpen(true);
    await fetchOptions("");
  };

  // Search with debounce when user types
  useEffect(() => {
    if (timer.current) window.clearTimeout(timer.current);
    timer.current = window.setTimeout(() => {
      fetchOptions(query);
      setOpen(true);
    }, 200);
    return () => { if (timer.current) window.clearTimeout(timer.current); };
  }, [query]);

  const selectedIds = useMemo(() => new Set(value.map(t => t.id)), [value]);
  const shown = useMemo(() => options.filter(o => !selectedIds.has(o.id)), [options, selectedIds]);

  const addTag = (t: TagDTO) => {
    onChange([...value, t]);
    setQuery("");
    setOpen(false);
  };

  const removeTag = (id: number) => onChange(value.filter(t => t.id !== id));

  const rootRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
  const onDocClick = (e: MouseEvent) => {
    if (!rootRef.current) return;
    if (!rootRef.current.contains(e.target as Node)) setOpen(false);
  };
  document.addEventListener('mousedown', onDocClick);
  return () => document.removeEventListener('mousedown', onDocClick);
  }, []);

  return (
    <div ref={rootRef} className={`tag-selector ${open ? 'is-open' : ''}`}>
      <div className="tag-chips">
        {value.map(t => (
          <span key={t.id} className="chip">
            {t.name}
            <button
              type="button"
              className="chip-x"
              onClick={() => removeTag(t.id)}
              aria-label={`Rimuovi ${t.name}`}
            >
              ×
            </button>
          </span>
        ))}

        <input
          value={query}
          onChange={(e)=>setQuery(e.target.value)}
          onFocus={handleFocus}
          placeholder={placeholder || "Aggiungi tag…"}
          onKeyDown={(e) => {
          if (e.key === 'Enter') {
            e.preventDefault();
            e.stopPropagation();
          } else if (e.key === 'Escape') {
            setOpen(false);
          }
          }}
        />
      </div>

      {/* Pulsante per aprire subito l’elenco completo */}
      <div className="tag-tools">
        <button
          type="button"
          className="back-button tag-browse-btn"
          onMouseDown={fetchAll}   // onMouseDown evita la perdita di focus
        >
          Mostra tag disponibili
        </button>
      </div>

      {open && (
        <ul className="tag-dropdown">
          {loading && <li className="tag-empty">Caricamento…</li>}
          {!loading && shown.length === 0 && <li className="tag-empty">Nessun tag disponibile</li>}
          {!loading && shown.map(opt => (
            <li key={opt.id} onMouseDown={() => addTag(opt)}>
              {opt.name}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
