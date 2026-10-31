DO $EF$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = 'finance') THEN
        CREATE SCHEMA finance;
    END IF;
END $EF$;
CREATE TABLE IF NOT EXISTS finance."__EFMigrationsHistory" (
    "MigrationId" character varying(150) NOT NULL,
    "ProductVersion" character varying(32) NOT NULL,
    CONSTRAINT "PK___EFMigrationsHistory" PRIMARY KEY ("MigrationId")
);

START TRANSACTION;
DO $EF$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = 'finance') THEN
        CREATE SCHEMA finance;
    END IF;
END $EF$;

CREATE TABLE finance.financial_audit_log (
    "Id" uuid NOT NULL,
    "AuditType" character varying(50) NOT NULL,
    "EntityType" character varying(100) NOT NULL,
    "EntityId" character varying(100) NOT NULL,
    external_transaction_id character varying(100),
    "Amount" numeric(18,4),
    "Currency" character varying(3),
    "PreviousState" jsonb,
    "NewState" jsonb,
    "ActorId" character varying(200) NOT NULL,
    "ActorType" character varying(20) NOT NULL,
    "Description" character varying(1000) NOT NULL,
    "Metadata" jsonb,
    "OccurredAt" timestamp with time zone NOT NULL,
    correlation_id character varying(100),
    CONSTRAINT "PK_financial_audit_log" PRIMARY KEY ("Id")
);
COMMENT ON TABLE finance.financial_audit_log IS 'Immutable audit log - INSERT only, no UPDATE/DELETE';

CREATE TABLE finance.processed_webhook_events (
    "Id" uuid NOT NULL,
    "StripeEventId" character varying(100) NOT NULL,
    "EventType" character varying(100) NOT NULL,
    "ReceivedAt" timestamp with time zone NOT NULL,
    "ProcessedAt" timestamp with time zone,
    "Status" character varying(20) NOT NULL,
    "PaymentIntentId" character varying(100),
    "ErrorMessage" character varying(1000),
    CONSTRAINT "PK_processed_webhook_events" PRIMARY KEY ("Id")
);

CREATE TABLE finance."ReconciliationSessions" (
    "Id" uuid NOT NULL,
    "CalculatedForDate" timestamp with time zone NOT NULL,
    "Status" text NOT NULL,
    "TotalInternalAmount" numeric(18,2) NOT NULL,
    "TotalExternalAmount" numeric(18,2) NOT NULL,
    "StartedAt" timestamp with time zone NOT NULL,
    "CompletedAt" timestamp with time zone,
    "Notes" character varying(2000),
    "Version" bigint NOT NULL,
    CONSTRAINT "PK_ReconciliationSessions" PRIMARY KEY ("Id")
);

CREATE TABLE finance."ReconciliationDiscrepancies" (
    "ExternalTxnId" character varying(100) NOT NULL,
    "DetectedAt" timestamp with time zone NOT NULL,
    "ReconciliationSessionId" uuid NOT NULL,
    "Type" text NOT NULL,
    "Difference" numeric(18,2) NOT NULL,
    "Reason" character varying(500),
    CONSTRAINT "PK_ReconciliationDiscrepancies" PRIMARY KEY ("ReconciliationSessionId", "ExternalTxnId", "DetectedAt"),
    CONSTRAINT "FK_ReconciliationDiscrepancies_ReconciliationSessions_Reconcil~" FOREIGN KEY ("ReconciliationSessionId") REFERENCES finance."ReconciliationSessions" ("Id") ON DELETE CASCADE
);

CREATE INDEX ix_financial_audit_correlation ON finance.financial_audit_log (correlation_id) WHERE correlation_id IS NOT NULL;

CREATE INDEX ix_financial_audit_entity ON finance.financial_audit_log ("EntityType", "EntityId");

CREATE INDEX ix_financial_audit_external_txn ON finance.financial_audit_log (external_transaction_id) WHERE external_transaction_id IS NOT NULL;

CREATE INDEX ix_financial_audit_occurred_at ON finance.financial_audit_log ("OccurredAt");

CREATE INDEX ix_financial_audit_type ON finance.financial_audit_log ("AuditType");

CREATE INDEX ix_processed_webhook_events_payment_intent_id ON finance.processed_webhook_events ("PaymentIntentId");

CREATE INDEX ix_processed_webhook_events_received_at ON finance.processed_webhook_events ("ReceivedAt");

CREATE UNIQUE INDEX ix_processed_webhook_events_stripe_event_id ON finance.processed_webhook_events ("StripeEventId");

CREATE INDEX "IX_ReconciliationSessions_CalculatedForDate" ON finance."ReconciliationSessions" ("CalculatedForDate");

