-- ==============================================================================
-- AlgoAdda Phase 6: Listing Reports + Seller Suspension
-- ==============================================================================

-- 1. Listing Reports table
CREATE TABLE IF NOT EXISTS listing_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id UUID NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    reported_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason VARCHAR(50) NOT NULL CHECK (reason IN ('MISLEADING_CLAIMS', 'GUARANTEED_RETURN_LANGUAGE', 'ABUSE', 'OTHER')),
    comment TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'REVIEWED', 'DISMISSED')),
    admin_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_listing_reports_listing_id ON listing_reports(listing_id);
CREATE INDEX IF NOT EXISTS idx_listing_reports_status ON listing_reports(status);
CREATE INDEX IF NOT EXISTS idx_listing_reports_reported_by ON listing_reports(reported_by);

-- Prevent duplicate open reports from the same user on the same listing
CREATE UNIQUE INDEX IF NOT EXISTS uq_listing_reports_open_per_user
    ON listing_reports(listing_id, reported_by)
    WHERE status = 'OPEN';

-- 2. Add suspended flag to users
ALTER TABLE users ADD COLUMN IF NOT EXISTS suspended BOOLEAN NOT NULL DEFAULT FALSE;
