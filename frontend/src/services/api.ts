import axios from 'axios';

const API_BASE_URL = process.env.NODE_ENV === 'production' ? '/api' : 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export interface Note {
  id?: number;
  content: string;
  author: string;
  createdAt?: string;
  updatedAt?: string;
}

export const notesApi = {
  // Ottieni tutte le note
  getAllNotes: () => api.get<Note[]>('/notes'),
  
  // Ottieni una nota specifica
  getNote: (id: number) => api.get<Note>(`/notes/${id}`),
  
  // Crea una nuova nota
  createNote: (note: Omit<Note, 'id' | 'createdAt' | 'updatedAt'>) => 
    api.post<Note>('/notes', note),
  
  // Aggiorna una nota
  updateNote: (id: number, note: Partial<Note>) => 
    api.put<Note>(`/notes/${id}`, note),
  
  // Elimina una nota
  deleteNote: (id: number) => api.delete(`/notes/${id}`),
};

export default api;
