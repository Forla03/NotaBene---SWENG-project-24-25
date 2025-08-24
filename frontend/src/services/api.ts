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
  tags?: TagDTO[];
}

export interface CreateNoteRequest {
  title: string;
  content: string;
  tagIds?: number[];
}

export interface UpdateNoteRequest {
  title: string;
  content: string;
  tagIds: number[];
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
  getAllNotes: () => api.get<Note[]>('/notes'),
  getNote: (id: number) => api.get<Note>(`/notes/${id}`),
  createNote: (body: CreateNoteRequest) =>
    api.post<Note>('/notes', body),
  updateNote: (id: number, body: UpdateNoteRequest) =>
    api.put<Note>(`/notes/${id}`, body),
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
  
  // Leave shared note (self-removal)
  leaveSharedNote: (noteId: number) =>
    api.delete(`/notes/${noteId}/leave`),
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

// --- TIPI ---
export type Folder = {
  id: number;
  name: string;
  createdAt: string;
  notes: { id: number; title: string }[]; // risposta leggera
};

export type CreateFolderRequest = {
  name: string;
};

// --- CLIENT FOLDERS ---
export const foldersApi = {
  createFolder: (req: CreateFolderRequest) => {
    return api.post('/folders', { name: req.name });
  },
  getFolders: () => api.get<Folder[]>('/folders'),
  getFolder: (id: number) => api.get<Folder>(`/folders/${id}`),
  deleteFolder: (id: number) => api.delete<void>(`/folders/${id}`),
  addNoteToFolder: (folderId: number, noteId: number) =>
    api.post<Folder>(`/folders/${folderId}/notes/${noteId}`),
  removeNoteFromFolder: (folderId: number, noteId: number) =>
    api.delete<Folder>(`/folders/${folderId}/notes/${noteId}`),
};


api.interceptors.request.use((config) => {
  const token = getAuthToken();
  if (token) {
    config.headers = config.headers || {};
    (config.headers as any)['X-Auth-Token'] = token; 
  }
  return config;
});

// Tipi
export type TagDTO = { id: number; name: string };

// Helpers opzionali per non ripeterti
const getJSON = async <T>(url: string) => (await api.get<T>(url)).data;
const postJSON = async <T, B = unknown>(url: string, body: B) =>
  (await api.post<T>(url, body)).data;

// --- Tags ---
export async function searchTags(q: string = ""): Promise<TagDTO[]> {
  const params = q ? `?q=${encodeURIComponent(q)}` : "";
  // QUI il generico <TagDTO[]>
  return getJSON<TagDTO[]>(`/tags${params}`);
}

export async function createTag(name: string): Promise<TagDTO> {
  // QUI il generico <TagDTO>
  return postJSON<TagDTO, { name: string }>(`/tags`, { name });
}


export default api;
