DO $EF$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = 'catalog') THEN
        CREATE SCHEMA catalog;
    END IF;
END $EF$;
CREATE TABLE IF NOT EXISTS catalog."__EFMigrationsHistory" (
    "MigrationId" character varying(150) NOT NULL,
    "ProductVersion" character varying(32) NOT NULL,
    CONSTRAINT "PK___EFMigrationsHistory" PRIMARY KEY ("MigrationId")
);

START TRANSACTION;
DO $EF$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = 'catalog') THEN
        CREATE SCHEMA catalog;
    END IF;
END $EF$;

CREATE TABLE catalog.categories (
    "Id" uuid NOT NULL,
    "Name" character varying(100) NOT NULL,
    "Description" character varying(500),
    "Slug" character varying(150) NOT NULL,
    "ParentCategoryId" uuid,
    "DisplayOrder" integer NOT NULL,
    "IsActive" boolean NOT NULL DEFAULT TRUE,
    "ImageKey" character varying(500),
    CONSTRAINT "PK_categories" PRIMARY KEY ("Id"),
    CONSTRAINT "FK_categories_categories_ParentCategoryId" FOREIGN KEY ("ParentCategoryId") REFERENCES catalog.categories ("Id") ON DELETE RESTRICT
);

CREATE TABLE catalog.products (
    "Id" uuid NOT NULL,
    "Name" character varying(200) NOT NULL,
    "Description" character varying(5000) NOT NULL,
    "Sku" character varying(50) NOT NULL,
    price numeric(18,2),
    currency character varying(3),
    "WeightKg" numeric NOT NULL,
    "CategoryId" uuid NOT NULL,
    "Status" text NOT NULL,
    "SeoTitle" character varying(70),
    "SeoDescription" character varying(160),
    "Slug" character varying(250),
    search_vector tsvector,
    CONSTRAINT "PK_products" PRIMARY KEY ("Id")
);

CREATE TABLE catalog.product_attributes (
    "Key" character varying(100) NOT NULL,
    "ProductId" uuid NOT NULL,
    "Value" character varying(500) NOT NULL,
    "DisplayName" character varying(100),
    CONSTRAINT "PK_product_attributes" PRIMARY KEY ("ProductId", "Key"),
    CONSTRAINT "FK_product_attributes_products_ProductId" FOREIGN KEY ("ProductId") REFERENCES catalog.products ("Id") ON DELETE CASCADE
);

CREATE TABLE catalog.product_images (
    "Id" uuid NOT NULL,
    "ImageKey" character varying(500) NOT NULL,
    "DisplayOrder" integer NOT NULL,
    "IsPrimary" boolean NOT NULL,
    "ProductId" uuid NOT NULL,
    CONSTRAINT "PK_product_images" PRIMARY KEY ("Id"),
    CONSTRAINT "FK_product_images_products_ProductId" FOREIGN KEY ("ProductId") REFERENCES catalog.products ("Id") ON DELETE CASCADE
);

CREATE INDEX "IX_categories_ParentCategoryId" ON catalog.categories ("ParentCategoryId");

CREATE UNIQUE INDEX "IX_categories_Slug" ON catalog.categories ("Slug");

CREATE INDEX "IX_product_images_ProductId" ON catalog.product_images ("ProductId");

CREATE INDEX "IX_products_CategoryId" ON catalog.products ("CategoryId");

CREATE INDEX "IX_products_search_vector" ON catalog.products USING GIN (search_vector);

CREATE UNIQUE INDEX "IX_products_Sku" ON catalog.products ("Sku");

CREATE INDEX "IX_products_Slug" ON catalog.products ("Slug");

INSERT INTO catalog."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20260903182440_InitialCreate', '10.0.12');

COMMIT;

START TRANSACTION;
UPDATE catalog.products SET price = 0.0 WHERE price IS NULL;
ALTER TABLE catalog.products ALTER COLUMN price SET NOT NULL;
ALTER TABLE catalog.products ALTER COLUMN price SET DEFAULT 0.0;

UPDATE catalog.products SET currency = '' WHERE currency IS NULL;
ALTER TABLE catalog.products ALTER COLUMN currency SET NOT NULL;
ALTER TABLE catalog.products ALTER COLUMN currency SET DEFAULT '';

UPDATE catalog.categories SET "Description" = '' WHERE "Description" IS NULL;
ALTER TABLE catalog.categories ALTER COLUMN "Description" SET NOT NULL;
ALTER TABLE catalog.categories ALTER COLUMN "Description" SET DEFAULT '';

INSERT INTO catalog."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20261002222958_RequirePriceCurrencyAndDescription', '10.0.12');

COMMIT;

START TRANSACTION;

CREATE TABLE IF NOT EXISTS catalog.wolverine_incoming_envelopes (
    id uuid NOT NULL,
    status character varying NOT NULL,
    owner_id integer NOT NULL,
    execution_time timestamp with time zone,
    attempts integer DEFAULT 0,
    body bytea NOT NULL,
    message_type character varying NOT NULL,
    received_at character varying NOT NULL,
    keep_until timestamp with time zone
);
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid
        JOIN pg_namespace n ON n.oid = t.relnamespace
        WHERE c.conname = 'pkey_wolverine_incoming_envelopes_id_received_at'
          AND n.nspname = 'catalog') THEN
        ALTER TABLE catalog.wolverine_incoming_envelopes
            ADD CONSTRAINT pkey_wolverine_incoming_envelopes_id_received_at PRIMARY KEY (id, received_at);
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_keep_until ON catalog.wolverine_incoming_envelopes USING btree (keep_until) WHERE ((status)::text = 'Handled'::text);
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_owner ON catalog.wolverine_incoming_envelopes USING btree (owner_id) WHERE (owner_id <> 0);
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_recover ON catalog.wolverine_incoming_envelopes USING btree (received_at) WHERE (((status)::text = 'Incoming'::text) AND (owner_id = 0));
CREATE TABLE IF NOT EXISTS catalog.wolverine_outgoing_envelopes (
    id uuid NOT NULL,
    owner_id integer NOT NULL,
    destination character varying NOT NULL,
    deliver_by timestamp with time zone,
    body bytea NOT NULL,
    attempts integer DEFAULT 0,
    message_type character varying NOT NULL
);
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid
        JOIN pg_namespace n ON n.oid = t.relnamespace
        WHERE c.conname = 'pkey_wolverine_outgoing_envelopes_id'
          AND n.nspname = 'catalog') THEN
        ALTER TABLE catalog.wolverine_outgoing_envelopes
            ADD CONSTRAINT pkey_wolverine_outgoing_envelopes_id PRIMARY KEY (id);
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_wolverine_outgoing_envelopes_owner ON catalog.wolverine_outgoing_envelopes USING btree (owner_id) WHERE (owner_id <> 0);
CREATE INDEX IF NOT EXISTS idx_wolverine_outgoing_envelopes_recover ON catalog.wolverine_outgoing_envelopes USING btree (destination) WHERE (owner_id = 0);


INSERT INTO catalog."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20261002232119_EnsureWolverineEnvelopeTables', '10.0.12');

COMMIT;

