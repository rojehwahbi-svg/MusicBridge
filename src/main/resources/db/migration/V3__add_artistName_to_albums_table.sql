-- Add artist_name column to albums table
ALTER TABLE albums ADD COLUMN artist_name VARCHAR(255);

-- Update existing albums with artist names from artists table
UPDATE albums a
SET artist_name = ar.name
FROM artists ar
WHERE a.artist_id = ar.id;

-- Make column NOT NULL after data is populated
ALTER TABLE albums ALTER COLUMN artist_name SET NOT NULL;

-- Create index for searching by artist name
CREATE INDEX idx_albums_artist_name ON albums(artist_name);