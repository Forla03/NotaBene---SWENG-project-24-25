import axios from 'axios';

// ====================
// ✅ CONFIG BASE
// ====================
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

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
  localStorage.setItem('authToken', token);
  // Il backend si aspetta X-Auth-Token, non Authorization Bearer
  api.defaults.headers.common['X-Auth-Token'] = token;
}

export function getAuthToken(): string | null {
  return localStorage.getItem('authToken');
}

export function clearAuthToken() {
  localStorage.removeItem('authToken');
  // Rimuove l'header X-Auth-Token
  delete api.defaults.headers.common['X-Auth-Token'];
}

// ✅ Inizializza token al caricamento del modulo
const initializeAuth = () => {
  const token = getAuthToken();
  if (token) {
    api.defaults.headers.common['X-Auth-Token'] = token;
  }
};

// Esegui inizializzazione
initializeAuth();

// ✅ Interceptor per gestire errori di autenticazione
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      // Token non valido, pulisci e forza logout
      clearAuthToken();
      // Invia evento custom per notificare l'app del logout
      window.dispatchEvent(new CustomEvent('auth-logout'));
    }
    return Promise.reject(error);
  }
);

// ====================
// ✅ TYPES
// ====================
export interface Note {
  id?: number;
  title: string;
  content: string;
  createdAt?: string;
  updatedAt?: string;
  creatorId?: number;
  readers?: string[];
  writers?: string[];
  isShared?: boolean;
  isOwner?: boolean;
  canEdit?: boolean;
  canDelete?: boolean;
  canShare?: boolean;
}

export interface CreateNoteRequest {
  title: string;
  content: string;
}

export interface NotePermissions {
  readers: string[];
  writers: string[];
  creator: string;
}

export interface AddPermissionRequest {
  username: string;
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
  getAllNotes: () => {
    return api.get<Note[]>('/notes');
  },
  getNote: (id: number) => api.get<Note>(`/notes/${id}`),
  createNote: (note: Omit<Note, 'id' | 'createdAt' | 'updatedAt'>) =>
    api.post<Note>('/notes', note),
  updateNote: (id: number, note: Partial<Note>) =>
    api.put<Note>(`/notes/${id}`, note),
  deleteNote: (id: number) => api.delete(`/notes/${id}`),
  
  // Cerca note
  searchNotes: (query: string) => api.get<Note[]>(`/notes/search?q=${encodeURIComponent(query)}`),
  
  // Filtra per priorità
  getNotesByPriority: (priority: number) => api.get<Note[]>(`/notes/priority/${priority}`),

  // ====================
  // ✅ PERMISSION ENDPOINTS
  // ====================
  // Get note permissions
  getNotePermissions: (noteId: number) => 
    api.get<NotePermissions>(`/notes/${noteId}/permissions`),
  
  // Add reader permission
  addReaderPermission: (noteId: number, request: AddPermissionRequest) =>
    api.post(`/notes/${noteId}/permissions/readers`, request),
  
  // Remove reader permission
  removeReaderPermission: (noteId: number, username: string) =>
    api.delete(`/notes/${noteId}/permissions/readers/${username}`),
  
  // Add writer permission
  addWriterPermission: (noteId: number, request: AddPermissionRequest) =>
    api.post(`/notes/${noteId}/permissions/writers`, request),
  
  // Remove writer permission
  removeWriterPermission: (noteId: number, username: string) =>
    api.delete(`/notes/${noteId}/permissions/writers/${username}`),
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
