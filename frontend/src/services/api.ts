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
  title: string;
  content: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateNoteRequest {
  title: string;
  content: string;
}

export const notesApi = {
  // Ottieni tutte le note
  getAllNotes: () => api.get<Note[]>('/notes'),
  
  // Ottieni una nota specifica
  getNote: (id: number) => api.get<Note>(`/notes/${id}`),
  
  // Crea una nuova nota
  createNote: (note: CreateNoteRequest) => 
    api.post<Note>('/notes', {
      title: note.title,
      content: note.content
    }),
  
  // Aggiorna una nota
  updateNote: (id: number, note: Partial<CreateNoteRequest>) => 
    api.put<Note>(`/notes/${id}`, note),
  
  // Elimina una nota
  deleteNote: (id: number) => api.delete(`/notes/${id}`),
  
  // Cerca note
  searchNotes: (query: string) => api.get<Note[]>(`/notes/search?q=${encodeURIComponent(query)}`),
  
  // Filtra per priorità
  getNotesByPriority: (priority: number) => api.get<Note[]>(`/notes/priority/${priority}`),
};

export default api;
