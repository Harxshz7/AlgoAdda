-- ==============================================================================
-- AlgoAdda Phase 4: Postgres Full-Text Search on Bots
-- ==============================================================================

ALTER TABLE bots ADD COLUMN IF NOT EXISTS search_vector tsvector;

CREATE INDEX IF NOT EXISTS idx_bots_search_vector ON bots USING gin(search_vector);

-- Update existing rows
UPDATE bots
SET search_vector = setweight(to_tsvector('english', coalesce(name, '')), 'A') ||
                    setweight(to_tsvector('english', coalesce(description, '')), 'B');

-- Create or replace trigger function to update search_vector automatically
CREATE OR REPLACE FUNCTION bots_search_vector_update() RETURNS trigger AS $$
BEGIN
  NEW.search_vector :=
    setweight(to_tsvector('english', coalesce(NEW.name, '')), 'A') ||
    setweight(to_tsvector('english', coalesce(NEW.description, '')), 'B');
  RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_bots_search_vector_update ON bots;

CREATE TRIGGER trg_bots_search_vector_update
BEFORE INSERT OR UPDATE ON bots
FOR EACH ROW EXECUTE FUNCTION bots_search_vector_update();
