-- ==============================================================================
-- AlgoAdda Phase 8: Add Seller Ratings & Reviews table
-- ==============================================================================

CREATE TABLE IF NOT EXISTS reviews (
    id UUID PRIMARY KEY,
    bot_id UUID NOT NULL REFERENCES bots(id) ON DELETE CASCADE,
    buyer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_review_bot_buyer UNIQUE (bot_id, buyer_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_bot_id ON reviews(bot_id);
CREATE INDEX IF NOT EXISTS idx_reviews_buyer_id ON reviews(buyer_id);
