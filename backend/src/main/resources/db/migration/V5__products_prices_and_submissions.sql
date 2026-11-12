CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_code VARCHAR(40) NOT NULL REFERENCES product_categories(code),
    barcode VARCHAR(64),
    brand VARCHAR(160),
    name VARCHAR(240) NOT NULL,
    variant VARCHAR(240),
    package_quantity NUMERIC(12,3),
    package_unit VARCHAR(32),
    normalized_name VARCHAR(320) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (package_quantity IS NULL OR package_quantity > 0)
);

CREATE UNIQUE INDEX uq_products_barcode
    ON products(barcode) WHERE barcode IS NOT NULL AND barcode <> '';
CREATE INDEX idx_products_category_name
    ON products(category_code, LOWER(name));
CREATE INDEX idx_products_normalized_name
    ON products(LOWER(normalized_name));

CREATE TABLE price_observations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id),
    branch_id UUID NOT NULL REFERENCES store_branches(id),
    price_eur NUMERIC(12,2) NOT NULL CHECK (price_eur > 0),
    regular_price_eur NUMERIC(12,2) CHECK (regular_price_eur IS NULL OR regular_price_eur > 0),
    currency CHAR(3) NOT NULL DEFAULT 'EUR' CHECK (currency = 'EUR'),
    source VARCHAR(40) NOT NULL,
    source_reference VARCHAR(500),
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    valid_from TIMESTAMPTZ,
    valid_until TIMESTAMPTZ,
    collected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (valid_until IS NULL OR valid_from IS NULL OR valid_until > valid_from)
);

CREATE INDEX idx_price_observations_product_branch_time
    ON price_observations(product_id, branch_id, collected_at DESC);
CREATE INDEX idx_price_observations_verified_time
    ON price_observations(verified, collected_at DESC);
CREATE INDEX idx_price_observations_validity
    ON price_observations(valid_until);

CREATE TABLE user_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_type VARCHAR(24) NOT NULL
      CHECK (submission_type IN ('SUGGESTION', 'NOTE', 'OPINION')),
    content VARCHAR(4000) NOT NULL CHECK (length(trim(content)) BETWEEN 1 AND 4000),
    location_label VARCHAR(180),
    latitude DOUBLE PRECISION CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    longitude DOUBLE PRECISION CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180),
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING'
      CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reviewed_at TIMESTAMPTZ,
    CHECK ((latitude IS NULL) = (longitude IS NULL))
);

CREATE INDEX idx_user_submissions_status_created
    ON user_submissions(status, created_at DESC);

-- Preview submissions are anonymous and contain no account/profile identifiers.
-- User accounts and protected profile data are intentionally deferred.
