CREATE TEMP TABLE bg_prices_demo_samples (
    category_code VARCHAR(40) NOT NULL,
    brand VARCHAR(160),
    name VARCHAR(240) NOT NULL,
    variant VARCHAR(240),
    package_quantity NUMERIC(12,3),
    package_unit VARCHAR(32),
    retailer_code VARCHAR(80) NOT NULL,
    retailer_name VARCHAR(160) NOT NULL,
    price_eur NUMERIC(12,2) NOT NULL,
    regular_price_eur NUMERIC(12,2),
    source_reference VARCHAR(500) NOT NULL,
    valid_from TIMESTAMPTZ,
    valid_until TIMESTAMPTZ,
    price_note VARCHAR(240)
) ON COMMIT PRESERVE ROWS;

INSERT INTO bg_prices_demo_samples VALUES
    ('technology', 'Acer', 'Aspire Go 15', 'AG15-42P-R5MP', NULL, NULL, 'ardes', 'Ardes', 679.99, 738.99, 'https://ardes.bg/laptopi/laptopi', '2026-11-12 00:00:00+02', '2026-11-13 00:00:00+02', 'Национална онлайн цена от официалната страница на Ardes, проверена на 12.11.2026; наличността във физически магазин не е потвърдена.'),
    ('home', 'IKEA', 'LACK помощна масичка', '35x35 cm · 305.147.91', NULL, NULL, 'ikea', 'IKEA', 7.66, NULL, 'https://www.ikea.bg/products/tables/coffee-side-tables/side-tables/', '2026-11-12 00:00:00+02', '2026-11-19 00:00:00+02', 'Национална онлайн цена от официалния сайт на IKEA България, проверена на 12.11.2026; наличността във физически магазин не е потвърдена.'),
    ('home', 'IKEA', 'BURVIK помощна масичка', NULL, NULL, NULL, 'ikea', 'IKEA', 30.63, NULL, 'https://www.ikea.bg/products/tables/coffee-side-tables/side-tables/', '2026-11-12 00:00:00+02', '2026-11-19 00:00:00+02', 'Национална онлайн цена от официалния сайт на IKEA България, проверена на 12.11.2026; наличността във физически магазин не е потвърдена.'),
    ('home', 'IKEA', 'LACK помощна маса', '55x55 cm · black-brown · 801.042.68', NULL, NULL, 'ikea', 'IKEA', 11.71, NULL, 'https://www.ikea.bg/products/tables/coffee-side-tables/side-tables/', '2026-11-12 00:00:00+02', '2026-11-19 00:00:00+02', 'Национална онлайн цена от официалния сайт на IKEA България, проверена на 12.11.2026; наличността във физически магазин не е потвърдена.'),
    ('home', 'IKEA', 'RIAN помощна масичка', '50x30 cm · 603.434.20', NULL, NULL, 'ikea', 'IKEA', 35.74, NULL, 'https://www.ikea.bg/products/tables/coffee-side-tables/side-tables/', '2026-11-12 00:00:00+02', '2026-11-19 00:00:00+02', 'Национална онлайн цена от официалния сайт на IKEA България, проверена на 12.11.2026; наличността във физически магазин не е потвърдена.'),
    ('home', 'Einhell', 'CC-BC 10 E battery charger', NULL, NULL, NULL, 'praktiker', 'Praktiker', 35.99, 44.99, 'https://praktiker.bg/en/PredefinedProductList/10-11-2026-30-11-2026/c/weekly-offers?currentPage=1', '2026-11-10 00:00:00+02', '2026-12-01 00:00:00+02', 'Black Friday оферта 10–30.11.2026 от официалния сайт на Praktiker; наличността във физически магазин не е потвърдена.'),
    ('home', NULL, 'EKO MILANO kitchen 250 cm', '250 cm', NULL, NULL, 'praktiker', 'Praktiker', 269.99, 349.99, 'https://praktiker.bg/en/PredefinedProductList/10-11-2026-30-11-2026/c/weekly-offers?currentPage=1', '2026-11-10 00:00:00+02', '2026-12-01 00:00:00+02', 'Black Friday оферта 10–30.11.2026 от официалния сайт на Praktiker; наличността във физически магазин не е потвърдена.'),
    ('home', 'LEKO', 'Alkyd satin paint cherry', '2.5 L', 2.5, 'L', 'praktiker', 'Praktiker', 13.99, 19.99, 'https://praktiker.bg/en/PredefinedProductList/10-11-2026-30-11-2026/c/weekly-offers?currentPage=1', '2026-11-10 00:00:00+02', '2026-12-01 00:00:00+02', 'Black Friday оферта 10–30.11.2026 от официалния сайт на Praktiker; наличността във физически магазин не е потвърдена.'),
    ('home', 'Kapitan', 'Quattro towel rack', '50 cm', 50, 'cm', 'praktiker', 'Praktiker', 22.99, 25.59, 'https://praktiker.bg/en/PredefinedProductList/10-11-2026-30-11-2026/c/weekly-offers?currentPage=1', '2026-11-10 00:00:00+02', '2026-12-01 00:00:00+02', 'Black Friday оферта 10–30.11.2026 от официалния сайт на Praktiker; наличността във физически магазин не е потвърдена.');

INSERT INTO products(category_code, brand, name, variant, package_quantity, package_unit, normalized_name)
SELECT DISTINCT s.category_code, s.brand, s.name, s.variant, s.package_quantity, s.package_unit,
       LOWER(TRIM(CONCAT_WS(' ', s.brand, s.name, s.variant)))
FROM bg_prices_demo_samples s
WHERE NOT EXISTS (
    SELECT 1 FROM products p
    WHERE p.category_code = s.category_code
      AND p.normalized_name = LOWER(TRIM(CONCAT_WS(' ', s.brand, s.name, s.variant)))
);

INSERT INTO price_observations(
    product_id, branch_id, price_eur, regular_price_eur, currency, source, source_reference,
    verified, valid_from, valid_until, collected_at, created_at, price_note,
    price_scope, retailer_code, retailer_name
)
SELECT p.id, NULL, s.price_eur, s.regular_price_eur, 'EUR', 'retailer_web', s.source_reference,
       TRUE, s.valid_from, s.valid_until, NOW(), NOW(), s.price_note,
       'NATIONAL_ONLINE', s.retailer_code, s.retailer_name
FROM bg_prices_demo_samples s
JOIN products p
  ON p.category_code = s.category_code
 AND p.normalized_name = LOWER(TRIM(CONCAT_WS(' ', s.brand, s.name, s.variant)))
WHERE NOT EXISTS (
    SELECT 1 FROM price_observations po
    WHERE po.product_id = p.id
      AND po.price_scope = 'NATIONAL_ONLINE'
      AND po.retailer_code = s.retailer_code
      AND po.source_reference = s.source_reference
      AND po.price_eur = s.price_eur
      AND po.valid_until IS NOT DISTINCT FROM s.valid_until
);

SELECT 'products' AS entity, COUNT(*) AS total FROM products
UNION ALL SELECT 'national_online_prices', COUNT(*) FROM price_observations WHERE price_scope = 'NATIONAL_ONLINE';

DROP TABLE bg_prices_demo_samples;