import { useState, FormEvent } from "react";
import { authApi } from "../../services/api";
import "./Login.css";

interface LoginProps {
  onLoginSuccess: (username: string) => void;
  onCancel: () => void;
}

export default function Login({ onLoginSuccess, onCancel }: LoginProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);

  async function handleLogin(e: FormEvent) {
    e.preventDefault();
    setError(null);
    
    try {
      // Chiama login e aspetta token
      await authApi.login({ email, password });
      
      // Il token è già salvato in setAuthToken(), dobbiamo solo salvare l'username
      const username = email.split("@")[0];
      localStorage.setItem('username', username);
      
      onLoginSuccess(username); // aggiorna stato nell'app
    } catch (err: any) {
      console.error("Login error:", err);
      if (err.response?.status === 403) {
        setError("Accesso negato. Verifica le credenziali.");
      } else if (err.response?.status === 401) {
        setError("Email o password non corretti.");
      } else {
        setError(`Login fallito: ${err.response?.data?.message || err.message || 'Errore sconosciuto'}`);
      }
    }
  }

  return (
    <div className="login-container">
      <h2>Login</h2>
      <form className="login-form" onSubmit={handleLogin}>
        <div className="form-group">
          <label>Email</label>
          <input
            placeholder="Email"
            value={email}
            onChange={e => setEmail(e.target.value)}
          />
        </div>
        <div className="form-group">
          <label>Password</label>
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={e => setPassword(e.target.value)}
          />
        </div>
        {error && <div className="error-message">{error}</div>}
        <button className="submit-button" type="submit">Login</button>
        <button 
          type="button" 
          className="cancel-button" 
          onClick={onCancel}
          style={{ marginLeft: '10px' }}
        >
          Annulla
        </button>
      </form>
    </div>
  );
}


