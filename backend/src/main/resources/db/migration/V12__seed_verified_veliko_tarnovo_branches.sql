-- Initial verified physical branch directory for the beta build.
-- Addresses are transcribed from the retailers' own store pages, not inferred.
INSERT INTO store_branches(
 retailer_code,retailer_name,branch_name,city,settlement,locality_type,municipality,region,
 postal_code,address,latitude,longitude,source_type,source_url,source_verified_at,active
)
VALUES
 ('kaufland','Kaufland','Kaufland Велико Търново','Велико Търново','Велико Търново','CITY','Велико Търново','Велико Търново', '5000','ул. Краков 2Б',NULL,NULL,'OFFICIAL_WEBSITE','https://www.kaufland.bg/moyat-kaufland/uslugi/filiali/veliko-trnovo-8600.html',NOW(),TRUE),
 ('technopolis','Technopolis','Технополис Велико Търново','Велико Търново','Велико Търново','CITY','Велико Търново','Велико Търново',NULL,'ул. Магистрална 4',43.071944,25.638972,'OFFICIAL_WEBSITE','https://www.technopolis.bg/bg/stores/1308',NOW(),TRUE),
 ('jysk','JYSK','JYSK Велико Търново','Велико Търново','Велико Търново','CITY','Велико Търново','Велико Търново','5002','ул. Магистрална 17',NULL,NULL,'OFFICIAL_WEBSITE','https://jysk.bg/magazini/veliko-trnovo/ul-magistralna',NOW(),TRUE),
 ('ikea','IKEA','IKEA Велико Търново','Велико Търново','Велико Търново','CITY','Велико Търново','Велико Търново',NULL,'IKEA Mall Veliko Tarnovo, ул. Оборище 18, ж.к. Колю Фичето',NULL,NULL,'OFFICIAL_WEBSITE','https://www.ikea.bg/stores/veliko-tarnovo/',NOW(),TRUE)
ON CONFLICT DO NOTHING;
