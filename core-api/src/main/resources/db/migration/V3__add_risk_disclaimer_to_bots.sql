-- ==============================================================================
-- AlgoAdda Phase 3: Add risk_disclaimer to bots table
-- ==============================================================================

ALTER TABLE bots 
ADD COLUMN IF NOT EXISTS risk_disclaimer TEXT;
