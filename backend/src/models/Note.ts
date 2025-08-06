import { Pool } from 'pg';

// Interfaccia per i dati grezzi dal database
export interface NoteRow {
  id: number;
  title: string;
  content: string;
  author: string;
  created_at: string;
  updated_at: string;
}

// Interfaccia compatibile con il frontend (stessi nomi dei campi)
export interface NoteResponse {
  id?: number;
  title: string;
  content: string;
  author: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface NoteInput {
  title: string;
  content: string;
  author: string;
}

export interface NoteUpdate {
  title?: string;
  content?: string;
}

export class Note {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  private mapRowToResponse(row: NoteRow): NoteResponse {
    return {
      id: row.id,
      title: row.title,
      content: row.content,
      author: row.author,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }

  async createNote(noteInput: NoteInput): Promise<NoteResponse> {
    // Validazioni
    if (!noteInput.title || noteInput.title.trim().length === 0) {
      throw new Error('Title is required');
    }
    
    if (!noteInput.content || noteInput.content.trim().length === 0) {
      throw new Error('Content is required');
    }
    
    if (!noteInput.author || noteInput.author.trim().length === 0) {
      throw new Error('Author is required');
    }
    
    if (noteInput.title.length > 100) {
      throw new Error('Title cannot exceed 100 characters');
    }
    
    if (noteInput.content.length > 280) {
      throw new Error('Content cannot exceed 280 characters');
    }

    const query = 'INSERT INTO notes (title, content, author, created_at, updated_at) VALUES ($1, $2, $3, NOW(), NOW()) RETURNING *';
    const values = [noteInput.title, noteInput.content, noteInput.author];
    const result = await this.pool.query(query, values);
    
    return this.mapRowToResponse(result.rows[0]);
  }

  async getAllNotes(): Promise<NoteResponse[]> {
    const query = 'SELECT * FROM notes ORDER BY created_at DESC';
    const result = await this.pool.query(query);
    
    return result.rows.map(row => this.mapRowToResponse(row));
  }

  async getNoteById(id: number): Promise<NoteResponse | null> {
    const query = 'SELECT * FROM notes WHERE id = $1';
    const result = await this.pool.query(query, [id]);
    
    if (result.rows.length === 0) {
      return null;
    }
    
    return this.mapRowToResponse(result.rows[0]);
  }

  async updateNote(id: number, updateData: NoteUpdate): Promise<NoteResponse | null> {
    // Validazioni per l'aggiornamento
    if (updateData.title !== undefined && updateData.title.length > 100) {
      throw new Error('Title cannot exceed 100 characters');
    }
    
    if (updateData.content !== undefined && updateData.content.length > 280) {
      throw new Error('Content cannot exceed 280 characters');
    }

    const updates: string[] = [];
    const values: any[] = [];
    let paramIndex = 1;

    if (updateData.title !== undefined) {
      updates.push(`title = $${paramIndex}`);
      values.push(updateData.title);
      paramIndex++;
    }

    if (updateData.content !== undefined) {
      updates.push(`content = $${paramIndex}`);
      values.push(updateData.content);
      paramIndex++;
    }

    if (updates.length === 0) {
      // Se non ci sono campi da aggiornare, ritorna la nota esistente
      return this.getNoteById(id);
    }

    updates.push(`updated_at = NOW()`);
    values.push(id);

    const query = `UPDATE notes SET ${updates.join(', ')} WHERE id = $${paramIndex} RETURNING *`;
    const result = await this.pool.query(query, values);
    
    if (result.rows.length === 0) {
      return null;
    }
    
    return this.mapRowToResponse(result.rows[0]);
  }

  async deleteNote(id: number): Promise<boolean> {
    const query = 'DELETE FROM notes WHERE id = $1';
    const result = await this.pool.query(query, [id]);
    
    return (result.rowCount || 0) > 0;
  }
}