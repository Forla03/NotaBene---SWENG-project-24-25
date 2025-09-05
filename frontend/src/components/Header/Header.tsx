import React from 'react';
import './Header.css';

interface HeaderProps {
  isLoggedIn: boolean;
  username?: string;
  currentPage: 'home' | 'notes' | 'register' | 'login';
  onNavigate: (page: 'home' | 'notes' | 'register' | 'login') => void;
  onLogout: () => void;
}

const Header: React.FC<HeaderProps> = ({ 
  isLoggedIn, 
  username, 
  currentPage, 
  onNavigate, 
  onLogout 
}) => {
  return (
    <header className="app-header">
      <div className="header-content">
        <div className="header-left">
          <button 
            className={`nav-button ${currentPage === 'home' ? 'active' : ''}`}
            onClick={() => onNavigate('home')}
          >
            🏠 Home
          </button>
          {isLoggedIn && (
            <button 
              className={`nav-button ${currentPage === 'notes' ? 'active' : ''}`}
              onClick={() => onNavigate('notes')}
            >
              📝 Le mie Note
            </button>
          )}
        </div>
        
        <div className="header-center">
          <h1 className="app-title">NotaBene</h1>
        </div>
        
        <div className="header-right">
          {isLoggedIn ? (
            <div className="user-section">
              <span className="username">👤 {username}</span>
              <button 
                className="logout-button"
                onClick={onLogout}
              >
                Logout
              </button>
            </div>
          ) : (
            <div className="auth-buttons">
              {currentPage !== 'login' && (
                <button 
                  className="auth-button login-button"
                  onClick={() => onNavigate('login')}
                >
                  Login
                </button>
              )}
              {currentPage !== 'register' && (
                <button 
                  className="auth-button register-button"
                  onClick={() => onNavigate('register')}
                >
                  Registrati
                </button>
              )}
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
