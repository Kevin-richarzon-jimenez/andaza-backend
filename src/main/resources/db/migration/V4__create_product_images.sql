-- Imágenes de un producto. Cada imagen pertenece a un color del producto (las tallas de un mismo color
-- comparten fotos). El archivo vive en Supabase Storage: aquí solo se guardan sus direcciones públicas.
CREATE TABLE product_images (
    id             uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id     uuid         NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    color          varchar(40)  NOT NULL,
    sort_order     integer      NOT NULL DEFAULT 0,
    url            varchar(500) NOT NULL,
    thumbnail_url  varchar(500) NOT NULL,
    created_at     timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX product_images_product_id_idx ON product_images (product_id);

ALTER TABLE product_images ENABLE ROW LEVEL SECURITY;
