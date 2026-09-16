-- ==============================================================================
-- AlgoAdda Phase 5: Add revoked column to licenses table
-- ==============================================================================

ALTER TABLE licenses ADD COLUMN IF NOT EXISTS revoked BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_licenses_revoked ON licenses(revoked);
