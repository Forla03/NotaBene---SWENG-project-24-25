import React from 'react';
import './Homepage.css'

type HomePageProps = {
  goToNotes: () => void;
  onLogout: () => void;
  isLoggedIn: boolean;
  username?: string;
  onLoginClick: () => void;
  onRegisterClick: () => void;
};

const HomePage: React.FC<HomePageProps> = ({ 
  goToNotes, 
  onLogout, 
  isLoggedIn, 
  username, 
  onLoginClick, 
  onRegisterClick 
}) => {
  return (
    <div className="homepage-container">
      {/* Header con gestione autenticazione */}
      <header className="app-header">
        <div className="logo">NotaBene</div>
        <nav className="auth-nav">
          {isLoggedIn ? (
            <div className="user-section">
              <span className="welcome-message">Benvenuto, {username}</span>
              <button className="notes-button" onClick={goToNotes}>Le tue Note</button>
              <button className="logout-button" onClick={onLogout}>Logout</button>
            </div>
          ) : (
            <div className="auth-buttons">
              <button className="login-button" onClick={onLoginClick}>Login</button>
              <button className="register-button" onClick={onRegisterClick}>Registrati</button>
            </div>
          )}
        </nav>
      </header>

      <main className="homepage-content">
        <h1>Benvenuto su NotaBene</h1>
        <p>App per la gestione delle tue note.</p>
        {isLoggedIn && (
          <button className="create-notes-button" onClick={goToNotes}>Crea Note</button>
        )}
      </main>
    </div>
  );
};

export default HomePage;