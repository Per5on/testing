-- Beta data foundation: source-tracked imports. No invented physical addresses or current prices.
CREATE TABLE IF NOT EXISTS data_sources (
 code VARCHAR(80) PRIMARY KEY, display_name VARCHAR(160) NOT NULL,
 source_type VARCHAR(32) NOT NULL CHECK (source_type IN ('OFFICIAL_WEBSITE','OFFICIAL_APP','PUBLIC_DATASET','MANUAL_REVIEW')),
 base_url VARCHAR(1000) NOT NULL, update_frequency_hours INTEGER NOT NULL DEFAULT 24 CHECK (update_frequency_hours > 0),
 active BOOLEAN NOT NULL DEFAULT TRUE, last_checked_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS data_import_batches (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), source_code VARCHAR(80) REFERENCES data_sources(code),
 import_type VARCHAR(32) NOT NULL CHECK (import_type IN ('BRANCHES','ONLINE_PRICES','FUEL_PRICES','PRODUCTS','SETTLEMENTS')),
 source_reference VARCHAR(1000) NOT NULL, imported_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 imported_by VARCHAR(120), row_count INTEGER NOT NULL DEFAULT 0 CHECK (row_count >= 0),
 status VARCHAR(20) NOT NULL DEFAULT 'IMPORTED' CHECK (status IN ('IMPORTED','REVIEW_REQUIRED','REJECTED')), notes VARCHAR(1000)
);
CREATE TABLE IF NOT EXISTS official_settlements (
 ekatte VARCHAR(5) PRIMARY KEY, name_bg VARCHAR(180) NOT NULL, name_en VARCHAR(180),
 locality_type VARCHAR(24) NOT NULL CHECK (locality_type IN ('CITY','TOWN','VILLAGE','OTHER')),
 municipality VARCHAR(160), region VARCHAR(160), source_url VARCHAR(1000) NOT NULL,
 source_checked_at TIMESTAMPTZ NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS idx_official_settlements_name_bg ON official_settlements(LOWER(name_bg));
CREATE INDEX IF NOT EXISTS idx_official_settlements_name_en ON official_settlements(LOWER(name_en));
CREATE INDEX IF NOT EXISTS idx_official_settlements_municipality ON official_settlements(LOWER(municipality));
CREATE TABLE IF NOT EXISTS fuel_price_observations (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), branch_id UUID NOT NULL REFERENCES store_branches(id),
 fuel_type VARCHAR(24) NOT NULL CHECK (fuel_type IN ('GASOLINE_95','GASOLINE_98','DIESEL','LPG','CNG','ELECTRIC')),
 price_eur NUMERIC(12,3) NOT NULL CHECK (price_eur > 0), currency CHAR(3) NOT NULL DEFAULT 'EUR' CHECK (currency = 'EUR'),
 source_code VARCHAR(80) REFERENCES data_sources(code), source_reference VARCHAR(1000) NOT NULL,
 verified BOOLEAN NOT NULL DEFAULT FALSE, collected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), valid_until TIMESTAMPTZ,
 import_batch_id UUID REFERENCES data_import_batches(id), CHECK (valid_until IS NULL OR valid_until > collected_at)
);
CREATE INDEX IF NOT EXISTS idx_fuel_price_branch_type_time ON fuel_price_observations(branch_id, fuel_type, collected_at DESC);
CREATE INDEX IF NOT EXISTS idx_fuel_price_verified_latest ON fuel_price_observations(verified, collected_at DESC);
CREATE TABLE IF NOT EXISTS branch_import_staging (
 import_row_id BIGSERIAL PRIMARY KEY, import_batch_id UUID REFERENCES data_import_batches(id),
 retailer_code VARCHAR(64) NOT NULL, retailer_name VARCHAR(160) NOT NULL, branch_name VARCHAR(160),
 city VARCHAR(120) NOT NULL, settlement VARCHAR(160), municipality VARCHAR(120), region VARCHAR(120),
 postal_code VARCHAR(4), address VARCHAR(300) NOT NULL, latitude DOUBLE PRECISION, longitude DOUBLE PRECISION,
 source_url VARCHAR(1000) NOT NULL, source_type VARCHAR(24) NOT NULL CHECK (source_type IN ('OFFICIAL_WEBSITE','OFFICIAL_APP')),
 source_verified_at TIMESTAMPTZ NOT NULL,
 CHECK (postal_code IS NULL OR postal_code ~ '^[0-9]{4}$'),
 CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90), CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180),
 CHECK ((latitude IS NULL) = (longitude IS NULL))
);
CREATE INDEX IF NOT EXISTS idx_branch_import_staging_batch ON branch_import_staging(import_batch_id);
CREATE TABLE IF NOT EXISTS price_import_staging (
 import_row_id BIGSERIAL PRIMARY KEY, import_batch_id UUID REFERENCES data_import_batches(id),
 category_code VARCHAR(40) NOT NULL REFERENCES product_categories(code), barcode VARCHAR(64), brand VARCHAR(160),
 product_name VARCHAR(240) NOT NULL, variant VARCHAR(240), package_quantity NUMERIC(12,3), package_unit VARCHAR(32),
 retailer_code VARCHAR(80) NOT NULL, retailer_name VARCHAR(160) NOT NULL,
 price_eur NUMERIC(12,2) NOT NULL CHECK (price_eur > 0), regular_price_eur NUMERIC(12,2),
 price_scope VARCHAR(24) NOT NULL CHECK (price_scope IN ('BRANCH','NATIONAL_ONLINE')),
 city VARCHAR(120), settlement VARCHAR(160), postal_code VARCHAR(4), branch_address VARCHAR(300),
 source_url VARCHAR(1000) NOT NULL, collected_at TIMESTAMPTZ NOT NULL, valid_until TIMESTAMPTZ,
 verified BOOLEAN NOT NULL DEFAULT FALSE, price_note VARCHAR(240),
 CHECK (valid_until IS NULL OR valid_until > collected_at),
 CHECK ((price_scope = 'NATIONAL_ONLINE') OR (city IS NOT NULL AND branch_address IS NOT NULL)),
 CHECK (price_scope <> 'NATIONAL_ONLINE' OR (city IS NULL AND branch_address IS NULL))
);
CREATE INDEX IF NOT EXISTS idx_price_import_staging_batch ON price_import_staging(import_batch_id);
INSERT INTO data_sources(code,display_name,source_type,base_url,update_frequency_hours) VALUES
 ('nsi_ekatte','National Statistical Institute — National Register of Settlements','PUBLIC_DATASET','https://www.nsi.bg/nrnm/ekatte/territorial-units/json',720),
 ('fuelo_daily','Fuelo daily fuel prices','PUBLIC_DATASET','https://bg.fuelo.net/prices/date/',24)
