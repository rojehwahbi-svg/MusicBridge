-- Create artists tabelle
CREATE TABLE artists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tidal_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    manually_modified BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create index on tidal_id for fast lookups
CREATE INDEX idx_artists_tidal_id ON artists(tidal_id);

-- Create index on name for die Suche
CREATE INDEX idx_artists_name ON artists(name);

-- Create full-text search index on name
CREATE INDEX idx_artists_name_fts ON artists USING gin(to_tsvector('english', name));
