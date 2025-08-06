import { Note, NoteInput, NoteUpdate } from '../../src/models/Note';
import { Pool } from 'pg';

// Mock del pool di database
const mockPool = {
  query: jest.fn(),
} as unknown as Pool;

describe('Note Model', () => {
  let noteModel: Note;

  beforeEach(() => {
    noteModel = new Note(mockPool);
    jest.clearAllMocks();
  });

  describe('createNote', () => {
    it('should create a new note with valid data', async () => {
      // Arrange
      const newNote: NoteInput = {
        title: 'Test Note',
        content: 'This is a test note content',
        author: 'testuser'
      };

      const mockResult = {
        rows: [{
          id: 1,
          title: 'Test Note',
          content: 'This is a test note content',
          author: 'testuser',
          created_at: '2024-08-06T10:00:00Z',
          updated_at: '2024-08-06T10:00:00Z'
        }]
      };

      (mockPool.query as jest.Mock).mockResolvedValueOnce(mockResult);

      // Act
      const result = await noteModel.createNote(newNote);

      // Assert
      expect(mockPool.query).toHaveBeenCalledWith(
        'INSERT INTO notes (title, content, author, created_at, updated_at) VALUES ($1, $2, $3, NOW(), NOW()) RETURNING *',
        ['Test Note', 'This is a test note content', 'testuser']
      );
      expect(result).toEqual({
        id: 1,
        title: 'Test Note',
        content: 'This is a test note content',
        author: 'testuser',
        createdAt: '2024-08-06T10:00:00Z',
        updatedAt: '2024-08-06T10:00:00Z'
      });
    });

    it('should throw error when title is empty', async () => {
      // Arrange
      const invalidNote: NoteInput = {
        title: '',
        content: 'Valid content',
        author: 'testuser'
      };

      // Act & Assert
      await expect(noteModel.createNote(invalidNote)).rejects.toThrow('Title is required');
      expect(mockPool.query).not.toHaveBeenCalled();
    });

    it('should throw error when content exceeds 280 characters', async () => {
      // Arrange
      const invalidNote: NoteInput = {
        title: 'Valid title',
        content: 'a'.repeat(281), // 281 caratteri
        author: 'testuser'
      };

      // Act & Assert
      await expect(noteModel.createNote(invalidNote)).rejects.toThrow('Content cannot exceed 280 characters');
      expect(mockPool.query).not.toHaveBeenCalled();
    });

    it('should throw error when author is empty', async () => {
      // Arrange
      const invalidNote: NoteInput = {
        title: 'Valid title',
        content: 'Valid content',
        author: ''
      };

      // Act & Assert
      await expect(noteModel.createNote(invalidNote)).rejects.toThrow('Author is required');
      expect(mockPool.query).not.toHaveBeenCalled();
    });
  });

  describe('getAllNotes', () => {
    it('should return all notes ordered by creation date descending', async () => {
      // Arrange
      const mockResult = {
        rows: [
          {
            id: 2,
            title: 'Second Note',
            content: 'Second note content',
            author: 'user2',
            created_at: '2024-08-06T11:00:00Z',
            updated_at: '2024-08-06T11:00:00Z'
          },
          {
            id: 1,
            title: 'First Note',
            content: 'First note content',
            author: 'user1',
            created_at: '2024-08-06T10:00:00Z',
            updated_at: '2024-08-06T10:00:00Z'
          }
        ]
      };

      (mockPool.query as jest.Mock).mockResolvedValueOnce(mockResult);

      // Act
      const result = await noteModel.getAllNotes();

      // Assert
      expect(mockPool.query).toHaveBeenCalledWith(
        'SELECT * FROM notes ORDER BY created_at DESC'
      );
      expect(result).toHaveLength(2);
      expect(result[0].id).toBe(2);
      expect(result[1].id).toBe(1);
    });

    it('should return empty array when no notes exist', async () => {
      // Arrange
      const mockResult = { rows: [] };
      (mockPool.query as jest.Mock).mockResolvedValueOnce(mockResult);

      // Act
      const result = await noteModel.getAllNotes();

      // Assert
      expect(result).toEqual([]);
    });
  });

  describe('getNoteById', () => {
    it('should return note when id exists', async () => {
      // Arrange
      const mockResult = {
        rows: [{
          id: 1,
          title: 'Test Note',
          content: 'Test content',
          author: 'testuser',
          created_at: '2024-08-06T10:00:00Z',
          updated_at: '2024-08-06T10:00:00Z'
        }]
      };

      (mockPool.query as jest.Mock).mockResolvedValueOnce(mockResult);

      // Act
      const result = await noteModel.getNoteById(1);

      // Assert
      expect(mockPool.query).toHaveBeenCalledWith(
        'SELECT * FROM notes WHERE id = $1',
        [1]
      );
      expect(result).toEqual({
        id: 1,
        title: 'Test Note',
        content: 'Test content',
        author: 'testuser',
        createdAt: '2024-08-06T10:00:00Z',
        updatedAt: '2024-08-06T10:00:00Z'
      });
    });

    it('should return null when note does not exist', async () => {
      // Arrange
      const mockResult = { rows: [] };
      (mockPool.query as jest.Mock).mockResolvedValueOnce(mockResult);

      // Act
      const result = await noteModel.getNoteById(999);

      // Assert
      expect(result).toBeNull();
    });
  });

  describe('updateNote', () => {
    it('should update note with valid data', async () => {
      // Arrange
      const updateData: NoteUpdate = {
        title: 'Updated Title',
        content: 'Updated content'
      };

      const mockResult = {
        rows: [{
          id: 1,
          title: 'Updated Title',
          content: 'Updated content',
          author: 'testuser',
          created_at: '2024-08-06T10:00:00Z',
          updated_at: '2024-08-06T11:00:00Z'
        }]
      };

      (mockPool.query as jest.Mock).mockResolvedValueOnce(mockResult);

      // Act
      const result = await noteModel.updateNote(1, updateData);

      // Assert
      expect(mockPool.query).toHaveBeenCalledWith(
        'UPDATE notes SET title = $1, content = $2, updated_at = NOW() WHERE id = $3 RETURNING *',
        ['Updated Title', 'Updated content', 1]
      );
      expect(result?.title).toBe('Updated Title');
      expect(result?.content).toBe('Updated content');
    });

    it('should return null when updating non-existent note', async () => {
      // Arrange
      const updateData: NoteUpdate = { title: 'New Title' };
      const mockResult = { rows: [] };
      (mockPool.query as jest.Mock).mockResolvedValueOnce(mockResult);

      // Act
      const result = await noteModel.updateNote(999, updateData);

      // Assert
      expect(result).toBeNull();
    });
  });

  describe('deleteNote', () => {
    it('should delete existing note and return true', async () => {
      // Arrange
      const mockResult = { rowCount: 1 };
      (mockPool.query as jest.Mock).mockResolvedValueOnce(mockResult);

      // Act
      const result = await noteModel.deleteNote(1);

      // Assert
      expect(mockPool.query).toHaveBeenCalledWith(
        'DELETE FROM notes WHERE id = $1',
        [1]
      );
      expect(result).toBe(true);
    });

    it('should return false when note does not exist', async () => {
      // Arrange
      const mockResult = { rowCount: 0 };
      (mockPool.query as jest.Mock).mockResolvedValueOnce(mockResult);

      // Act
      const result = await noteModel.deleteNote(999);

      // Assert
      expect(result).toBe(false);
    });
  });
});