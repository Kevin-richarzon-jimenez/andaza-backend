-- Esquema inicial de Andanza. Las tablas van en inglés y en plural; las columnas en snake_case.
-- Los ids son uuid; el dinero, numeric(12,2); las fechas, timestamptz.

CREATE TABLE users (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name      varchar(40)  NOT NULL,
    last_name       varchar(40)  NOT NULL,
    email           varchar(255) NOT NULL,
    password_hash   varchar(255) NOT NULL,
    role            varchar(20)  NOT NULL DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER', 'ADMIN')),
    account_status  varchar(20)  NOT NULL DEFAULT 'ACTIVE'   CHECK (account_status IN ('ACTIVE', 'BLOCKED')),
    created_at      timestamptz  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX users_email_key ON users (lower(email));

CREATE TABLE addresses (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         uuid         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    label           varchar(40)  NOT NULL,
    recipient_name  varchar(80)  NOT NULL,
    street          varchar(150) NOT NULL,
    city            varchar(60)  NOT NULL,
    department      varchar(60)  NOT NULL,
    phone           varchar(20)  NOT NULL,
    postal_code     varchar(6),
    is_default      boolean      NOT NULL DEFAULT false,
    created_at      timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX addresses_user_id_idx ON addresses (user_id);
-- Una sola dirección predeterminada por usuario.
CREATE UNIQUE INDEX addresses_one_default_per_user_key ON addresses (user_id) WHERE is_default;

CREATE TABLE categories (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    name            varchar(40)  NOT NULL,
    description     varchar(200) NOT NULL,
    created_at      timestamptz  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX categories_name_key ON categories (lower(name));

CREATE TABLE products (
    id              uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    name            varchar(100)  NOT NULL,
    brand           varchar(60)   NOT NULL,
    description     varchar(1000) NOT NULL,
    category_id     uuid          NOT NULL REFERENCES categories (id),
    price           numeric(12,2) NOT NULL CHECK (price > 0),
    created_at      timestamptz   NOT NULL DEFAULT now()
);
CREATE INDEX products_category_id_idx ON products (category_id);

-- Una variante es una combinación concreta de producto + color + talla, y es la unidad de stock.
CREATE TABLE product_variants (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      uuid         NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    color           varchar(40)  NOT NULL,
    size            varchar(10)  NOT NULL,
    stock           integer      NOT NULL DEFAULT 0 CHECK (stock >= 0)
);
CREATE UNIQUE INDEX product_variants_product_color_size_key ON product_variants (product_id, lower(color), size);

CREATE TABLE favorites (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         uuid         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    product_id      uuid         NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    UNIQUE (user_id, product_id)
);

CREATE TABLE comments (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         uuid         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    product_id      uuid         NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    rating          integer      NOT NULL CHECK (rating BETWEEN 1 AND 5),
    text            varchar(500) NOT NULL CHECK (char_length(text) >= 5),
    status          varchar(20)  NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at      timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX comments_product_id_idx ON comments (product_id);
CREATE INDEX comments_user_id_idx ON comments (user_id);

CREATE TABLE contact_messages (
    id              uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    name            varchar(100)  NOT NULL,
    email           varchar(255)  NOT NULL,
    subject         varchar(120)  NOT NULL,
    message         varchar(1000) NOT NULL,
    created_at      timestamptz   NOT NULL DEFAULT now()
);

CREATE TABLE newsletter_subscriptions (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    email           varchar(255) NOT NULL,
    created_at      timestamptz  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX newsletter_subscriptions_email_key ON newsletter_subscriptions (lower(email));

-- Supabase expone el esquema public por su API REST. Con RLS activado y sin políticas, esa API
-- no puede leer ni escribir nada; el backend se conecta como rol postgres y no se ve afectado.
ALTER TABLE users                    ENABLE ROW LEVEL SECURITY;
ALTER TABLE addresses                ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories               ENABLE ROW LEVEL SECURITY;
ALTER TABLE products                 ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_variants         ENABLE ROW LEVEL SECURITY;
ALTER TABLE favorites                ENABLE ROW LEVEL SECURITY;
ALTER TABLE comments                 ENABLE ROW LEVEL SECURITY;
ALTER TABLE contact_messages         ENABLE ROW LEVEL SECURITY;
ALTER TABLE newsletter_subscriptions ENABLE ROW LEVEL SECURITY;
