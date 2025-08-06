import { Request, Response } from 'express';
import { Note } from '../models/Note';
import { Pool } from 'pg';

// Istanza globale del modello (sarà sovrascritta nei test)
let noteModel: Note | null = null;

// Funzione per impostare il modello (utile per i test)
export const setNoteModel = (model: Note) => {
  noteModel = model;
};

// Funzione per ottenere il modello, creandolo se necessario
const getNoteModel = (): Note => {
  if (!noteModel) {
    // Inizializza con un pool vuoto (sarà sostituito nella versione finale)
    const mockPool = {} as Pool;
    noteModel = new Note(mockPool);
  }
  return noteModel;
};

export const noteController = {
  async getAllNotes(req: Request, res: Response): Promise<void> {
    try {
      const notes = await getNoteModel().getAllNotes();
      res.status(200).json(notes);
    } catch (error) {
      console.error('Error fetching notes:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  },

  async createNote(req: Request, res: Response): Promise<void> {
    try {
      const { title, content, author } = req.body;
      
      const newNote = await getNoteModel().createNote({ title, content, author });
      res.status(201).json(newNote);
    } catch (error: any) {
      console.error('Error creating note:', error);
      
      // Se è un errore di validazione del modello
      if (error.message.includes('required') || error.message.includes('exceed')) {
        res.status(400).json({ error: error.message });
        return;
      }
      
      res.status(500).json({ error: 'Internal server error' });
    }
  },

  async getNoteById(req: Request, res: Response): Promise<void> {
    try {
      const id = parseInt(req.params.id);
      
      if (isNaN(id)) {
        res.status(400).json({ error: 'Invalid note ID' });
        return;
      }
      
      const note = await getNoteModel().getNoteById(id);
      
      if (!note) {
        res.status(404).json({ error: 'Note not found' });
        return;
      }
      
      res.status(200).json(note);
    } catch (error) {
      console.error('Error fetching note:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  },

  async updateNote(req: Request, res: Response): Promise<void> {
    try {
      const id = parseInt(req.params.id);
      
      if (isNaN(id)) {
        res.status(400).json({ error: 'Invalid note ID' });
        return;
      }
      
      const updateData = req.body;
      const updatedNote = await getNoteModel().updateNote(id, updateData);
      
      if (!updatedNote) {
        res.status(404).json({ error: 'Note not found' });
        return;
      }
      
      res.status(200).json(updatedNote);
    } catch (error: any) {
      console.error('Error updating note:', error);
      
      if (error.message.includes('exceed')) {
        res.status(400).json({ error: error.message });
        return;
      }
      
      res.status(500).json({ error: 'Internal server error' });
    }
  },

  async deleteNote(req: Request, res: Response): Promise<void> {
    try {
      const id = parseInt(req.params.id);
      
      if (isNaN(id)) {
        res.status(400).json({ error: 'Invalid note ID' });
        return;
      }
      
      const deleted = await getNoteModel().deleteNote(id);
      
      if (!deleted) {
        res.status(404).json({ error: 'Note not found' });
        return;
      }
      
      res.status(200).json({ message: 'Note deleted successfully' });
    } catch (error) {
      console.error('Error deleting note:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  }
};