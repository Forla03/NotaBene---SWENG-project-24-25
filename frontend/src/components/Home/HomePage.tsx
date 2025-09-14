import React, { useState, useEffect } from 'react';
import { notesApi, Note } from '../../services/api';
import './Homepage.css';

type HomePageProps = {
  goToNotes: () => void;
  onLogout: () => void;
  isLoggedIn: boolean;
  username?: string;
  onLoginClick: () => void;
  onRegisterClick: () => void;
  refreshTimestamp?: number; // Per forzare il refresh delle preview
};

const HomePage = ({ 
  goToNotes, 
  onLogout, 
  isLoggedIn, 
  username, 
  onLoginClick, 
  onRegisterClick,
  refreshTimestamp 
}: HomePageProps) => {
  const [latestNote, setLatestNote] = useState<Note | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const loadLatestNotes = async () => {
      if (!isLoggedIn) {
        setLatestNote(null);
        return;
      }

      setLoading(true);
      try {
        const response = await notesApi.getAllNotes();
        const notes = response.data;
        
        if (notes.length > 0) {
          // Sort by creation date (newest first) - prendi solo l'ultima creata
          const byCreated = [...notes].sort((a, b) => {
            const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
            const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
            return dateB - dateA;
          });
          setLatestNote(byCreated[0]);
        }
      } catch (error) {
        // Error loading notes preview
      } finally {
        setLoading(false);
      }
    };

    loadLatestNotes();
  }, [isLoggedIn, refreshTimestamp]); // Added refreshTimestamp as dependency

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const truncateContent = (content: string, maxLength: number = 150) => {
    if (content.length <= maxLength) return content;
    return content.substring(0, maxLength) + '...';
  };

  return (
    <div className="homepage-container">
      <main className="homepage-content">
        <div className="welcome-section">
          <h1>Benvenuto su NotaBene</h1>
          <p className="subtitle">La tua app personale per la gestione delle note</p>
          
          {isLoggedIn ? (
            <div className="user-welcome">
              <p className="welcome-message">👋 Ciao, <strong>{username}</strong>!</p>
              <button className="cta-button" onClick={goToNotes}>
                📝 Vai alle tue Note
              </button>
            </div>
          ) : (
            <div className="auth-section">
              <p className="auth-message">Accedi per iniziare a creare le tue note</p>
              <div className="auth-buttons-home">
                <button className="login-button-home" onClick={onLoginClick}>
                  Accedi
                </button>
                <button className="register-button-home" onClick={onRegisterClick}>
                  Registrati
                </button>
              </div>
            </div>
          )}
        </div>

        {isLoggedIn && (
          <div className="notes-preview-section">
            <h2>🔍 Ultima Nota</h2>
            
            {loading ? (
              <div className="loading-preview">Caricamento nota...</div>
            ) : (
              <div className="notes-preview-single">
                <div className="preview-card-single">
                  <h3>📅 Ultima Nota Creata</h3>
                  {latestNote ? (
                    <div className="note-preview">
                      <h4 className="note-title">{latestNote.title}</h4>
                      {latestNote.lastModifiedByUsername && (
                        <p className="note-author">di {latestNote.lastModifiedByUsername}</p>
                      )}
                      <p className="note-content">
                        {truncateContent(latestNote.content)}
                      </p>
                      <div className="note-meta">
                        {latestNote.createdAt && (
                          <small>Creata: {formatDate(latestNote.createdAt)}</small>
                        )}
                        {latestNote.currentVersion && (
                          <small className="version-badge">v{latestNote.currentVersion}</small>
                        )}
                      </div>
                      <button 
                        className="view-note-button"
                        onClick={goToNotes}
                      >
                        Visualizza Note
                      </button>
                    </div>
                  ) : (
                    <div className="no-notes">
                      <p>📄 Nessuna nota ancora creata</p>
                      <button className="create-first-note" onClick={goToNotes}>
                        Crea la prima nota
                      </button>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        )}

        <div className="features-section">
          <h2>✨ Funzionalità</h2>
          <div className="features-grid">
            <div className="feature-card">
              <div className="feature-icon">📝</div>
              <h3>Crea Note</h3>
              <p>Scrivi e organizza le tue note in modo semplice e veloce</p>
            </div>
            <div className="feature-card">
              <div className="feature-icon">🔄</div>
              <h3>Versioning</h3>
              <p>Tieni traccia delle modifiche con il sistema di versioning automatico</p>
            </div>
            <div className="feature-card">
              <div className="feature-icon">📋</div>
              <h3>Copia Note</h3>
              <p>Duplica facilmente le tue note esistenti per creare nuovi contenuti</p>
            </div>
            <div className="feature-card">
              <div className="feature-icon">🔍</div>
              <h3>Visualizza</h3>
              <p>Naviga tra le tue note con un'interfaccia intuitiva e moderna</p>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default HomePage;