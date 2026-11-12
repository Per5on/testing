WITH samples(category_code, brand, name, variant, package_quantity, package_unit, retailer_code, retailer_name, price_eur, regular_price_eur, source_reference, valid_from, valid_until, price_note) AS (
    VALUES
        ('groceries', 'Deluxe', 'Рустикален хляб с два вида квас', '500 g', 500, 'g', 'lidl', 'Lidl', 1.99, 2.99, 'https://www.lidl.bg/c/aktsiya/a10106189', '2026-11-09 00:00:00+02', '2026-11-16 00:00:00+02', 'Lidl Plus цена; промоция 09–15.11.2026; национална онлайн оферта, наличността по филиал не е потвърдена.'),
        ('groceries', NULL, 'Милинка', '140 g', 140, 'g', 'lidl', 'Lidl', 0.69, 0.99, 'https://www.lidl.bg/c/aktsiya/a10106189', '2026-11-09 00:00:00+02', '2026-11-16 00:00:00+02', 'Промоция 09–15.11.2026; национална онлайн оферта, наличността по филиал не е потвърдена.'),
        ('groceries', NULL, 'Бутерка', '70 g', 70, 'g', 'lidl', 'Lidl', 0.35, 0.49, 'https://www.lidl.bg/c/aktsiya/a10106189', '2026-11-09 00:00:00+02', '2026-11-16 00:00:00+02', 'Lidl Plus цена; промоция 09–15.11.2026; наличността по филиал не е потвърдена.'),
        ('groceries', NULL, 'Филе от сьомгова пъстърва', 'за kg', 1, 'kg', 'lidl', 'Lidl', 11.49, 14.99, 'https://www.lidl.bg/c/aktsiya/a10106189', '2026-11-09 00:00:00+02', '2026-11-16 00:00:00+02', 'Промоция 09–15.11.2026; цена за kg; наличността по филиал не е потвърдена.'),
        ('groceries', NULL, 'Пресен шаран', 'за kg', 1, 'kg', 'lidl', 'Lidl', 5.69, NULL, 'https://www.lidl.bg/c/aktsiya/a10106189', '2026-11-09 00:00:00+02', '2026-11-16 00:00:00+02', 'Промоция 09–15.11.2026; цена за kg; наличността по филиал не е потвърдена.'),
        ('groceries', NULL, 'Прясно пилешко филе', 'за kg', 1, 'kg', 'lidl', 'Lidl', 6.69, 8.99, 'https://www.lidl.bg/c/aktsiya/a10106189', '2026-11-12 00:00:00+02', '2026-11-16 00:00:00+02', 'Промоция 12–15.11.2026; цена за kg; наличността по филиал не е потвърдена.'),
        ('groceries', NULL, 'Хотдог', '108 g', 108, 'g', 'lidl', 'Lidl', 0.69, 0.99, 'https://www.lidl.bg/c/aktsiya/a10106189', '2026-11-12 00:00:00+02', '2026-11-16 00:00:00+02', 'Промоция 12–15.11.2026; наличността по филиал не е потвърдена.'),
        ('groceries', 'Freshona', 'Пасирани домати', '500 g', 500, 'g', 'lidl', 'Lidl', 0.75, 1.12, 'https://www.lidl.bg/c/aktsiya/a10106189', '2026-11-12 00:00:00+02', '2026-11-16 00:00:00+02', 'Промоция 12–15.11.2026; наличността по филиал не е потвърдена.'),
        ('groceries', NULL, 'Европейска салата', '400 g', 400, 'g', 'lidl', 'Lidl', 1.19, 1.59, 'https://www.lidl.bg/c/aktsiya/a10106189', '2026-11-12 00:00:00+02', '2026-11-16 00:00:00+02', 'Промоция 12–15.11.2026; наличността по филиал не е потвърдена.'),
        ('groceries', NULL, 'Пастет', '150 g', 150, 'g', 'lidl', 'Lidl', 0.79, 1.02, 'https://www.lidl.bg/c/aktsiya/a10106189', '2026-11-12 00:00:00+02', '2026-11-16 00:00:00+02', 'Lidl Plus цена; промоция 12–15.11.2026; наличността по филиал не е потвърдена.'),
        ('technology', 'Lenovo', 'ThinkPad E16 G4', NULL, NULL, NULL, 'ardes', 'Ardes', 1079.99, NULL, 'https://ardes.bg/homepage', '2026-11-12 00:00:00+02', '2026-11-13 00:00:00+02', 'Национална онлайн цена, видяна на официалния сайт на 12.11.2026; клоновата наличност не е потвърдена.'),
        ('technology', 'Kingston', 'NV3 SSD 1TB', NULL, 1, 'TB', 'ardes', 'Ardes', 159.99, NULL, 'https://ardes.bg/homepage', '2026-11-12 00:00:00+02', '2026-11-13 00:00:00+02', 'Национална онлайн цена, видяна на официалния сайт на 12.11.2026; клоновата наличност не е потвърдена.'),
        ('technology', 'Apple', 'USB Type-C 96W', NULL, NULL, NULL, 'ardes', 'Ardes', 71.99, NULL, 'https://ardes.bg/homepage', '2026-11-12 00:00:00+02', '2026-11-13 00:00:00+02', 'Национална онлайн цена, видяна на официалния сайт на 12.11.2026; клоновата наличност не е потвърдена.'),
        ('technology', 'Samsung', 'Galaxy Tab A11 Cellular', NULL, NULL, NULL, 'ardes', 'Ardes', 148.99, NULL, 'https://ardes.bg/homepage', '2026-11-12 00:00:00+02', '2026-11-13 00:00:00+02', 'Национална онлайн цена, видяна на официалния сайт на 12.11.2026; клоновата наличност не е потвърдена.'),
        ('technology', 'ASUS', 'ROG STRIX SCOPE II 96', NULL, NULL, NULL, 'technopolis', 'Technopolis', 125, NULL, 'https://www.technopolis.bg/bg/Kompyutri-i-periferiya/c/P1101', '2026-11-12 00:00:00+02', '2026-11-13 00:00:00+02', 'Цена от официалната онлайн категория, обходена на 12.11.2026; наличността по филиал не е потвърдена.'),
        ('technology', 'Brother', 'DCP-1510E multifunction printer', NULL, NULL, NULL, 'technopolis', 'Technopolis', 129.9, NULL, 'https://www.technopolis.bg/bg/Kompyutri-i-periferiya/c/P1101', '2026-11-12 00:00:00+02', '2026-11-13 00:00:00+02', 'Цена от официалната онлайн категория, обходена на 12.11.2026; наличността по филиал не е потвърдена.'),
        ('technology', 'PocketBook', 'Verse Lite PB619 Midnight Grey', NULL, NULL, NULL, 'technopolis', 'Technopolis', 134.9, NULL, 'https://www.technopolis.bg/bg/Kompyutri-i-periferiya/c/P1101', '2026-11-12 00:00:00+02', '2026-11-13 00:00:00+02', 'Цена от официалната онлайн категория, обходена на 12.11.2026; наличността по филиал не е потвърдена.'),
        ('technology', 'ASUS', 'TUF Gaming VG279Q5A 27-inch monitor', NULL, NULL, NULL, 'technopolis', 'Technopolis', 135, NULL, 'https://www.technopolis.bg/bg/Kompyutri-i-periferiya/c/P1101', '2026-11-12 00:00:00+02', '2026-11-13 00:00:00+02', 'Цена от официалната онлайн категория, обходена на 12.11.2026; наличността по филиал не е потвърдена.'),
        ('home', 'IKEA', 'LACK side table 35x35 cm', '305.147.91', NULL, NULL, 'ikea', 'IKEA', 7.66, NULL, 'https://www.ikea.bg/', '2026-11-12 00:00:00+02', '2026-11-13 00:00:00+02', 'Национална онлайн цена от официалния сайт, проверена на 12.11.2026; магазинната наличност не е потвърдена.'),
        ('home', 'IKEA', 'GAVLE chair', '605.888.46', NULL, NULL, 'ikea', 'IKEA', 30.63, NULL, 'https://www.ikea.bg/', '2026-11-12 00:00:00+02', '2026-11-13 00:00:00+02', 'Национална онлайн цена от официалния сайт, проверена на 12.11.2026; магазинната наличност не е потвърдена.'),
        ('home', 'IKEA', 'GLOSTAD 2-seat sofa', '504.890.12', NULL, NULL, 'ikea', 'IKEA', 152.88, NULL, 'https://www.ikea.bg/', '2026-11-12 00:00:00+02', '2026-11-13 00:00:00+02', 'Национална онлайн цена от официалния сайт, проверена на 12.11.2026; магазинната наличност не е потвърдена.'),
        ('home', 'Einhell', 'CC-BC 10 E battery charger', NULL, NULL, NULL, 'praktiker', 'Praktiker', 35.99, 44.99, 'https://praktiker.bg/en/PredefinedProductList/10-11-2026-30-11-2026/c/weekly-offers?currentPage=1', '2026-11-10 00:00:00+02', '2026-12-01 00:00:00+02', 'Black Friday оферта 10–30.11.2026; наличността по филиал не е потвърдена.'),
        ('home', NULL, 'EKO MILANO kitchen 250 cm', NULL, NULL, NULL, 'praktiker', 'Praktiker', 269.99, 349.99, 'https://praktiker.bg/en/PredefinedProductList/10-11-2026-30-11-2026/c/weekly-offers?currentPage=1', '2026-11-10 00:00:00+02', '2026-12-01 00:00:00+02', 'Black Friday оферта 10–30.11.2026; наличността по филиал не е потвърдена.'),
        ('home', 'LEKO', 'Alkyd satin paint cherry 2.5 L', NULL, 2.5, 'L', 'praktiker', 'Praktiker', 13.99, 19.99, 'https://praktiker.bg/en/PredefinedProductList/10-11-2026-30-11-2026/c/weekly-offers?currentPage=1', '2026-11-10 00:00:00+02', '2026-12-01 00:00:00+02', 'Black Friday оферта 10–30.11.2026; наличността по филиал не е потвърдена.'),
        ('home', 'Kapitan', 'Quattro towel rack 50 cm', NULL, NULL, NULL, 'praktiker', 'Praktiker', 22.99, 25.59, 'https://praktiker.bg/en/PredefinedProductList/10-11-2026-30-11-2026/c/weekly-offers?currentPage=1', '2026-11-10 00:00:00+02', '2026-12-01 00:00:00+02', 'Black Friday оферта 10–30.11.2026; наличността по филиал не е потвърдена.')
), inserted_products AS (
    INSERT INTO products(category_code, brand, name, variant, package_quantity, package_unit, normalized_name)
    SELECT DISTINCT s.category_code, s.brand, s.name, s.variant, s.package_quantity, s.package_unit,
           LOWER(TRIM(CONCAT_WS(' ', s.brand, s.name, s.variant)))
    FROM samples s
    WHERE NOT EXISTS (
        SELECT 1 FROM products p
        WHERE p.category_code = s.category_code
          AND p.normalized_name = LOWER(TRIM(CONCAT_WS(' ', s.brand, s.name, s.variant)))
    )
    RETURNING id
)
INSERT INTO price_observations(
    product_id, branch_id, price_eur, regular_price_eur, currency, source, source_reference,
    verified, valid_from, valid_until, collected_at, created_at, price_note,
    price_scope, retailer_code, retailer_name
)
SELECT p.id, NULL, s.price_eur, s.regular_price_eur, 'EUR', 'retailer_web', s.source_reference,
       TRUE, s.valid_from::timestamptz, s.valid_until::timestamptz, NOW(), NOW(), s.price_note,
       'NATIONAL_ONLINE', s.retailer_code, s.retailer_name
FROM samples s
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
      AND po.valid_until IS NOT DISTINCT FROM s.valid_until::timestamptz
);

SELECT 'products' AS entity, COUNT(*) AS total FROM products
UNION ALL SELECT 'national_online_prices', COUNT(*) FROM price_observations WHERE price_scope = 'NATIONAL_ONLINE';