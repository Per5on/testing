-- Run after loading CSV rows into staging and reviewing every source.
INSERT INTO store_branches(
 retailer_code,retailer_name,branch_name,city,settlement,locality_type,municipality,region,
 postal_code,address,latitude,longitude,source_type,source_url,source_verified_at,active
)
SELECT s.retailer_code,s.retailer_name,s.branch_name,s.city,s.settlement,'OTHER',s.municipality,s.region,
 s.postal_code,s.address,s.latitude,s.longitude,s.source_type,s.source_url,s.source_verified_at,TRUE
FROM branch_import_staging s
WHERE NOT EXISTS (SELECT 1 FROM store_branches b WHERE LOWER(b.retailer_code)=LOWER(s.retailer_code)
 AND LOWER(b.address)=LOWER(s.address) AND LOWER(b.city)=LOWER(s.city));

INSERT INTO products(category_code,barcode,brand,name,variant,package_quantity,package_unit,normalized_name)
SELECT DISTINCT s.category_code,NULLIF(s.barcode,''),s.brand,s.product_name,s.variant,s.package_quantity,s.package_unit,
 LOWER(TRIM(CONCAT_WS(' ',s.brand,s.product_name,s.variant)))
FROM price_import_staging s
WHERE s.verified=TRUE AND NOT EXISTS (
 SELECT 1 FROM products p WHERE p.category_code=s.category_code
 AND p.normalized_name=LOWER(TRIM(CONCAT_WS(' ',s.brand,s.product_name,s.variant)))
);

INSERT INTO price_observations(product_id,branch_id,price_eur,regular_price_eur,currency,source,source_reference,
 verified,valid_from,valid_until,collected_at,created_at,price_note,price_scope,retailer_code,retailer_name)
SELECT p.id,b.id,s.price_eur,s.regular_price_eur,'EUR','retailer_web',s.source_url,TRUE,
 s.collected_at,s.valid_until,s.collected_at,NOW(),s.price_note,s.price_scope,s.retailer_code,s.retailer_name
FROM price_import_staging s
JOIN products p ON p.category_code=s.category_code
 AND p.normalized_name=LOWER(TRIM(CONCAT_WS(' ',s.brand,s.product_name,s.variant)))
LEFT JOIN store_branches b ON s.price_scope='BRANCH' AND LOWER(b.retailer_code)=LOWER(s.retailer_code)
 AND LOWER(b.city)=LOWER(s.city) AND LOWER(b.address)=LOWER(s.branch_address)
WHERE s.verified=TRUE AND (s.price_scope='NATIONAL_ONLINE' OR b.id IS NOT NULL)
 AND (s.valid_until IS NULL OR s.valid_until>NOW())
 AND NOT EXISTS (SELECT 1 FROM price_observations po WHERE po.product_id=p.id
 AND po.source_reference=s.source_url AND po.retailer_code=s.retailer_code
 AND po.price_eur=s.price_eur AND po.collected_at=s.collected_at);
