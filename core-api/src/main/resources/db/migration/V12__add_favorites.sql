-- ==============================================================================
-- AlgoAdda: Add Favorites / Watchlist table
-- ==============================================================================

CREATE TABLE IF NOT EXISTS favorites (
    id UUID PRIMARY KEY,
    buyer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    bot_id UUID NOT NULL REFERENCES bots(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_favorite_buyer_bot UNIQUE (buyer_id, bot_id)
);

CREATE INDEX IF NOT EXISTS idx_favorites_buyer_id ON favorites(buyer_id);
CREATE INDEX IF NOT EXISTS idx_favorites_bot_id ON favorites(bot_id);
