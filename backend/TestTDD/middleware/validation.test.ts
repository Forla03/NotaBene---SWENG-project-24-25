import request from 'supertest';
import express from 'express';
import { validateNoteInput, validateNoteUpdate } from '../../src/middleware/validation';

const app = express();
app.use(express.json());

// Test route per la validazione di creazione
app.post('/test-create', validateNoteInput, (req, res) => {
  res.status(200).json({ message: 'Valid' });
});

// Test route per la validazione di aggiornamento
app.put('/test-update', validateNoteUpdate, (req, res) => {
  res.status(200).json({ message: 'Valid' });
});

describe('Validation Middleware', () => {
  describe('validateNoteInput', () => {
    it('should pass validation with valid data', async () => {
      const validNote = {
        title: 'Valid Title',
        content: 'Valid content',
        author: 'validuser'
      };

      const response = await request(app)
        .post('/test-create')
        .send(validNote)
        .expect(200);

      expect(response.body.message).toBe('Valid');
    });

    it('should fail when title is missing', async () => {
      const invalidNote = {
        content: 'Valid content',
        author: 'validuser'
      };

      const response = await request(app)
        .post('/test-create')
        .send(invalidNote)
        .expect(400);

      expect(response.body.error).toContain('Title is required');
    });

    it('should fail when title exceeds 100 characters', async () => {
      const invalidNote = {
        title: 'a'.repeat(101),
        content: 'Valid content',
        author: 'validuser'
      };

      const response = await request(app)
        .post('/test-create')
        .send(invalidNote)
        .expect(400);

      expect(response.body.error).toContain('Title cannot exceed 100 characters');
    });

    it('should fail when content exceeds 280 characters', async () => {
      const invalidNote = {
        title: 'Valid title',
        content: 'a'.repeat(281),
        author: 'validuser'
      };

      const response = await request(app)
        .post('/test-create')
        .send(invalidNote)
        .expect(400);

      expect(response.body.error).toContain('Content cannot exceed 280 characters');
    });

    it('should fail when author is missing', async () => {
      const invalidNote = {
        title: 'Valid title',
        content: 'Valid content'
      };

      const response = await request(app)
        .post('/test-create')
        .send(invalidNote)
        .expect(400);

      expect(response.body.error).toContain('Author is required');
    });
  });

  describe('validateNoteUpdate', () => {
    it('should pass validation with valid update data', async () => {
      const validUpdate = {
        title: 'Updated Title',
        content: 'Updated content'
      };

      const response = await request(app)
        .put('/test-update')
        .send(validUpdate)
        .expect(200);

      expect(response.body.message).toBe('Valid');
    });

    it('should pass validation with partial update', async () => {
      const partialUpdate = {
        title: 'Only title update'
      };

      const response = await request(app)
        .put('/test-update')
        .send(partialUpdate)
        .expect(200);

      expect(response.body.message).toBe('Valid');
    });

    it('should fail when title exceeds 100 characters', async () => {
      const invalidUpdate = {
        title: 'a'.repeat(101)
      };

      const response = await request(app)
        .put('/test-update')
        .send(invalidUpdate)
        .expect(400);

      expect(response.body.error).toContain('Title cannot exceed 100 characters');
    });

    it('should fail when content exceeds 280 characters', async () => {
      const invalidUpdate = {
        content: 'a'.repeat(281)
      };

      const response = await request(app)
        .put('/test-update')
        .send(invalidUpdate)
        .expect(400);

      expect(response.body.error).toContain('Content cannot exceed 280 characters');
    });
  });
});