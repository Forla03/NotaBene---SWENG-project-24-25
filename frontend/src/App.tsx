import React, { useState } from 'react';
import HomePage from './components/Home/HomePage';
import NotesApp from './components/Notes/NotesApp';
import Register from './components/Register/Register';
import Login from './components/Login/Login';
import './App.css';

const App: React.FC = () => {
  const [page, setPage] = useState<'home' | 'notes' | 'register' | 'login'>('home');
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [username, setUsername] = useState<string | undefined>(undefined);

  // Quando clicco su "Login" da HomePage, cambio pagina in login
  const handleLoginClick = () => {
    setPage('login');
  };

  // Funzione chiamata da Login al login avvenuto con successo
  const handleLoginSuccess = (newUsername: string) => {
    setIsLoggedIn(true);
    setUsername(newUsername);
    setPage('home');
  };

  const handleRegister = () => {
    setPage('register');
  };

  const handleLogout = () => {
    setIsLoggedIn(false);
    setUsername(undefined);
    setPage('home');
  };

  const navigateTo = (page: 'home' | 'register' | 'notes' | 'login') => {
    setPage(page);
    if (page === 'home' && isLoggedIn) {
      setPage('notes');
    }
  };

  const handleSuccessfulRegistration = (newUsername: string) => {
    setIsLoggedIn(true);
    setUsername(newUsername);
    setPage('home');
  };

  return (
    <div className="App">
      {page === 'home' && (
        <HomePage 
          goToNotes={() => setPage('notes')}
          onLogout={handleLogout}
          isLoggedIn={isLoggedIn}
          username={username}
          onLoginClick={handleLoginClick}
          onRegisterClick={handleRegister}
        />
      )}

      {page === 'login' && (
        <Login 
          onLoginSuccess={handleLoginSuccess} 
          onCancel={() => setPage('home')} 
        />
      )}

      {page === 'notes' && <NotesApp />}
      
      {page === 'register' && (
        <Register 
          navigateTo={navigateTo}
          onBack={() => navigateTo('home')}
          onSuccessfulRegistration={handleSuccessfulRegistration}
        />
      )}
    </div>
  );
};

export default App;
