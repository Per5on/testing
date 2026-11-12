CREATE TABLE inhabited_localities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source VARCHAR(40) NOT NULL,
    source_id VARCHAR(100) NOT NULL,
    name VARCHAR(180) NOT NULL,
    name_bg VARCHAR(180),
    type VARCHAR(24) NOT NULL DEFAULT 'OTHER'
      CHECK (type IN ('CITY', 'TOWN', 'VILLAGE', 'OTHER')),
    municipality VARCHAR(160),
    region VARCHAR(160),
    latitude DOUBLE PRECISION NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude DOUBLE PRECISION NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    on_map BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_inhabited_locality_source UNIQUE (source, source_id)
);

CREATE INDEX idx_inhabited_localities_name ON inhabited_localities (LOWER(name));
CREATE INDEX idx_inhabited_localities_name_bg ON inhabited_localities (LOWER(name_bg));
CREATE INDEX idx_inhabited_localities_type ON inhabited_localities (type);
CREATE INDEX idx_inhabited_localities_on_map ON inhabited_localities (on_map);

-- Import mapped populated-place features here (e.g. OpenStreetMap place=city/town/village/hamlet/isolated_dwelling).
-- No sample or invented settlements are inserted by this migration.
