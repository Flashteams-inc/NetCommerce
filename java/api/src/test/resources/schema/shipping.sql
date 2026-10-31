DO $EF$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = 'shipping') THEN
        CREATE SCHEMA shipping;
    END IF;
END $EF$;
CREATE TABLE IF NOT EXISTS shipping."__EFMigrationsHistory" (
    "MigrationId" character varying(150) NOT NULL,
    "ProductVersion" character varying(32) NOT NULL,
    CONSTRAINT "PK___EFMigrationsHistory" PRIMARY KEY ("MigrationId")
);

START TRANSACTION;
DO $EF$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = 'shipping') THEN
        CREATE SCHEMA shipping;
    END IF;
END $EF$;

CREATE TABLE shipping.shipments (
    "Id" uuid NOT NULL,
    "OrderId" uuid NOT NULL,
    "TrackingNumber" character varying(50) NOT NULL,
    "CourierProvider" character varying(50) NOT NULL,
    "WeightKg" numeric(10,2) NOT NULL,
    length_cm numeric,
    width_cm numeric,
    height_cm numeric,
    "Status" character varying(30) NOT NULL,
    recipient_name character varying(200),
    street character varying(500),
    city character varying(100),
    state character varying(100),
    country character varying(100),
    postal_code character varying(20),
    phone character varying(30),
    "CreatedAt" timestamp with time zone NOT NULL,
    "PickedUpAt" timestamp with time zone,
    "DeliveredAt" timestamp with time zone,
    "EstimatedDeliveryDate" timestamp with time zone,
    "FailureReason" character varying(500),
    CONSTRAINT "PK_shipments" PRIMARY KEY ("Id")
);

CREATE INDEX "IX_shipments_CourierProvider" ON shipping.shipments ("CourierProvider");

CREATE UNIQUE INDEX "IX_shipments_OrderId" ON shipping.shipments ("OrderId");

CREATE INDEX "IX_shipments_Status" ON shipping.shipments ("Status");

CREATE INDEX "IX_shipments_Status_CreatedAt" ON shipping.shipments ("Status", "CreatedAt");

CREATE UNIQUE INDEX "IX_shipments_TrackingNumber" ON shipping.shipments ("TrackingNumber");

INSERT INTO shipping."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20260903182839_InitialCreate', '10.0.12');

COMMIT;

START TRANSACTION;
UPDATE shipping.shipments SET width_cm = 0.0 WHERE width_cm IS NULL;
ALTER TABLE shipping.shipments ALTER COLUMN width_cm SET NOT NULL;
ALTER TABLE shipping.shipments ALTER COLUMN width_cm SET DEFAULT 0.0;

UPDATE shipping.shipments SET street = '' WHERE street IS NULL;
ALTER TABLE shipping.shipments ALTER COLUMN street SET NOT NULL;
ALTER TABLE shipping.shipments ALTER COLUMN street SET DEFAULT '';

UPDATE shipping.shipments SET state = '' WHERE state IS NULL;
ALTER TABLE shipping.shipments ALTER COLUMN state SET NOT NULL;
ALTER TABLE shipping.shipments ALTER COLUMN state SET DEFAULT '';

UPDATE shipping.shipments SET recipient_name = '' WHERE recipient_name IS NULL;
ALTER TABLE shipping.shipments ALTER COLUMN recipient_name SET NOT NULL;
ALTER TABLE shipping.shipments ALTER COLUMN recipient_name SET DEFAULT '';

UPDATE shipping.shipments SET postal_code = '' WHERE postal_code IS NULL;
ALTER TABLE shipping.shipments ALTER COLUMN postal_code SET NOT NULL;
ALTER TABLE shipping.shipments ALTER COLUMN postal_code SET DEFAULT '';

UPDATE shipping.shipments SET phone = '' WHERE phone IS NULL;
ALTER TABLE shipping.shipments ALTER COLUMN phone SET NOT NULL;
ALTER TABLE shipping.shipments ALTER COLUMN phone SET DEFAULT '';

UPDATE shipping.shipments SET length_cm = 0.0 WHERE length_cm IS NULL;
ALTER TABLE shipping.shipments ALTER COLUMN length_cm SET NOT NULL;
ALTER TABLE shipping.shipments ALTER COLUMN length_cm SET DEFAULT 0.0;

UPDATE shipping.shipments SET height_cm = 0.0 WHERE height_cm IS NULL;
ALTER TABLE shipping.shipments ALTER COLUMN height_cm SET NOT NULL;
ALTER TABLE shipping.shipments ALTER COLUMN height_cm SET DEFAULT 0.0;

UPDATE shipping.shipments SET country = '' WHERE country IS NULL;
ALTER TABLE shipping.shipments ALTER COLUMN country SET NOT NULL;
ALTER TABLE shipping.shipments ALTER COLUMN country SET DEFAULT '';

UPDATE shipping.shipments SET city = '' WHERE city IS NULL;
ALTER TABLE shipping.shipments ALTER COLUMN city SET NOT NULL;
ALTER TABLE shipping.shipments ALTER COLUMN city SET DEFAULT '';

INSERT INTO shipping."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20261002223318_RequireShipmentFields', '10.0.12');

COMMIT;

START TRANSACTION;

CREATE TABLE IF NOT EXISTS shipping.wolverine_incoming_envelopes (
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
          AND n.nspname = 'shipping') THEN
        ALTER TABLE shipping.wolverine_incoming_envelopes
            ADD CONSTRAINT pkey_wolverine_incoming_envelopes_id_received_at PRIMARY KEY (id, received_at);
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_keep_until ON shipping.wolverine_incoming_envelopes USING btree (keep_until) WHERE ((status)::text = 'Handled'::text);
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_owner ON shipping.wolverine_incoming_envelopes USING btree (owner_id) WHERE (owner_id <> 0);
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_recover ON shipping.wolverine_incoming_envelopes USING btree (received_at) WHERE (((status)::text = 'Incoming'::text) AND (owner_id = 0));
CREATE TABLE IF NOT EXISTS shipping.wolverine_outgoing_envelopes (
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
          AND n.nspname = 'shipping') THEN
        ALTER TABLE shipping.wolverine_outgoing_envelopes
            ADD CONSTRAINT pkey_wolverine_outgoing_envelopes_id PRIMARY KEY (id);
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_wolverine_outgoing_envelopes_owner ON shipping.wolverine_outgoing_envelopes USING btree (owner_id) WHERE (owner_id <> 0);
CREATE INDEX IF NOT EXISTS idx_wolverine_outgoing_envelopes_recover ON shipping.wolverine_outgoing_envelopes USING btree (destination) WHERE (owner_id = 0);


INSERT INTO shipping."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20261002232351_EnsureWolverineEnvelopeTables', '10.0.12');

COMMIT;

