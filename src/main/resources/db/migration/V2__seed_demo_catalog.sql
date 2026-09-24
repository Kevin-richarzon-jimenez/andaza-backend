-- Catálogo de ejemplo: las categorías base y cinco productos con sus variantes (color + talla + stock).

INSERT INTO categories (name, description) VALUES
    ('Deportivo', 'Calzado para entrenar y para el día a día activo'),
    ('Casual',    'Calzado cómodo para todos los días'),
    ('Formal',    'Calzado para ocasiones formales y de oficina'),
    ('Botas',     'Botas y botines'),
    ('Sandalias', 'Calzado abierto para clima cálido');

INSERT INTO products (name, brand, description, category_id, price)
SELECT p.name, 'Andanza', p.description, c.id, p.price
FROM (VALUES
    ('Runner Air',     'Tenis ligeros con suela amortiguada para correr y entrenar.', 'Deportivo', 289900),
    ('Urban Loafer',   'Mocasín de cuero suave para el uso diario.',                  'Casual',    199900),
    ('Oxford Classic', 'Zapato Oxford de cuero para ocasiones formales.',             'Formal',    259900),
    ('Trail Boot',     'Bota resistente con suela de agarre para caminata.',          'Botas',     329900),
    ('Summer Slide',   'Sandalia liviana con plantilla ergonómica.',                  'Sandalias', 129900)
) AS p (name, description, category, price)
JOIN categories c ON c.name = p.category;

INSERT INTO product_variants (product_id, color, size, stock)
SELECT pr.id, v.color, v.size, 10
FROM (VALUES
    ('Runner Air',     'Negro',  '38'), ('Runner Air',     'Negro',  '40'), ('Runner Air',     'Negro',  '42'),
    ('Runner Air',     'Blanco', '38'), ('Runner Air',     'Blanco', '40'), ('Runner Air',     'Blanco', '42'),
    ('Urban Loafer',   'Café',   '39'), ('Urban Loafer',   'Café',   '41'), ('Urban Loafer',   'Café',   '43'),
    ('Urban Loafer',   'Negro',  '39'), ('Urban Loafer',   'Negro',  '41'), ('Urban Loafer',   'Negro',  '43'),
    ('Oxford Classic', 'Negro',  '40'), ('Oxford Classic', 'Negro',  '42'), ('Oxford Classic', 'Negro',  '44'),
    ('Trail Boot',     'Café',   '40'), ('Trail Boot',     'Café',   '41'), ('Trail Boot',     'Café',   '42'),
    ('Trail Boot',     'Verde',  '40'), ('Trail Boot',     'Verde',  '41'), ('Trail Boot',     'Verde',  '42'),
    ('Summer Slide',   'Azul',   '38'), ('Summer Slide',   'Azul',   '40'),
    ('Summer Slide',   'Blanco', '38'), ('Summer Slide',   'Blanco', '40')
) AS v (product, color, size)
JOIN products pr ON pr.name = v.product;
