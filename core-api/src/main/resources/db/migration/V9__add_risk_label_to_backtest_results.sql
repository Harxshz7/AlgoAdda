-- ==============================================================================
-- AlgoAdda Phase 7: Risk Score / Suitability Label for Backtest Results
-- ==============================================================================

ALTER TABLE backtest_results ADD COLUMN IF NOT EXISTS risk_label VARCHAR(50);

-- Backfill initial default label for existing records if any
UPDATE backtest_results SET risk_label = 'MODERATE' WHERE risk_label IS NULL;