CREATE INDEX "IX_ReconciliationSessions_Status" ON finance."ReconciliationSessions" ("Status");

INSERT INTO finance."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20260903182823_InitialCreate', '10.0.12');

COMMIT;

START TRANSACTION;
CREATE TABLE finance.pii_vault_entries (
    "Id" uuid NOT NULL,
    "EncryptedFullName" text NOT NULL,
    "EncryptedEmail" text NOT NULL,
    "EmailBlindIndex" character varying(256) NOT NULL,
    "EncryptedPhoneNumber" text NOT NULL,
    "PhoneBlindIndex" character varying(256) NOT NULL,
    "EncryptedAddress" text NOT NULL,
    "EncryptedDateOfBirth" text,
    "EncryptedNationalId" text,
    "ProfileId" uuid NOT NULL,
    "UserId" character varying(256) NOT NULL,
    "TenantId" character varying(128) NOT NULL,
    "DeletedBy" character varying(256),
    "CreatedAt" timestamp with time zone NOT NULL,
    "UpdatedAt" timestamp with time zone NOT NULL,
    "LastAccessedAt" timestamp with time zone NOT NULL,
    "KeyVersion" integer NOT NULL,
    "IsDeleted" boolean NOT NULL,
    "DeletedAt" timestamp with time zone,
    CONSTRAINT "PK_pii_vault_entries" PRIMARY KEY ("Id")
);

CREATE UNIQUE INDEX ix_pii_vault_entries_profile_id ON finance.pii_vault_entries ("ProfileId");

CREATE UNIQUE INDEX ix_pii_vault_entries_tenant_email_index ON finance.pii_vault_entries ("TenantId", "EmailBlindIndex");

CREATE UNIQUE INDEX ix_pii_vault_entries_tenant_phone_index ON finance.pii_vault_entries ("TenantId", "PhoneBlindIndex");

CREATE INDEX "IX_pii_vault_entries_TenantId" ON finance.pii_vault_entries ("TenantId");

CREATE INDEX ix_pii_vault_entries_user_id ON finance.pii_vault_entries ("UserId");

INSERT INTO finance."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20260903215630_AddPiiVaultEntries', '10.0.12');

COMMIT;

START TRANSACTION;
DROP INDEX finance."IX_pii_vault_entries_TenantId";

INSERT INTO finance."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20260926214705_RemoveStalePiiVaultTenantIndex', '10.0.12');

COMMIT;

START TRANSACTION;
UPDATE finance."ReconciliationDiscrepancies" SET "Reason" = '' WHERE "Reason" IS NULL;
ALTER TABLE finance."ReconciliationDiscrepancies" ALTER COLUMN "Reason" SET NOT NULL;
ALTER TABLE finance."ReconciliationDiscrepancies" ALTER COLUMN "Reason" SET DEFAULT '';

INSERT INTO finance."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20261002223305_RequireReconciliationDiscrepancyFields', '10.0.12');

COMMIT;

START TRANSACTION;

CREATE TABLE IF NOT EXISTS finance.wolverine_incoming_envelopes (
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
          AND n.nspname = 'finance') THEN
        ALTER TABLE finance.wolverine_incoming_envelopes
            ADD CONSTRAINT pkey_wolverine_incoming_envelopes_id_received_at PRIMARY KEY (id, received_at);
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_keep_until ON finance.wolverine_incoming_envelopes USING btree (keep_until) WHERE ((status)::text = 'Handled'::text);
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_owner ON finance.wolverine_incoming_envelopes USING btree (owner_id) WHERE (owner_id <> 0);
CREATE INDEX IF NOT EXISTS idx_wolverine_incoming_envelopes_recover ON finance.wolverine_incoming_envelopes USING btree (received_at) WHERE (((status)::text = 'Incoming'::text) AND (owner_id = 0));
CREATE TABLE IF NOT EXISTS finance.wolverine_outgoing_envelopes (
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
          AND n.nspname = 'finance') THEN
        ALTER TABLE finance.wolverine_outgoing_envelopes
            ADD CONSTRAINT pkey_wolverine_outgoing_envelopes_id PRIMARY KEY (id);
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_wolverine_outgoing_envelopes_owner ON finance.wolverine_outgoing_envelopes USING btree (owner_id) WHERE (owner_id <> 0);
CREATE INDEX IF NOT EXISTS idx_wolverine_outgoing_envelopes_recover ON finance.wolverine_outgoing_envelopes USING btree (destination) WHERE (owner_id = 0);


INSERT INTO finance."__EFMigrationsHistory" ("MigrationId", "ProductVersion")
VALUES ('20261002232337_EnsureWolverineEnvelopeTables', '10.0.12');

COMMIT;

