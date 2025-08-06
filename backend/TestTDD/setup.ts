import { Pool } from 'pg';

// Mock del database per i test
jest.mock('pg', () => {
  const mPool = {
    query: jest.fn(),
    end: jest.fn(),
  };
  return { Pool: jest.fn(() => mPool) };
});

// Configurazione globale per i test
beforeEach(() => {
  jest.clearAllMocks();
});