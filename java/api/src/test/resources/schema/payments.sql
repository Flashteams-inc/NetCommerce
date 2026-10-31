DO $EF$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = 'payments') THEN
        CREATE SCHEMA payments;
    END IF;
END $EF$;
CREATE TABLE IF NOT EXISTS payments."__EFMigrationsHistory" (
    "MigrationId" character varying(150) NOT NULL,
    "ProductVersion" character varying(32) NOT NULL,
    CONSTRAINT "PK___EFMigrationsHistory" PRIMARY KEY ("MigrationId")
);

START TRANSACTION;
DO $EF$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = 'payments') THEN
        CREATE SCHEMA payments;
    END IF;
END $EF$;

CREATE TABLE payments.payment_transactions (
    id uuid NOT NULL,
    order_id uuid NOT NULL,
    amount numeric(18,2),
    currency character varying(3),
    provider character varying(50) NOT NULL,
    status character varying(50) NOT NULL,
    external_transaction_id character varying(256),
    idempotency_key character varying(100),
    created_at timestamp with time zone NOT NULL,
    completed_at timestamp with time zone,
    failure_reason character varying(1000),
    metadata jsonb,
    "Version" bigint NOT NULL,
    CONSTRAINT "PK_payment_transactions" PRIMARY KEY (id)
);

CREATE INDEX "IX_payment_transactions_external_transaction_id" ON payments.payment_transactions (external_transaction_id) WHERE external_transaction_id IS NOT NULL;

CREATE UNIQUE INDEX "IX_payment_transactions_idempotency_key" ON payments.payment_transactions (idempotency_key) WHERE idempotency_key IS NOT NULL;

CREATE INDEX "IX_payment_transactions_order_id" ON payments.payment_transactions (order_id);

INSERT INTO payments."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20260903182806_InitialCreate', '10.0.12');

COMMIT;

START TRANSACTION;
UPDATE payments.payment_transactions SET currency = '' WHERE currency IS NULL;
ALTER TABLE payments.payment_transactions ALTER COLUMN currency SET NOT NULL;
ALTER TABLE payments.payment_transactions ALTER COLUMN currency SET DEFAULT '';

UPDATE payments.payment_transactions SET amount = 0.0 WHERE amount IS NULL;
ALTER TABLE payments.payment_transactions ALTER COLUMN amount SET NOT NULL;
ALTER TABLE payments.payment_transactions ALTER COLUMN amount SET DEFAULT 0.0;

INSERT INTO payments."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20261002223253_RequirePaymentTransactionFields', '10.0.12');

COMMIT;

START TRANSACTION;

CREATE TABLE IF NOT EXISTS payments.wolverine_incoming_envelopes (
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
          AND n.nspname = 'payments') THEN
        ALTER TABLE payments.wolverine_incoming_envelopes
            ADD CONSTRAINT pkey_wolverine_incoming_envelopes_id_received_at PRIMARY KEY (id, received_at);
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_keep_until ON payments.wolverine_incoming_envelopes USING btree (keep_until) WHERE ((status)::text = 'Handled'::text);
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_owner ON payments.wolverine_incoming_envelopes USING btree (owner_id) WHERE (owner_id <> 0);
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_recover ON payments.wolverine_incoming_envelopes USING btree (received_at) WHERE (((status)::text = 'Incoming'::text) AND (owner_id = 0));
CREATE TABLE IF NOT EXISTS payments.wolverine_outgoing_envelopes (
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
          AND n.nspname = 'payments') THEN
        ALTER TABLE payments.wolverine_outgoing_envelopes
            ADD CONSTRAINT pkey_wolverine_outgoing_envelopes_id PRIMARY KEY (id);
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_wolverine_outgoing_envelopes_owner ON payments.wolverine_outgoing_envelopes USING btree (owner_id) WHERE (owner_id <> 0);
CREATE INDEX IF NOT EXISTS idx_wolverine_outgoing_envelopes_recover ON payments.wolverine_outgoing_envelopes USING btree (destination) WHERE (owner_id = 0);


INSERT INTO payments."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20261002232324_EnsureWolverineEnvelopeTables', '10.0.12');

COMMIT;

