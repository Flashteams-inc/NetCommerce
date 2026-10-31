DO $EF$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = 'ordering') THEN
        CREATE SCHEMA ordering;
    END IF;
END $EF$;
CREATE TABLE IF NOT EXISTS ordering."__EFMigrationsHistory" (
    "MigrationId" character varying(150) NOT NULL,
    "ProductVersion" character varying(32) NOT NULL,
    CONSTRAINT "PK___EFMigrationsHistory" PRIMARY KEY ("MigrationId")
);

START TRANSACTION;
DO $EF$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = 'ordering') THEN
        CREATE SCHEMA ordering;
    END IF;
END $EF$;

CREATE TABLE ordering.orders (
    id uuid NOT NULL,
    order_number character varying(50) NOT NULL,
    customer_id uuid NOT NULL,
    tenant_id character varying(100) NOT NULL,
    status character varying(50) NOT NULL,
    total_amount numeric(18,2),
    total_currency character varying(3),
    shipping_recipient character varying(200),
    shipping_street character varying(200),
    shipping_city character varying(100),
    shipping_state character varying(100),
    shipping_country character varying(100),
    shipping_postal_code character varying(20),
    shipping_phone character varying(50),
    billing_name character varying(200),
    billing_street character varying(200),
    billing_city character varying(100),
    billing_state character varying(100),
    billing_country character varying(100),
    billing_postal_code character varying(20),
    created_at timestamp with time zone NOT NULL,
    paid_at timestamp with time zone,
    "PaymentTransactionId" text,
    shipped_at timestamp with time zone,
    delivered_at timestamp with time zone,
    cancelled_at timestamp with time zone,
    cancellation_reason character varying(500),
    notes character varying(1000),
    idempotency_key character varying(100),
    "IsShadowOrder" boolean NOT NULL,
    "SourceDiscrepancyTxnId" text,
    "Version" bigint NOT NULL,
    CONSTRAINT "PK_orders" PRIMARY KEY (id)
);

CREATE TABLE ordering.order_items (
    id uuid NOT NULL,
    product_id uuid NOT NULL,
    applied_title character varying(500) NOT NULL,
    applied_price_amount numeric(18,2),
    applied_price_currency character varying(3),
    applied_weight_kg numeric(10,3) NOT NULL,
    quantity integer NOT NULL,
    sku character varying(100),
    base_price numeric(18,2),
    discount_amount numeric(18,2),
    tax_amount numeric(18,2),
    tax_rate numeric(10,4),
    tax_type character varying(50),
    breakdown_currency character varying(3),
    breakdown_quantity integer,
    line_discount_total numeric(18,2),
    line_tax_total numeric(18,2),
    order_id uuid,
    CONSTRAINT "PK_order_items" PRIMARY KEY (id),
    CONSTRAINT "FK_order_items_orders_order_id" FOREIGN KEY (order_id) REFERENCES ordering.orders (id) ON DELETE CASCADE
);
COMMENT ON COLUMN ordering.order_items.base_price IS 'Original price from catalog at order time';
COMMENT ON COLUMN ordering.order_items.discount_amount IS 'Discount per unit (for backward compatibility)';
COMMENT ON COLUMN ordering.order_items.tax_amount IS 'Tax per unit (for backward compatibility)';
COMMENT ON COLUMN ordering.order_items.tax_rate IS 'Tax rate applied (e.g., 0.18 for 18% VAT) - crucial for legal audits';
COMMENT ON COLUMN ordering.order_items.tax_type IS 'Type of tax applied (VAT, SALES_TAX, GST, etc.)';
COMMENT ON COLUMN ordering.order_items.breakdown_quantity IS 'Quantity for this pricing breakdown';
COMMENT ON COLUMN ordering.order_items.line_discount_total IS 'TOTAL discount for line item - PRIMARY source of truth to avoid penny variance';
COMMENT ON COLUMN ordering.order_items.line_tax_total IS 'TOTAL tax for line item - PRIMARY source of truth to avoid penny variance';

CREATE INDEX "IX_order_items_order_id" ON ordering.order_items (order_id);

CREATE INDEX "IX_orders_customer_id" ON ordering.orders (customer_id);

CREATE UNIQUE INDEX "IX_orders_idempotency_key_customer_id" ON ordering.orders (idempotency_key, customer_id) WHERE idempotency_key IS NOT NULL;

