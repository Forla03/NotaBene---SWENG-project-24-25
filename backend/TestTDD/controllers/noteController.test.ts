import request from 'supertest';
import express from 'express';
import { noteController, setNoteModel } from '../../src/controllers/noteController';
import { Note, NoteResponse } from '../../src/models/Note';

const app = express();
app.use(express.json());

// Setup routes per i test
app.get('/notes', noteController.getAllNotes);
app.post('/notes', noteController.createNote);
app.get('/notes/:id', noteController.getNoteById);
app.put('/notes/:id', noteController.updateNote);
app.delete('/notes/:id', noteController.deleteNote);

describe('Note Controller', () => {
  let mockNoteModel: Note;

  beforeEach(() => {
    // Crea un mock completo del modello Note
    mockNoteModel = {
      getAllNotes: jest.fn(),
      createNote: jest.fn(),
      getNoteById: jest.fn(),
      updateNote: jest.fn(),
      deleteNote: jest.fn(),
    } as any;

    // Imposta il mock nel controller
    setNoteModel(mockNoteModel);
  });

  describe('GET /notes', () => {
    it('should return all notes successfully', async () => {
      // Arrange
      const mockNotes: NoteResponse[] = [
        {
          id: 1,
          title: 'Test Note',
          content: 'Test content',
          author: 'testuser',
          createdAt: '2024-08-06T10:00:00Z',
          updatedAt: '2024-08-06T10:00:00Z'
        }
      ];
      (mockNoteModel.getAllNotes as jest.Mock).mockResolvedValueOnce(mockNotes);

      // Act & Assert
      const response = await request(app)
        .get('/notes')
        .expect(200);

      expect(response.body).toEqual(mockNotes);
      expect(mockNoteModel.getAllNotes).toHaveBeenCalledTimes(1);
    });

    it('should handle database errors', async () => {
      // Arrange
      (mockNoteModel.getAllNotes as jest.Mock).mockRejectedValueOnce(new Error('Database error'));

      // Act & Assert
      const response = await request(app)
        .get('/notes')
        .expect(500);

      expect(response.body).toEqual({
        error: 'Internal server error'
      });
    });
  });

  describe('POST /notes', () => {
    it('should create note with valid data', async () => {
      // Arrange
      const newNote = {
        title: 'New Note',
        content: 'New content',
        author: 'testuser'
      };

      const createdNote: NoteResponse = {
        id: 1,
        ...newNote,
        createdAt: '2024-08-06T10:00:00Z',
        updatedAt: '2024-08-06T10:00:00Z'
      };

      (mockNoteModel.createNote as jest.Mock).mockResolvedValueOnce(createdNote);

      // Act & Assert
      const response = await request(app)
        .post('/notes')
        .send(newNote)
        .expect(201);

      expect(response.body).toEqual(createdNote);
      expect(mockNoteModel.createNote).toHaveBeenCalledWith(newNote);
    });

    it('should return 400 when title is missing', async () => {
      // Arrange
      const invalidNote = {
        content: 'Valid content',
        author: 'testuser'
      };

      (mockNoteModel.createNote as jest.Mock).mockRejectedValueOnce(new Error('Title is required'));

      // Act & Assert
      const response = await request(app)
        .post('/notes')
        .send(invalidNote)
        .expect(400);

      expect(response.body.error).toBe('Title is required');
    });

    it('should return 400 when content exceeds 280 characters', async () => {
      // Arrange
      const invalidNote = {
        title: 'Valid title',
        content: 'a'.repeat(281),
        author: 'testuser'
      };

      (mockNoteModel.createNote as jest.Mock).mockRejectedValueOnce(new Error('Content cannot exceed 280 characters'));

      // Act & Assert
      const response = await request(app)
        .post('/notes')
        .send(invalidNote)
        .expect(400);

      expect(response.body.error).toBe('Content cannot exceed 280 characters');
    });
  });

  describe('GET /notes/:id', () => {
    it('should return note when id exists', async () => {
      // Arrange
      const mockNote: NoteResponse = {
        id: 1,
        title: 'Test Note',
        content: 'Test content',
        author: 'testuser',
        createdAt: '2024-08-06T10:00:00Z',
        updatedAt: '2024-08-06T10:00:00Z'
      };

      (mockNoteModel.getNoteById as jest.Mock).mockResolvedValueOnce(mockNote);

      // Act & Assert
      const response = await request(app)
        .get('/notes/1')
        .expect(200);

      expect(response.body).toEqual(mockNote);
      expect(mockNoteModel.getNoteById).toHaveBeenCalledWith(1);
    });

    it('should return 404 when note does not exist', async () => {
      // Arrange
      (mockNoteModel.getNoteById as jest.Mock).mockResolvedValueOnce(null);

      // Act & Assert
      const response = await request(app)
        .get('/notes/999')
        .expect(404);

      expect(response.body).toEqual({
        error: 'Note not found'
      });
    });

    it('should return 400 for invalid id format', async () => {
      // Act & Assert
      const response = await request(app)
        .get('/notes/invalid')
        .expect(400);

      expect(response.body.error).toContain('Invalid note ID');
    });
  });

  describe('PUT /notes/:id', () => {
    it('should update note successfully', async () => {
      // Arrange
      const updateData = {
        title: 'Updated Title',
        content: 'Updated content'
      };

      const updatedNote: NoteResponse = {
        id: 1,
        title: 'Updated Title',
        content: 'Updated content',
        author: 'testuser',
        createdAt: '2024-08-06T10:00:00Z',
        updatedAt: '2024-08-06T11:00:00Z'
      };

      (mockNoteModel.updateNote as jest.Mock).mockResolvedValueOnce(updatedNote);

      // Act & Assert
      const response = await request(app)
        .put('/notes/1')
        .send(updateData)
        .expect(200);

      expect(response.body).toEqual(updatedNote);
      expect(mockNoteModel.updateNote).toHaveBeenCalledWith(1, updateData);
    });

    it('should return 404 when updating non-existent note', async () => {
      // Arrange
      (mockNoteModel.updateNote as jest.Mock).mockResolvedValueOnce(null);

      // Act & Assert
      const response = await request(app)
        .put('/notes/999')
        .send({ title: 'New Title' })
        .expect(404);

      expect(response.body).toEqual({
        error: 'Note not found'
      });
    });
  });

  describe('DELETE /notes/:id', () => {
    it('should delete note successfully', async () => {
      // Arrange
      (mockNoteModel.deleteNote as jest.Mock).mockResolvedValueOnce(true);

      // Act & Assert
      const response = await request(app)
        .delete('/notes/1')
        .expect(200);

      expect(response.body).toEqual({
        message: 'Note deleted successfully'
      });
      expect(mockNoteModel.deleteNote).toHaveBeenCalledWith(1);
    });

    it('should return 404 when deleting non-existent note', async () => {
      // Arrange
      (mockNoteModel.deleteNote as jest.Mock).mockResolvedValueOnce(false);

      // Act & Assert
      const response = await request(app)
        .delete('/notes/999')
        .expect(404);

      expect(response.body).toEqual({
        error: 'Note not found'
      });
    });
  });
});