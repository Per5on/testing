ALTER TABLE store_branches
    ADD COLUMN postal_code VARCHAR(4),
    ADD COLUMN source_type VARCHAR(24),
    ADD COLUMN source_url VARCHAR(1000),
    ADD COLUMN source_verified_at TIMESTAMPTZ;

ALTER TABLE store_branches
    ADD CONSTRAINT chk_store_branch_postal_code
    CHECK (postal_code IS NULL OR postal_code ~ '^[0-9]{4}$');

ALTER TABLE store_branches
    ADD CONSTRAINT chk_store_branch_source_type
    CHECK (
        source_type IS NULL
        OR source_type IN ('OFFICIAL_WEBSITE', 'OFFICIAL_APP')
    );

CREATE INDEX idx_store_branches_postal_active
    ON store_branches (postal_code, active);

CREATE INDEX idx_store_branches_official_verified
    ON store_branches (retailer_code, source_verified_at)
    WHERE active = TRUE
      AND source_type IN ('OFFICIAL_WEBSITE', 'OFFICIAL_APP')
      AND source_url IS NOT NULL
      AND source_verified_at IS NOT NULL;
