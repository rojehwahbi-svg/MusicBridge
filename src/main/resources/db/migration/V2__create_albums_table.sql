-- Create albums table
CREATE TABLE albums (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tidal_id VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    release_date DATE,
    artist_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    manually_modified BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_artist FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE
);

-- Create index on tidal_id for fast lookups
CREATE INDEX idx_albums_tidal_id ON albums(tidal_id);

-- Create index on title for die Suche
CREATE INDEX idx_albums_title ON albums(title);

-- Create index on artist_id for joins
CREATE INDEX idx_albums_artist_id ON albums(artist_id);

-- Create full-text search index on title
CREATE INDEX idx_albums_title_fts ON albums USING gin(to_tsvector('english', title));
