import React, { useState } from 'react';
import './SearchBar.css';

interface SearchBarProps {
  onSearch: (query: string) => void;
  onAdvancedSearch: () => void;
  onClearSearch: () => void;
  isSearchActive: boolean;
  placeholder?: string;
}

const SearchBar: React.FC<SearchBarProps> = ({
  onSearch,
  onAdvancedSearch,
  onClearSearch,
  isSearchActive,
  placeholder = "Cerca nelle note..."
}) => {
  const [query, setQuery] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (query.trim()) {
      onSearch(query.trim());
    }
  };

  const handleClear = () => {
    setQuery('');
    onClearSearch();
  };

  return (
    <div className="search-bar-container">
      <form onSubmit={handleSubmit} className="search-form">
        <div className="search-input-group">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder={placeholder}
            className="search-input"
          />
          <button
            type="submit"
            className="search-btn"
            disabled={!query.trim()}
            title="Cerca"
          >
            🔍
          </button>
        </div>
      </form>
      
      <div className="search-actions">
        <button
          type="button"
          onClick={onAdvancedSearch}
          className="advanced-search-btn"
          title="Ricerca avanzata"
        >
          🔧 Ricerca Avanzata
        </button>
        
        {isSearchActive && (
          <button
            type="button"
            onClick={handleClear}
            className="clear-search-btn"
            title="Cancella ricerca"
          >
            ✕ Cancella
          </button>
        )}
      </div>
    </div>
  );
};

export default SearchBar;
