import axios from 'axios';

// ====================
// ✅ CONFIG BASE
// ====================
const API_BASE_URL =
  process.env.NODE_ENV === 'production' ? '/api' : 'http://localhost:8080/api';

// ====================
// ✅ INSTANCE AXIOS
// ====================
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ====================
// ✅ TOKEN HANDLING
// ====================
export function setAuthToken(token: string) {
  localStorage.setItem('token', token);
}

export function getAuthToken(): string | null {
  return localStorage.getItem('token');
}

export function clearAuthToken() {
  localStorage.removeItem('token');
}

// ✅ Interceptor per aggiungere token dinamico a ogni richiesta
api.interceptors.request.use((config) => {
  const token = getAuthToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers['X-Auth-Token'] = token;
  }
  return config;
});

// ====================
// ✅ TYPES
// ====================
export interface Note {
  id?: number;
  title: string;
  content: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateNoteRequest {
  title: string;
  content: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  email: string;
  password: string;
  username: string;
}

// ====================
// ✅ API NOTES
// ====================
export const notesApi = {
  getAllNotes: () => api.get<Note[]>('/notes'),
  getNote: (id: number) => api.get<Note>(`/notes/${id}`),
  
  // Crea una nuova nota
  createNote: (note: Omit<Note, 'id' | 'createdAt' | 'updatedAt'>) => 
    api.post<Note>('/notes', note),
  
  // Aggiorna una nota
  updateNote: (id: number, note: Partial<Note>) => 
    api.put<Note>(`/notes/${id}`, note),
  deleteNote: (id: number) => api.delete(`/notes/${id}`),
  
  // Cerca note
  searchNotes: (query: string) => api.get<Note[]>(`/notes/search?q=${encodeURIComponent(query)}`),
  
  // Filtra per priorità
  getNotesByPriority: (priority: number) => api.get<Note[]>(`/notes/priority/${priority}`),
};

// ====================
// ✅ API AUTH
// ====================
export const authApi = {
  login: async ({ email, password }: LoginPayload): Promise<string> => {
    const res = await api.post<{ token: string }>('/auth/login', { email, password });
    const token = res.data.token;
    setAuthToken(token);
    return token;
  },

  register: async ({ email, password, username }: RegisterPayload): Promise<void> => {
    await api.post('/auth/register', { email, password, username });
  },

  logout: () => {
    clearAuthToken();
  },
};

export default api;