CREATE UNIQUE INDEX "IX_orders_order_number" ON ordering.orders (order_number);

CREATE INDEX "IX_Orders_Status_CreatedAt" ON ordering.orders (status, created_at);

CREATE INDEX "IX_Orders_TenantId" ON ordering.orders (tenant_id);

INSERT INTO ordering."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20260203000000_BaselineOrderSchema', '10.0.12');

COMMIT;

START TRANSACTION;
UPDATE ordering.orders SET total_currency = '' WHERE total_currency IS NULL;
ALTER TABLE ordering.orders ALTER COLUMN total_currency SET NOT NULL;
ALTER TABLE ordering.orders ALTER COLUMN total_currency SET DEFAULT '';

UPDATE ordering.orders SET total_amount = 0.0 WHERE total_amount IS NULL;
ALTER TABLE ordering.orders ALTER COLUMN total_amount SET NOT NULL;
ALTER TABLE ordering.orders ALTER COLUMN total_amount SET DEFAULT 0.0;

UPDATE ordering.orders SET shipping_street = '' WHERE shipping_street IS NULL;
ALTER TABLE ordering.orders ALTER COLUMN shipping_street SET NOT NULL;
ALTER TABLE ordering.orders ALTER COLUMN shipping_street SET DEFAULT '';

UPDATE ordering.orders SET shipping_state = '' WHERE shipping_state IS NULL;
ALTER TABLE ordering.orders ALTER COLUMN shipping_state SET NOT NULL;
ALTER TABLE ordering.orders ALTER COLUMN shipping_state SET DEFAULT '';

UPDATE ordering.orders SET shipping_recipient = '' WHERE shipping_recipient IS NULL;
ALTER TABLE ordering.orders ALTER COLUMN shipping_recipient SET NOT NULL;
ALTER TABLE ordering.orders ALTER COLUMN shipping_recipient SET DEFAULT '';

UPDATE ordering.orders SET shipping_postal_code = '' WHERE shipping_postal_code IS NULL;
ALTER TABLE ordering.orders ALTER COLUMN shipping_postal_code SET NOT NULL;
ALTER TABLE ordering.orders ALTER COLUMN shipping_postal_code SET DEFAULT '';

UPDATE ordering.orders SET shipping_phone = '' WHERE shipping_phone IS NULL;
ALTER TABLE ordering.orders ALTER COLUMN shipping_phone SET NOT NULL;
ALTER TABLE ordering.orders ALTER COLUMN shipping_phone SET DEFAULT '';

UPDATE ordering.orders SET shipping_country = '' WHERE shipping_country IS NULL;
ALTER TABLE ordering.orders ALTER COLUMN shipping_country SET NOT NULL;
ALTER TABLE ordering.orders ALTER COLUMN shipping_country SET DEFAULT '';

UPDATE ordering.orders SET shipping_city = '' WHERE shipping_city IS NULL;
ALTER TABLE ordering.orders ALTER COLUMN shipping_city SET NOT NULL;
ALTER TABLE ordering.orders ALTER COLUMN shipping_city SET DEFAULT '';

UPDATE ordering.orders SET idempotency_key = '' WHERE idempotency_key IS NULL;
ALTER TABLE ordering.orders ALTER COLUMN idempotency_key SET NOT NULL;
ALTER TABLE ordering.orders ALTER COLUMN idempotency_key SET DEFAULT '';

UPDATE ordering.order_items SET tax_type = '' WHERE tax_type IS NULL;
ALTER TABLE ordering.order_items ALTER COLUMN tax_type SET NOT NULL;
ALTER TABLE ordering.order_items ALTER COLUMN tax_type SET DEFAULT '';

UPDATE ordering.order_items SET tax_rate = 0.0 WHERE tax_rate IS NULL;
ALTER TABLE ordering.order_items ALTER COLUMN tax_rate SET NOT NULL;
ALTER TABLE ordering.order_items ALTER COLUMN tax_rate SET DEFAULT 0.0;

UPDATE ordering.order_items SET tax_amount = 0.0 WHERE tax_amount IS NULL;
ALTER TABLE ordering.order_items ALTER COLUMN tax_amount SET NOT NULL;
ALTER TABLE ordering.order_items ALTER COLUMN tax_amount SET DEFAULT 0.0;

