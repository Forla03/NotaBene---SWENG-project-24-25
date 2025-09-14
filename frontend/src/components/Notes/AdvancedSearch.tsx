import React, { useState, useEffect } from 'react';
import { SearchNotesRequest, TagDTO, searchTags } from '../../services/api';
import './AdvancedSearch.css';

interface AdvancedSearchProps {
  onSearch: (searchRequest: SearchNotesRequest) => void;
  onClose: () => void;
  currentFolderId?: number | null;
  isVisible: boolean;
}

const AdvancedSearch: React.FC<AdvancedSearchProps> = ({
  onSearch,
  onClose,
  currentFolderId,
  isVisible
}) => {
  const [searchRequest, setSearchRequest] = useState<SearchNotesRequest>({
    query: '',
    tags: [],
    author: '',
    createdAfter: '',
    createdBefore: '',
    updatedAfter: '',
    updatedBefore: '',
    folderId: currentFolderId || undefined
  });

  const [availableTags, setAvailableTags] = useState<TagDTO[]>([]);
  const [tagInput, setTagInput] = useState('');
  const [showTagSuggestions, setShowTagSuggestions] = useState(false);

  useEffect(() => {
    if (isVisible) {
      loadTags();
    }
  }, [isVisible]);

  useEffect(() => {
    setSearchRequest(prev => ({
      ...prev,
      folderId: currentFolderId || undefined
    }));
  }, [currentFolderId]);

  const loadTags = async () => {
    try {
      const tags = await searchTags();
      setAvailableTags(tags);
    } catch (error) {
      // Error loading tags
    }
  };

  const handleInputChange = (field: keyof SearchNotesRequest, value: string) => {
    setSearchRequest(prev => ({
      ...prev,
      [field]: value || undefined
    }));
  };

  const handleTagSelect = (tagName: string) => {
    if (!searchRequest.tags?.includes(tagName)) {
      setSearchRequest(prev => ({
        ...prev,
        tags: [...(prev.tags || []), tagName]
      }));
    }
    setTagInput('');
    setShowTagSuggestions(false);
  };

  const handleTagRemove = (tagName: string) => {
    setSearchRequest(prev => ({
      ...prev,
      tags: prev.tags?.filter(tag => tag !== tagName) || []
    }));
  };

  const handleTagInputChange = (value: string) => {
    setTagInput(value);
    setShowTagSuggestions(value.length > 0);
  };

  const handleSearch = () => {
    // Pulisci i campi vuoti
    const cleanedRequest: SearchNotesRequest = {};
    
    if (searchRequest.query?.trim()) {
      cleanedRequest.query = searchRequest.query.trim();
    }
    if (searchRequest.tags?.length) {
      cleanedRequest.tags = searchRequest.tags;
    }
    if (searchRequest.author?.trim()) {
      cleanedRequest.author = searchRequest.author.trim();
    }
    if (searchRequest.createdAfter) {
      // Converte la data in LocalDateTime con ora 00:00:00
      cleanedRequest.createdAfter = searchRequest.createdAfter + 'T00:00:00';
    }
    if (searchRequest.createdBefore) {
      // Converte la data in LocalDateTime con ora 23:59:59
      cleanedRequest.createdBefore = searchRequest.createdBefore + 'T23:59:59';
    }
    if (searchRequest.updatedAfter) {
      cleanedRequest.updatedAfter = searchRequest.updatedAfter + 'T00:00:00';
    }
    if (searchRequest.updatedBefore) {
      cleanedRequest.updatedBefore = searchRequest.updatedBefore + 'T23:59:59';
    }
    if (currentFolderId) {
      cleanedRequest.folderId = currentFolderId;
    }

    onSearch(cleanedRequest);
  };

  const handleReset = () => {
    setSearchRequest({
      query: '',
      tags: [],
      author: '',
      createdAfter: '',
      createdBefore: '',
      updatedAfter: '',
      updatedBefore: '',
      folderId: currentFolderId || undefined
    });
    setTagInput('');
  };

  const filteredTags = availableTags.filter(tag =>
    tag.name.toLowerCase().includes(tagInput.toLowerCase()) &&
    !searchRequest.tags?.includes(tag.name)
  );

  if (!isVisible) return null;

  return (
    <div className="advanced-search-overlay">
      <div className="advanced-search-modal">
        <div className="advanced-search-header">
          <h3>
            Ricerca Avanzata
            {currentFolderId && <span className="folder-search-indicator"> (nella cartella)</span>}
          </h3>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>

        <div className="advanced-search-content">
          <div className="search-field">
            <label htmlFor="query">Testo da cercare (titolo o contenuto):</label>
            <input
              id="query"
              type="text"
              value={searchRequest.query || ''}
              onChange={(e) => handleInputChange('query', e.target.value)}
              placeholder="Inserisci parole chiave..."
            />
          </div>

          <div className="search-field">
            <label htmlFor="author">Autore (username):</label>
            <input
              id="author"
              type="text"
              value={searchRequest.author || ''}
              onChange={(e) => handleInputChange('author', e.target.value)}
              placeholder="Nome utente dell'autore..."
            />
          </div>

          <div className="search-field">
            <label htmlFor="tags">Tag:</label>
            <div className="tag-input-container">
              <input
                id="tags"
                type="text"
                value={tagInput}
                onChange={(e) => handleTagInputChange(e.target.value)}
                placeholder="Cerca e seleziona tag..."
                onFocus={() => setShowTagSuggestions(tagInput.length > 0)}
              />
              {showTagSuggestions && filteredTags.length > 0 && (
                <div className="tag-suggestions">
                  {filteredTags.slice(0, 5).map(tag => (
                    <div
                      key={tag.id}
                      className="tag-suggestion"
                      onClick={() => handleTagSelect(tag.name)}
                    >
                      {tag.name}
                    </div>
                  ))}
                </div>
              )}
            </div>
            {searchRequest.tags && searchRequest.tags.length > 0 && (
              <div className="selected-tags">
                {searchRequest.tags.map(tag => (
                  <span key={tag} className="selected-tag">
                    {tag}
                    <button
                      type="button"
                      onClick={() => handleTagRemove(tag)}
                      className="remove-tag-btn"
                    >
                      ×
                    </button>
                  </span>
                ))}
              </div>
            )}
          </div>

          <div className="date-fields">
            <div className="date-field-group">
              <h4>Data di Creazione</h4>
              <div className="date-inputs">
                <div className="date-field">
                  <label htmlFor="createdAfter">Da:</label>
                  <input
                    id="createdAfter"
                    type="date"
                    value={searchRequest.createdAfter || ''}
                    onChange={(e) => handleInputChange('createdAfter', e.target.value)}
                  />
                </div>
                <div className="date-field">
                  <label htmlFor="createdBefore">A:</label>
                  <input
                    id="createdBefore"
                    type="date"
                    value={searchRequest.createdBefore || ''}
                    onChange={(e) => handleInputChange('createdBefore', e.target.value)}
                  />
                </div>
              </div>
            </div>

            <div className="date-field-group">
              <h4>Data di Modifica</h4>
              <div className="date-inputs">
                <div className="date-field">
                  <label htmlFor="updatedAfter">Da:</label>
                  <input
                    id="updatedAfter"
                    type="date"
                    value={searchRequest.updatedAfter || ''}
                    onChange={(e) => handleInputChange('updatedAfter', e.target.value)}
                  />
                </div>
                <div className="date-field">
                  <label htmlFor="updatedBefore">A:</label>
                  <input
                    id="updatedBefore"
                    type="date"
                    value={searchRequest.updatedBefore || ''}
                    onChange={(e) => handleInputChange('updatedBefore', e.target.value)}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="advanced-search-actions">
          <button className="reset-btn" onClick={handleReset}>
            Cancella Filtri
          </button>
          <button className="search-btn" onClick={handleSearch}>
            🔍 Cerca
          </button>
        </div>
      </div>
    </div>
  );
};

export default AdvancedSearch;
