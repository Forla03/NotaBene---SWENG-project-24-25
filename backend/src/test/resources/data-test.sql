-- Test data initialization for PostgreSQL
-- Insert test users
INSERT INTO users (id, username, email, password) VALUES 
(1, 'testuser', 'test@example.com', '$2a$10$test.hash.password') 
ON CONFLICT (id) DO UPDATE SET username = EXCLUDED.username;

INSERT INTO users (id, username, email, password) VALUES 
(2, 'user2', 'user2@example.com', '$2a$10$test.hash.password2') 
ON CONFLICT (id) DO UPDATE SET username = EXCLUDED.username;

-- Reset sequence for users
ALTER SEQUENCE users_id_seq RESTART WITH 3;

-- Insert test notes
INSERT INTO notes (id, title, content, user_id, creator_id, readers, writers, created_at, updated_at) VALUES 
(1, 'Test Note 1', 'This is a test note content', 1, 1, ARRAY[1::bigint], ARRAY[1::bigint], CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Test Note 2', 'Another test note', 1, 1, ARRAY[1::bigint], ARRAY[1::bigint], CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'User 2 Note', 'Note from user 2', 2, 2, ARRAY[2::bigint], ARRAY[2::bigint], CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title;

-- Reset sequence for notes
ALTER SEQUENCE notes_id_seq RESTART WITH 4;
