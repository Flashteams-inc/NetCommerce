DO $EF$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = 'inventory') THEN
        CREATE SCHEMA inventory;
    END IF;
END $EF$;
CREATE TABLE IF NOT EXISTS inventory."__EFMigrationsHistory" (
    "MigrationId" character varying(150) NOT NULL,
    "ProductVersion" character varying(32) NOT NULL,
    CONSTRAINT "PK___EFMigrationsHistory" PRIMARY KEY ("MigrationId")
);

START TRANSACTION;
DO $EF$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = 'inventory') THEN
        CREATE SCHEMA inventory;
    END IF;
END $EF$;

CREATE TABLE inventory.stocks (
    id uuid NOT NULL,
    product_id uuid NOT NULL,
    sku character varying(100) NOT NULL,
    quantity integer NOT NULL,
    low_stock_threshold integer NOT NULL DEFAULT 10,
    warehouse_location character varying(200),
    last_updated_at timestamp with time zone NOT NULL,
    CONSTRAINT "PK_stocks" PRIMARY KEY (id)
);

CREATE TABLE inventory.stock_reservations (
    id uuid NOT NULL,
    stock_id uuid NOT NULL,
    order_id uuid NOT NULL,
    quantity integer NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    status character varying(50) NOT NULL,
    confirmed_at timestamp with time zone,
    released_at timestamp with time zone,
    CONSTRAINT "PK_stock_reservations" PRIMARY KEY (id),
    CONSTRAINT "FK_stock_reservations_stocks_stock_id" FOREIGN KEY (stock_id) REFERENCES inventory.stocks (id) ON DELETE CASCADE
);

CREATE INDEX "IX_stock_reservations_order_id" ON inventory.stock_reservations (order_id);

CREATE INDEX "IX_stock_reservations_status_expires_at" ON inventory.stock_reservations (status, expires_at) WHERE status = 'Active';

CREATE INDEX "IX_stock_reservations_status_updated_at" ON inventory.stock_reservations (status, updated_at) WHERE status = 'PendingPayment';

CREATE INDEX "IX_stock_reservations_stock_id" ON inventory.stock_reservations (stock_id);

CREATE UNIQUE INDEX "IX_stocks_product_id" ON inventory.stocks (product_id);

CREATE UNIQUE INDEX "IX_stocks_sku" ON inventory.stocks (sku);

INSERT INTO inventory."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20260903182703_InitialCreate', '10.0.12');

COMMIT;

START TRANSACTION;

CREATE TABLE IF NOT EXISTS inventory.wolverine_incoming_envelopes (
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
          AND n.nspname = 'inventory') THEN
        ALTER TABLE inventory.wolverine_incoming_envelopes
            ADD CONSTRAINT pkey_wolverine_incoming_envelopes_id_received_at PRIMARY KEY (id, received_at);
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_keep_until ON inventory.wolverine_incoming_envelopes USING btree (keep_until) WHERE ((status)::text = 'Handled'::text);
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_owner ON inventory.wolverine_incoming_envelopes USING btree (owner_id) WHERE (owner_id <> 0);
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_recover ON inventory.wolverine_incoming_envelopes USING btree (received_at) WHERE (((status)::text = 'Incoming'::text) AND (owner_id = 0));
CREATE TABLE IF NOT EXISTS inventory.wolverine_outgoing_envelopes (
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
          AND n.nspname = 'inventory') THEN
        ALTER TABLE inventory.wolverine_outgoing_envelopes
            ADD CONSTRAINT pkey_wolverine_outgoing_envelopes_id PRIMARY KEY (id);
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_wolverine_outgoing_envelopes_owner ON inventory.wolverine_outgoing_envelopes USING btree (owner_id) WHERE (owner_id <> 0);
CREATE INDEX IF NOT EXISTS idx_wolverine_outgoing_envelopes_recover ON inventory.wolverine_outgoing_envelopes USING btree (destination) WHERE (owner_id = 0);


INSERT INTO inventory."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20261002232310_EnsureWolverineEnvelopeTables', '10.0.12');

COMMIT;