ON CONFLICT (code) DO UPDATE SET display_name=EXCLUDED.display_name,base_url=EXCLUDED.base_url,update_frequency_hours=EXCLUDED.update_frequency_hours;
-- Product reference catalogue only; these are not price or availability claims.
INSERT INTO products(category_code,brand,name,variant,normalized_name)
SELECT 'groceries',NULL,x.name,x.variant,LOWER(TRIM(x.name || ' ' || COALESCE(x.variant,'')))
FROM (VALUES
 ('Прясно мляко','3.6%'),('Прясно мляко','1.5%'),('Кисело мляко','3.6%'),('Кисело мляко','2.0%'),
 ('Яйца','Размер M · 10 бр.'),('Яйца','Размер L · 10 бр.'),('Бяло саламурено сирене','Краве'),('Кашкавал','Краве'),
 ('Масло','82% · 200 г'),('Олио','Слънчогледово · 1 л'),('Брашно','Тип 500 · 1 кг'),('Захар','Бяла · 1 кг'),
 ('Ориз','Бял · 1 кг'),('Боб','Зрял · 1 кг'),('Леща','1 кг'),('Хляб','Бял · 650 г'),
 ('Хляб','Пълнозърнест · 500 г'),('Пилешко филе','1 кг'),('Свинска плешка','1 кг'),
 ('Банани','1 кг'),('Ябълки','1 кг'),('Домати','1 кг'),('Краставици','1 кг'),('Картофи','1 кг')
) AS x(name,variant)
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.category_code='groceries' AND p.normalized_name=LOWER(TRIM(x.name || ' ' || COALESCE(x.variant,''))));
