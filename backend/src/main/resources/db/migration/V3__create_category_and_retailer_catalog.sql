CREATE TABLE product_categories (
    code VARCHAR(40) PRIMARY KEY,
    name_bg VARCHAR(120) NOT NULL,
    name_en VARCHAR(120) NOT NULL,
    description_bg VARCHAR(300) NOT NULL,
    description_en VARCHAR(300) NOT NULL,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE catalog_retailers (
    code VARCHAR(64) PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    category_code VARCHAR(40) NOT NULL REFERENCES product_categories(code),
    website_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO product_categories(code, name_bg, name_en, description_bg, description_en, sort_order) VALUES
('groceries', 'Хранителни стоки', 'Groceries', 'Ежедневни хранителни и домакински покупки', 'Everyday grocery and household shopping', 1),
('technology', 'Техника', 'Technology', 'Електроника, уреди и компютърна техника', 'Electronics, appliances and computing', 2),
('home', 'Дом и обзавеждане', 'Furniture & home', 'Мебели, ремонт и стоки за дома', 'Furniture, DIY and home products', 3),
('fuel', 'Бензиностанции', 'Fuel stations', 'Цени на горива по вид и местоположение', 'Fuel prices by type and location', 4);

INSERT INTO catalog_retailers(code, name, category_code, website_url) VALUES
('kaufland', 'Kaufland', 'groceries', 'https://www.kaufland.bg/'),
('lidl', 'Lidl', 'groceries', 'https://www.lidl.bg/'),
('billa', 'BILLA', 'groceries', 'https://www.billa.bg/'),
('fantastico', 'Fantastico', 'groceries', 'https://www.fantastico.bg/'),
('t_market', 'T MARKET', 'groceries', 'https://tmarket.bg/'),
('metro', 'METRO', 'groceries', 'https://www.metro.bg/'),
('moyat_magazin', 'Моят магазин', 'groceries', NULL),
('cba', 'CBA', 'groceries', 'https://www.cba.bg/'),
('promarket', 'ProMarket', 'groceries', 'https://promarket.bg/'),
('hit_max', 'HIT Max', 'groceries', NULL),
('burlex', 'Burlex', 'groceries', NULL),
('klasiko', 'Klasiko', 'groceries', NULL),
('abc_market', 'ABC Market', 'groceries', NULL),
('mini_mart', 'Mini Mart', 'groceries', 'https://minimart.bg/'),
('technopolis', 'Technopolis', 'technology', 'https://www.technopolis.bg/'),
('technomarket', 'Technomarket', 'technology', 'https://www.technomarket.bg/'),
('zora', 'Зора', 'technology', 'https://zora.bg/'),
('ardes', 'Ardes', 'technology', 'https://ardes.bg/'),
('plesio', 'Plesio', 'technology', 'https://plesio.bg/'),
('plasico', 'Plasico', 'technology', 'https://plasico.bg/'),
('vali', 'Вали', 'technology', 'https://www.vali.bg/'),
('jar_computers', 'Jar Computers', 'technology', 'https://www.jarcomputers.com/'),
('homemax', 'HomeMax', 'home', 'https://www.home-max.bg/'),
('bauhaus', 'Bauhaus', 'home', 'https://www.bauhaus.bg/'),
('ikea', 'IKEA', 'home', 'https://www.ikea.bg/'),
('jysk', 'JYSK', 'home', 'https://jysk.bg/'),
('mr_bricolage', 'Mr. Bricolage', 'home', 'https://mr-bricolage.bg/'),
('praktiker', 'Praktiker', 'home', 'https://praktiker.bg/'),
('praktis', 'Практис', 'home', 'https://praktis.bg/'),
('videnov', 'Мебели Виденов', 'home', 'https://videnov.bg/'),
('mondo', 'Mondo', 'home', NULL);

CREATE INDEX idx_catalog_retailers_category ON catalog_retailers(category_code, active);
