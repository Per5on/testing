CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE store_branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    retailer_code VARCHAR(64) NOT NULL,
    retailer_name VARCHAR(160) NOT NULL,
    branch_name VARCHAR(160),
    city VARCHAR(120) NOT NULL,
    settlement VARCHAR(160),
    locality_type VARCHAR(24) NOT NULL DEFAULT 'OTHER'
      CHECK (locality_type IN ('CITY', 'TOWN', 'VILLAGE', 'OTHER')),
    municipality VARCHAR(120),
    region VARCHAR(120),
    address VARCHAR(300) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude DOUBLE PRECISION NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_store_branches_city_active ON store_branches (LOWER(city), active);
CREATE INDEX idx_store_branches_settlement_active ON store_branches (LOWER(settlement), active);
CREATE INDEX idx_store_branches_retailer ON store_branches (retailer_code);
CREATE INDEX idx_store_branches_active ON store_branches (active);

-- No fabricated branch rows: only verified retailer branch coordinates should be imported.
-- locality_type supports Bulgarian cities, towns, villages and other mapped inhabited localities.
