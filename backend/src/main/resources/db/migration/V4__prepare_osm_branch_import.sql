ALTER TABLE store_branches
    ADD COLUMN source VARCHAR(40) NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN source_id VARCHAR(100),
    ADD COLUMN last_seen_at TIMESTAMPTZ;

CREATE UNIQUE INDEX uq_store_branches_source_id
    ON store_branches(source, source_id);

INSERT INTO catalog_retailers(code, name, category_code, website_url) VALUES
('lukoil', 'LUKOIL', 'fuel', 'https://lukoil.bg/'),
('omv', 'OMV', 'fuel', 'https://www.omv.bg/'),
('shell', 'Shell', 'fuel', 'https://www.shell.bg/'),
('eko', 'EKO', 'fuel', 'https://www.eko.bg/'),
('petrol', 'Petrol', 'fuel', 'https://www.petrol.bg/'),
('rompetrol', 'Rompetrol', 'fuel', 'https://www.rompetrol.bg/'),
('insa_oil', 'Insa Oil', 'fuel', 'https://insa-oil.com/');

-- OpenStreetMap-derived records retain their source identifiers for safe repeat imports.
-- OSM attribution is required in the app's location/data-source information.