UPDATE ordering.order_items SET line_tax_total = 0.0 WHERE line_tax_total IS NULL;
ALTER TABLE ordering.order_items ALTER COLUMN line_tax_total SET NOT NULL;
ALTER TABLE ordering.order_items ALTER COLUMN line_tax_total SET DEFAULT 0.0;

UPDATE ordering.order_items SET line_discount_total = 0.0 WHERE line_discount_total IS NULL;
ALTER TABLE ordering.order_items ALTER COLUMN line_discount_total SET NOT NULL;
ALTER TABLE ordering.order_items ALTER COLUMN line_discount_total SET DEFAULT 0.0;

UPDATE ordering.order_items SET discount_amount = 0.0 WHERE discount_amount IS NULL;
ALTER TABLE ordering.order_items ALTER COLUMN discount_amount SET NOT NULL;
ALTER TABLE ordering.order_items ALTER COLUMN discount_amount SET DEFAULT 0.0;

UPDATE ordering.order_items SET breakdown_quantity = 0 WHERE breakdown_quantity IS NULL;
ALTER TABLE ordering.order_items ALTER COLUMN breakdown_quantity SET NOT NULL;
ALTER TABLE ordering.order_items ALTER COLUMN breakdown_quantity SET DEFAULT 0;

UPDATE ordering.order_items SET breakdown_currency = '' WHERE breakdown_currency IS NULL;
ALTER TABLE ordering.order_items ALTER COLUMN breakdown_currency SET NOT NULL;
ALTER TABLE ordering.order_items ALTER COLUMN breakdown_currency SET DEFAULT '';

UPDATE ordering.order_items SET base_price = 0.0 WHERE base_price IS NULL;
ALTER TABLE ordering.order_items ALTER COLUMN base_price SET NOT NULL;
ALTER TABLE ordering.order_items ALTER COLUMN base_price SET DEFAULT 0.0;

UPDATE ordering.order_items SET applied_price_currency = '' WHERE applied_price_currency IS NULL;
ALTER TABLE ordering.order_items ALTER COLUMN applied_price_currency SET NOT NULL;
ALTER TABLE ordering.order_items ALTER COLUMN applied_price_currency SET DEFAULT '';

UPDATE ordering.order_items SET applied_price_amount = 0.0 WHERE applied_price_amount IS NULL;
ALTER TABLE ordering.order_items ALTER COLUMN applied_price_amount SET NOT NULL;
ALTER TABLE ordering.order_items ALTER COLUMN applied_price_amount SET DEFAULT 0.0;

INSERT INTO ordering."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20261002223241_RequireOrderNonNullableFields', '10.0.12');

COMMIT;

START TRANSACTION;

CREATE TABLE IF NOT EXISTS ordering.wolverine_incoming_envelopes (
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
          AND n.nspname = 'ordering') THEN
        ALTER TABLE ordering.wolverine_incoming_envelopes
            ADD CONSTRAINT pkey_wolverine_incoming_envelopes_id_received_at PRIMARY KEY (id, received_at);
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_keep_until ON ordering.wolverine_incoming_envelopes USING btree (keep_until) WHERE ((status)::text = 'Handled'::text);
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_owner ON ordering.wolverine_incoming_envelopes USING btree (owner_id) WHERE (owner_id <> 0);
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_recover ON ordering.wolverine_incoming_envelopes USING btree (received_at) WHERE (((status)::text = 'Incoming'::text) AND (owner_id = 0));
CREATE TABLE IF NOT EXISTS ordering.wolverine_outgoing_envelopes (
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
          AND n.nspname = 'ordering') THEN
        ALTER TABLE ordering.wolverine_outgoing_envelopes
            ADD CONSTRAINT pkey_wolverine_outgoing_envelopes_id PRIMARY KEY (id);
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_wolverine_outgoing_envelopes_owner ON ordering.wolverine_outgoing_envelopes USING btree (owner_id) WHERE (owner_id <> 0);
CREATE INDEX IF NOT EXISTS idx_wolverine_outgoing_envelopes_recover ON ordering.wolverine_outgoing_envelopes USING btree (destination) WHERE (owner_id = 0);


INSERT INTO ordering."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20261002232257_EnsureWolverineEnvelopeTables', '10.0.12');

COMMIT;

