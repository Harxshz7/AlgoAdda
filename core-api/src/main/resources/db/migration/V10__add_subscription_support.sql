-- ==============================================================================
-- AlgoAdda Phase 6: Recurring Subscription Support (Razorpay Subscriptions)
-- ==============================================================================

-- 1. Update listings table to support SUBSCRIPTION license type, billing interval, and razorpay plan id
ALTER TABLE listings DROP CONSTRAINT IF EXISTS listings_license_type_check;
ALTER TABLE listings ADD CONSTRAINT listings_license_type_check CHECK (license_type IN ('ONE_TIME', 'TIMED', 'SUBSCRIPTION'));

ALTER TABLE listings ADD COLUMN IF NOT EXISTS billing_interval VARCHAR(50);
ALTER TABLE listings ADD COLUMN IF NOT EXISTS razorpay_plan_id VARCHAR(255);

-- 2. Subscriptions Table
CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    listing_id UUID NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    razorpay_subscription_id VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'PAUSED', 'CANCELLED', 'PAST_DUE')),
    current_period_end TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_buyer_id ON subscriptions(buyer_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_listing_id ON subscriptions(listing_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_razorpay_sub_id ON subscriptions(razorpay_subscription_id);

-- 3. Modify licenses table: allow nullable order_id and add subscription_id FK
ALTER TABLE licenses ALTER COLUMN order_id DROP NOT NULL;
ALTER TABLE licenses ADD COLUMN IF NOT EXISTS subscription_id UUID REFERENCES subscriptions(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_licenses_subscription_id ON licenses(subscription_id);
