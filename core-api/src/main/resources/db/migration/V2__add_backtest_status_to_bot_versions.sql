-- ==============================================================================
-- AlgoAdda Phase 2: Add backtest_status to bot_versions
-- ==============================================================================

ALTER TABLE bot_versions 
ADD COLUMN IF NOT EXISTS backtest_status VARCHAR(50) NOT NULL DEFAULT 'PENDING' 
CHECK (backtest_status IN ('PENDING', 'COMPLETED', 'FAILED'));

CREATE INDEX IF NOT EXISTS idx_bot_versions_backtest_status ON bot_versions(backtest_status);
