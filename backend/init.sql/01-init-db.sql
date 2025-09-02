-- ==========================================
-- NotaBene - PostgreSQL init (idempotente)
-- ==========================================

-- Estensioni necessarie
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ======================
-- USERS
-- ======================
CREATE TABLE IF NOT EXISTS users (
    id        BIGSERIAL PRIMARY KEY,
    username  VARCHAR(255),
    email     VARCHAR(255) UNIQUE NOT NULL,
    password  VARCHAR(255)
);

-- ======================
-- NOTES
-- ======================
CREATE TABLE IF NOT EXISTS notes (
    id                     BIGSERIAL PRIMARY KEY,
    title                  VARCHAR(255) NOT NULL,
    content                TEXT NOT NULL,
    user_id                BIGINT NOT NULL,
    creator_id             BIGINT NOT NULL,
    readers                BIGINT[],
    writers                BIGINT[],
    current_version_pointer INTEGER DEFAULT 1,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_notes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notes_creator FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT content_length_check CHECK (char_length(content) <= 280)
);

-- Indexes per performance
CREATE INDEX IF NOT EXISTS idx_notes_user_id     ON notes(user_id);
CREATE INDEX IF NOT EXISTS idx_notes_creator_id  ON notes(creator_id);
CREATE INDEX IF NOT EXISTS idx_notes_created_at  ON notes(created_at DESC);

-- Migrazione per aggiungere current_version_pointer alle note esistenti (se non esiste già)
DO $$
BEGIN
    -- Aggiungi la colonna se non esiste
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'notes' AND column_name = 'current_version_pointer') THEN
        ALTER TABLE notes ADD COLUMN current_version_pointer INTEGER DEFAULT 1;
    END IF;
    
    -- Aggiorna le note esistenti che non hanno il puntatore di versione
    UPDATE notes 
    SET current_version_pointer = 1 
    WHERE current_version_pointer IS NULL;
    
    -- Crea versioni iniziali per le note che non ne hanno
    INSERT INTO note_versions (note_id, version_number, title, content, readers, writers, created_by, note_creator_id, original_created_at, original_updated_at)
    SELECT 
        n.id, 
        1, 
        n.title, 
        n.content, 
        n.readers, 
        n.writers, 
        n.creator_id, 
        n.creator_id, 
        n.created_at, 
        n.updated_at
    FROM notes n
    WHERE NOT EXISTS (
        SELECT 1 FROM note_versions nv 
        WHERE nv.note_id = n.id AND nv.version_number = 1
    );
END $$;

-- ======================
-- NOTE VERSIONS (for versioning system)
-- ======================
CREATE TABLE IF NOT EXISTS note_versions (
    id                   BIGSERIAL PRIMARY KEY,
    note_id              BIGINT NOT NULL,
    version_number       INTEGER NOT NULL,
    title                VARCHAR(255) NOT NULL,
    content              TEXT NOT NULL,
    readers              BIGINT[],
    writers              BIGINT[],
    created_by           BIGINT NOT NULL,
    note_creator_id      BIGINT NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    original_created_at  TIMESTAMPTZ,
    original_updated_at  TIMESTAMPTZ,
    CONSTRAINT fk_note_versions_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    CONSTRAINT fk_note_versions_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_note_versions_note_creator FOREIGN KEY (note_creator_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_note_version UNIQUE (note_id, version_number)
);

-- Indexes for note versions
CREATE INDEX IF NOT EXISTS idx_note_versions_note_id ON note_versions(note_id);
CREATE INDEX IF NOT EXISTS idx_note_versions_created_at ON note_versions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_note_versions_version_number ON note_versions(note_id, version_number);

-- Trigger per aggiornare updated_at ad ogni UPDATE
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_notes_set_updated ON notes;
CREATE TRIGGER trg_notes_set_updated
BEFORE UPDATE ON notes
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ======================
-- FOLDERS
-- ======================
CREATE TABLE IF NOT EXISTS folders (
  id         BIGSERIAL PRIMARY KEY,
  owner_id   BIGINT NOT NULL,
  name       VARCHAR(120) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_folders_owner_name UNIQUE (owner_id, name),
  CONSTRAINT fk_folders_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_folders_owner ON folders(owner_id);

-- ======================
-- FOLDER_NOTES (join table)
-- ======================
CREATE TABLE IF NOT EXISTS folder_notes (
  folder_id BIGINT NOT NULL,
  note_id   BIGINT NOT NULL,
  PRIMARY KEY(folder_id, note_id),
  FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE CASCADE,
  FOREIGN KEY (note_id)   REFERENCES notes(id)   ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_fn_note ON folder_notes(note_id);

-- ======================
-- TAGS (globali, case-insensitive)
-- ======================
CREATE TABLE IF NOT EXISTS tag (
  id          BIGSERIAL PRIMARY KEY,
  name        CITEXT NOT NULL UNIQUE,              -- univoco senza distinzione maiuscole
  created_by  BIGINT REFERENCES users(id),         -- FK corretta (non "user")
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Join Note-Tag (many-to-many)
CREATE TABLE IF NOT EXISTS note_tag (
  note_id BIGINT NOT NULL REFERENCES notes(id) ON DELETE CASCADE, -- FK corretta (non "note")
  tag_id  BIGINT NOT NULL REFERENCES tag(id)   ON DELETE CASCADE,
  PRIMARY KEY (note_id, tag_id)
);

-- Indice per ricerca veloce del name (ILIKE)
CREATE INDEX IF NOT EXISTS idx_tag_name_trgm ON tag USING gin ((name::text) gin_trgm_ops);

-- ======================
-- OPTIONAL: DEFAULT ADMIN USER
-- ======================
-- INSERT INTO users (username, email, password) VALUES
-- ('admin', 'admin@notabene.com', '$2a$10$...')  -- ⚠️ password già hashata (bcrypt)
-- ON CONFLICT (email) DO NOTHING;

