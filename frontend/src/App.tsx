import React, { useState, useEffect } from 'react';
import HomePage from './components/Home/HomePage';
import NotesApp from './components/Notes/NotesApp';
import Register from './components/Register/Register';
import Login from './components/Login/Login';
import Header from './components/Header/Header';
import ModalProvider from './components/Modal/ModalProvider';
import { getAuthToken, clearAuthToken } from './services/api';
import './App.css';

const App = () => {
  const [page, setPage] = useState<'home' | 'notes' | 'register' | 'login'>('home');
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [username, setUsername] = useState<string | undefined>(undefined);
  const [isLoading, setIsLoading] = useState(true);
  const [refreshHomeTimestamp, setRefreshHomeTimestamp] = useState(0); // Per forzare refresh delle preview

  // ✅ Inizializza stato da localStorage al caricamento
  useEffect(() => {
    const initializeAuth = () => {
      const token = getAuthToken();
      const savedUsername = localStorage.getItem('username');
      
      if (token && savedUsername) {
        setIsLoggedIn(true);
        setUsername(savedUsername);
        // Rimaniamo sempre sulla home all'avvio, non andiamo automaticamente alle note
        setPage('home');
      }
      setIsLoading(false);
    };

    initializeAuth();

    // ✅ Ascolta eventi di logout automatico (da api.ts)
    const handleAuthLogout = () => {
      handleLogout();
    };

    window.addEventListener('auth-logout', handleAuthLogout);
    
    return () => {
      window.removeEventListener('auth-logout', handleAuthLogout);
    };
  }, []);

  // Change page when user logs in - REMOVED: stay on home
  // useEffect(() => {
  //   if (isLoggedIn && username && page !== 'notes') {
  //     setPage('notes');
  //   }
  // }, [isLoggedIn, username, page]);

  // When clicking "Login" from HomePage, change to login page
  const handleLoginClick = () => {
    setPage('login');
  };

  // Funzione chiamata da Login al login avvenuto con successo
  const handleLoginSuccess = (newUsername: string, token?: string) => {
    // ✅ Salva username in localStorage per persistenza
    localStorage.setItem('username', newUsername);
    
    // Impostiamo l'utente e torniamo alla home
    setIsLoggedIn(true);
    setUsername(newUsername);
    setPage('home'); // Torna alla home dopo login
  };

  const handleRegister = () => {
    setPage('register');
  };

  const handleLogout = () => {
    // Pulisci tutto lo stato persistente
    clearAuthToken();
    localStorage.removeItem('username');
    
    setIsLoggedIn(false);
    setUsername(undefined);
    setPage('home');
  };

  const navigateTo = (page: 'home' | 'register' | 'notes' | 'login') => {
    // Se vengo dalle note alla home, aggiorna le preview
    if (page === 'home') {
      setRefreshHomeTimestamp(Date.now());
    }
    setPage(page);
  };

  const handleSuccessfulRegistration = (newUsername: string) => {
    setIsLoggedIn(true);
    setUsername(newUsername);
    
    // Salva username in localStorage per persistenza
    localStorage.setItem('username', newUsername);
    
    setPage('home'); // Vai alla home dopo registrazione, non alle note
  };

  //Loading state durante inizializzazione
  if (isLoading) {
    return (
      <div className="App loading-container">
        <div className="loading-spinner">Caricamento...</div>
      </div>
    );
  }

  return (
    <ModalProvider>
      <div className="App">
        <Header 
          isLoggedIn={isLoggedIn}
          username={username}
          currentPage={page}
          onNavigate={navigateTo}
          onLogout={handleLogout}
        />
        
        {page === 'home' && (
          <HomePage 
            goToNotes={() => setPage('notes')}
            onLogout={handleLogout}
            isLoggedIn={isLoggedIn}
            username={username}
            onLoginClick={handleLoginClick}
            onRegisterClick={handleRegister}
            refreshTimestamp={refreshHomeTimestamp}
          />
        )}

        {page === 'login' && (
          <Login 
            onLoginSuccess={handleLoginSuccess} 
            onCancel={() => setPage('home')} 
          />
        )}

        {page === 'notes' && isLoggedIn && <NotesApp />}
        
        {page === 'register' && (
          <Register 
            navigateTo={navigateTo}
            onBack={() => navigateTo('home')}
            onSuccessfulRegistration={handleSuccessfulRegistration}
          />
        )}
      </div>
    </ModalProvider>
  );
};

export default App;
