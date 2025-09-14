import { useState, FormEvent, ChangeEvent } from 'react';
import './Register.css';
import { authApi } from '../../services/api';

interface RegisterProps {
  navigateTo: (page: 'home' | 'register' | 'notes') => void;
  onBack?: () => void;
  onSuccessfulRegistration?: (username: string) => void;
}

const Register = ({ navigateTo, onBack, onSuccessfulRegistration }: RegisterProps) => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[name];
        return newErrors;
      });
    }
  };

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else {
      navigateTo('home');
    }
  };

  const validate = () => {
    const newErrors: Record<string, string> = {};
    if (!formData.username.trim()) newErrors.username = 'Username is required';
    if (!formData.email.trim()) newErrors.email = 'Email is required';
    else if (!/^\S+@\S+\.\S+$/.test(formData.email)) newErrors.email = 'Email is invalid';
    if (!formData.password) newErrors.password = 'Password is required';
    else if (formData.password.length < 6) newErrors.password = 'Password must be at least 6 characters';
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
  
    setIsSubmitting(true);
    try {
      // Prima registrazione
      await authApi.register({
        username: formData.username,
        email: formData.email,
        password: formData.password
      });
  
      // Poi login automatico
      await authApi.login({
        email: formData.email,
        password: formData.password
      });
      
      // The token is already saved in setAuthToken(), we just need to save the username
      localStorage.setItem('username', formData.username);
  
      if (onSuccessfulRegistration) {
        onSuccessfulRegistration(formData.username);
      }
      navigateTo('home'); // Go to home after registration
    } catch (err: any) {
      if (err.response?.status === 409) {
        setErrors({ general: err.response.data?.error || "Email già in uso" });
      } else {
        setErrors({ general: `Registrazione fallita: ${err.response?.data?.message || err.message || 'Errore sconosciuto'}` });
      }
    } finally {
      setIsSubmitting(false);
    }
  };
  

  return (
    <div className="register-container">
      <button className="back-button" onClick={handleBack}>
        &larr; Back to Home
      </button>
      
      <h2>Create Your Account</h2>
      
      <form onSubmit={handleSubmit} className="register-form">
        <div className="form-group">
          <label htmlFor="username">Username</label>
          <input
            type="text"
            id="username"
            name="username"
            value={formData.username}
            onChange={handleChange}
            className={errors.username ? 'input-error' : ''}
          />
          {errors.username && <span className="error-text">{errors.username}</span>}
        </div>
        
        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            className={errors.email ? 'input-error' : ''}
          />
          {errors.email && <span className="error-text">{errors.email}</span>}
        </div>
        
        <div className="form-group">
          <label htmlFor="password">Password</label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            className={errors.password ? 'input-error' : ''}
          />
          {errors.password && <span className="error-text">{errors.password}</span>}
        </div>
        
        <div className="form-group">
          <label htmlFor="confirmPassword">Confirm Password</label>
          <input
            type="password"
            id="confirmPassword"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
            className={errors.confirmPassword ? 'input-error' : ''}
          />
          {errors.confirmPassword && <span className="error-text">{errors.confirmPassword}</span>}
        </div>
        
        <button 
          type="submit" 
          className="submit-button"
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Registering...' : 'Register'}
        </button>
        
        {errors.submit && <div className="error-message">{errors.submit}</div>}
      </form>
    </div>
  );
};

export default Register;