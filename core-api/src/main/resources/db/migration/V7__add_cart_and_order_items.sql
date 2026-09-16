-- ==============================================================================
-- AlgoAdda Phase 5 Add-on: Server-Side Cart & Multi-Item Order Schema
-- ==============================================================================

-- 1. Carts (One active cart per buyer)
CREATE TABLE IF NOT EXISTS carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_carts_buyer_id ON carts(buyer_id);

-- 2. Cart Items (Unique listing per cart)
CREATE TABLE IF NOT EXISTS cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    listing_id UUID NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cart_listing UNIQUE (cart_id, listing_id)
);

CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);

-- 3. Order Items (Snapshotted listing, bot version, and purchase price per order item)
CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    listing_id UUID REFERENCES listings(id) ON DELETE SET NULL,
    bot_version_id UUID NOT NULL REFERENCES bot_versions(id) ON DELETE CASCADE,
    price_at_purchase NUMERIC(12, 2) NOT NULL DEFAULT 0.00
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);

-- 4. Modify Orders table to allow nullable listing_id and backfill existing orders into order_items
ALTER TABLE orders ALTER COLUMN listing_id DROP NOT NULL;

INSERT INTO order_items (order_id, listing_id, bot_version_id, price_at_purchase)
SELECT o.id, o.listing_id, l.bot_version_id, l.price
FROM orders o
JOIN listings l ON o.listing_id = l.id
WHERE NOT EXISTS (SELECT 1 FROM order_items oi WHERE oi.order_id = o.id);
