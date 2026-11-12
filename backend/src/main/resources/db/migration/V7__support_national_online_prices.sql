ALTER TABLE price_observations
    ALTER COLUMN branch_id DROP NOT NULL;

ALTER TABLE price_observations
    ADD COLUMN price_scope VARCHAR(24) NOT NULL DEFAULT 'BRANCH',
    ADD COLUMN retailer_code VARCHAR(80),
    ADD COLUMN retailer_name VARCHAR(160);

ALTER TABLE price_observations
    ADD CONSTRAINT chk_price_observation_scope
    CHECK (
        (price_scope = 'BRANCH' AND branch_id IS NOT NULL)
        OR
        (price_scope = 'NATIONAL_ONLINE'
            AND branch_id IS NULL
            AND retailer_code IS NOT NULL
            AND retailer_name IS NOT NULL)
    );

CREATE INDEX idx_price_observations_online_retailer
    ON price_observations(retailer_code, product_id, collected_at DESC)
    WHERE price_scope = 'NATIONAL_ONLINE';
