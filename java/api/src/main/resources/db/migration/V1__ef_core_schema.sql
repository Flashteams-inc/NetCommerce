-- V1: the schema the .NET EF Core migrations created, for every context
-- (catalog, finance, inventory, ordering, payments, shipping), as
-- pg_dump --schema-only gives it from a database those migrations built. Each
-- context's __EFMigrationsHistory table is part of that schema and stays,
-- unused, until .NET is decommissioned.
--
-- An existing database is not touched by this file: Flyway marks it baselined
-- at version 1 (spring.flyway.baseline-on-migrate) and applies only what
-- follows. This file builds the same schema in an empty database.

CREATE SCHEMA catalog;

CREATE SCHEMA finance;

CREATE SCHEMA inventory;

CREATE SCHEMA ordering;

CREATE SCHEMA payments;

CREATE SCHEMA shipping;

CREATE TABLE catalog."__EFMigrationsHistory" (
    "MigrationId" character varying(150) NOT NULL,
    "ProductVersion" character varying(32) NOT NULL
);

CREATE TABLE catalog.categories (
    "Id" uuid NOT NULL,
    "Name" character varying(100) NOT NULL,
    "Description" character varying(500) DEFAULT ''::character varying NOT NULL,
    "Slug" character varying(150) NOT NULL,
    "ParentCategoryId" uuid,
    "DisplayOrder" integer NOT NULL,
    "IsActive" boolean DEFAULT true NOT NULL,
    "ImageKey" character varying(500)
);

CREATE TABLE catalog.product_attributes (
    "Key" character varying(100) NOT NULL,
    "ProductId" uuid NOT NULL,
    "Value" character varying(500) NOT NULL,
    "DisplayName" character varying(100)
);

CREATE TABLE catalog.product_images (
    "Id" uuid NOT NULL,
    "ImageKey" character varying(500) NOT NULL,
    "DisplayOrder" integer NOT NULL,
    "IsPrimary" boolean NOT NULL,
    "ProductId" uuid NOT NULL
);

CREATE TABLE catalog.products (
    "Id" uuid NOT NULL,
    "Name" character varying(200) NOT NULL,
    "Description" character varying(5000) NOT NULL,
    "Sku" character varying(50) NOT NULL,
    price numeric(18,2) DEFAULT 0.0 NOT NULL,
    currency character varying(3) DEFAULT ''::character varying NOT NULL,
    "WeightKg" numeric NOT NULL,
    "CategoryId" uuid NOT NULL,
    "Status" text NOT NULL,
    "SeoTitle" character varying(70),
    "SeoDescription" character varying(160),
    "Slug" character varying(250),
    search_vector tsvector
);

CREATE TABLE catalog.wolverine_incoming_envelopes (
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

CREATE TABLE catalog.wolverine_outgoing_envelopes (
    id uuid NOT NULL,
    owner_id integer NOT NULL,
    destination character varying NOT NULL,
    deliver_by timestamp with time zone,
    body bytea NOT NULL,
    attempts integer DEFAULT 0,
    message_type character varying NOT NULL
);

CREATE TABLE finance."ReconciliationDiscrepancies" (
    "ExternalTxnId" character varying(100) NOT NULL,
    "DetectedAt" timestamp with time zone NOT NULL,
    "ReconciliationSessionId" uuid NOT NULL,
    "Type" text NOT NULL,
    "Difference" numeric(18,2) NOT NULL,
    "Reason" character varying(500) DEFAULT ''::character varying NOT NULL
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
    "Version" bigint NOT NULL
);

CREATE TABLE finance."__EFMigrationsHistory" (
    "MigrationId" character varying(150) NOT NULL,
    "ProductVersion" character varying(32) NOT NULL
);

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
    correlation_id character varying(100)
);

COMMENT ON TABLE finance.financial_audit_log IS 'Immutable audit log - INSERT only, no UPDATE/DELETE';

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
    "DeletedAt" timestamp with time zone
);

CREATE TABLE finance.processed_webhook_events (
    "Id" uuid NOT NULL,
    "StripeEventId" character varying(100) NOT NULL,
    "EventType" character varying(100) NOT NULL,
    "ReceivedAt" timestamp with time zone NOT NULL,
    "ProcessedAt" timestamp with time zone,
    "Status" character varying(20) NOT NULL,
    "PaymentIntentId" character varying(100),
    "ErrorMessage" character varying(1000)
);

CREATE TABLE finance.wolverine_incoming_envelopes (
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

CREATE TABLE finance.wolverine_outgoing_envelopes (
    id uuid NOT NULL,
    owner_id integer NOT NULL,
    destination character varying NOT NULL,
    deliver_by timestamp with time zone,
    body bytea NOT NULL,
    attempts integer DEFAULT 0,
    message_type character varying NOT NULL
);

CREATE TABLE inventory."__EFMigrationsHistory" (
    "MigrationId" character varying(150) NOT NULL,
    "ProductVersion" character varying(32) NOT NULL
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
    released_at timestamp with time zone
);

CREATE TABLE inventory.stocks (
    id uuid NOT NULL,
    product_id uuid NOT NULL,
    sku character varying(100) NOT NULL,
    quantity integer NOT NULL,
    low_stock_threshold integer DEFAULT 10 NOT NULL,
    warehouse_location character varying(200),
    last_updated_at timestamp with time zone NOT NULL
);

CREATE TABLE inventory.wolverine_incoming_envelopes (
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

CREATE TABLE inventory.wolverine_outgoing_envelopes (
    id uuid NOT NULL,
    owner_id integer NOT NULL,
    destination character varying NOT NULL,
    deliver_by timestamp with time zone,
    body bytea NOT NULL,
    attempts integer DEFAULT 0,
    message_type character varying NOT NULL
);

CREATE TABLE ordering."__EFMigrationsHistory" (
    "MigrationId" character varying(150) NOT NULL,
    "ProductVersion" character varying(32) NOT NULL
);

CREATE TABLE ordering.order_items (
    id uuid NOT NULL,
    product_id uuid NOT NULL,
    applied_title character varying(500) NOT NULL,
    applied_price_amount numeric(18,2) DEFAULT 0.0 NOT NULL,
    applied_price_currency character varying(3) DEFAULT ''::character varying NOT NULL,
    applied_weight_kg numeric(10,3) NOT NULL,
    quantity integer NOT NULL,
    sku character varying(100),
    base_price numeric(18,2) DEFAULT 0.0 NOT NULL,
    discount_amount numeric(18,2) DEFAULT 0.0 NOT NULL,
    tax_amount numeric(18,2) DEFAULT 0.0 NOT NULL,
    tax_rate numeric(10,4) DEFAULT 0.0 NOT NULL,
    tax_type character varying(50) DEFAULT ''::character varying NOT NULL,
    breakdown_currency character varying(3) DEFAULT ''::character varying NOT NULL,
    breakdown_quantity integer DEFAULT 0 NOT NULL,
    line_discount_total numeric(18,2) DEFAULT 0.0 NOT NULL,
    line_tax_total numeric(18,2) DEFAULT 0.0 NOT NULL,
    order_id uuid
);

COMMENT ON COLUMN ordering.order_items.base_price IS 'Original price from catalog at order time';

COMMENT ON COLUMN ordering.order_items.discount_amount IS 'Discount per unit (for backward compatibility)';

COMMENT ON COLUMN ordering.order_items.tax_amount IS 'Tax per unit (for backward compatibility)';

COMMENT ON COLUMN ordering.order_items.tax_rate IS 'Tax rate applied (e.g., 0.18 for 18% VAT) - crucial for legal audits';

COMMENT ON COLUMN ordering.order_items.tax_type IS 'Type of tax applied (VAT, SALES_TAX, GST, etc.)';

COMMENT ON COLUMN ordering.order_items.breakdown_quantity IS 'Quantity for this pricing breakdown';

COMMENT ON COLUMN ordering.order_items.line_discount_total IS 'TOTAL discount for line item - PRIMARY source of truth to avoid penny variance';

COMMENT ON COLUMN ordering.order_items.line_tax_total IS 'TOTAL tax for line item - PRIMARY source of truth to avoid penny variance';

CREATE TABLE ordering.orders (
    id uuid NOT NULL,
    order_number character varying(50) NOT NULL,
    customer_id uuid NOT NULL,
    tenant_id character varying(100) NOT NULL,
    status character varying(50) NOT NULL,
    total_amount numeric(18,2) DEFAULT 0.0 NOT NULL,
    total_currency character varying(3) DEFAULT ''::character varying NOT NULL,
    shipping_recipient character varying(200) DEFAULT ''::character varying NOT NULL,
    shipping_street character varying(200) DEFAULT ''::character varying NOT NULL,
    shipping_city character varying(100) DEFAULT ''::character varying NOT NULL,
    shipping_state character varying(100) DEFAULT ''::character varying NOT NULL,
    shipping_country character varying(100) DEFAULT ''::character varying NOT NULL,
    shipping_postal_code character varying(20) DEFAULT ''::character varying NOT NULL,
    shipping_phone character varying(50) DEFAULT ''::character varying NOT NULL,
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
    idempotency_key character varying(100) DEFAULT ''::character varying NOT NULL,
    "IsShadowOrder" boolean NOT NULL,
    "SourceDiscrepancyTxnId" text,
    "Version" bigint NOT NULL
);

CREATE TABLE ordering.wolverine_incoming_envelopes (
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

CREATE TABLE ordering.wolverine_outgoing_envelopes (
    id uuid NOT NULL,
    owner_id integer NOT NULL,
    destination character varying NOT NULL,
    deliver_by timestamp with time zone,
    body bytea NOT NULL,
    attempts integer DEFAULT 0,
    message_type character varying NOT NULL
);

CREATE TABLE payments."__EFMigrationsHistory" (
    "MigrationId" character varying(150) NOT NULL,
    "ProductVersion" character varying(32) NOT NULL
);

CREATE TABLE payments.payment_transactions (
    id uuid NOT NULL,
    order_id uuid NOT NULL,
    amount numeric(18,2) DEFAULT 0.0 NOT NULL,
    currency character varying(3) DEFAULT ''::character varying NOT NULL,
    provider character varying(50) NOT NULL,
    status character varying(50) NOT NULL,
    external_transaction_id character varying(256),
    idempotency_key character varying(100),
    created_at timestamp with time zone NOT NULL,
    completed_at timestamp with time zone,
    failure_reason character varying(1000),
    metadata jsonb,
    "Version" bigint NOT NULL
);

CREATE TABLE payments.wolverine_incoming_envelopes (
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

CREATE TABLE payments.wolverine_outgoing_envelopes (
    id uuid NOT NULL,
    owner_id integer NOT NULL,
    destination character varying NOT NULL,
    deliver_by timestamp with time zone,
    body bytea NOT NULL,
    attempts integer DEFAULT 0,
    message_type character varying NOT NULL
);

CREATE TABLE shipping."__EFMigrationsHistory" (
    "MigrationId" character varying(150) NOT NULL,
    "ProductVersion" character varying(32) NOT NULL
);

CREATE TABLE shipping.shipments (
    "Id" uuid NOT NULL,
    "OrderId" uuid NOT NULL,
    "TrackingNumber" character varying(50) NOT NULL,
    "CourierProvider" character varying(50) NOT NULL,
    "WeightKg" numeric(10,2) NOT NULL,
    length_cm numeric DEFAULT 0.0 NOT NULL,
    width_cm numeric DEFAULT 0.0 NOT NULL,
    height_cm numeric DEFAULT 0.0 NOT NULL,
    "Status" character varying(30) NOT NULL,
    recipient_name character varying(200) DEFAULT ''::character varying NOT NULL,
    street character varying(500) DEFAULT ''::character varying NOT NULL,
    city character varying(100) DEFAULT ''::character varying NOT NULL,
    state character varying(100) DEFAULT ''::character varying NOT NULL,
    country character varying(100) DEFAULT ''::character varying NOT NULL,
    postal_code character varying(20) DEFAULT ''::character varying NOT NULL,
    phone character varying(30) DEFAULT ''::character varying NOT NULL,
    "CreatedAt" timestamp with time zone NOT NULL,
    "PickedUpAt" timestamp with time zone,
    "DeliveredAt" timestamp with time zone,
    "EstimatedDeliveryDate" timestamp with time zone,
    "FailureReason" character varying(500)
);

CREATE TABLE shipping.wolverine_incoming_envelopes (
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

CREATE TABLE shipping.wolverine_outgoing_envelopes (
    id uuid NOT NULL,
    owner_id integer NOT NULL,
    destination character varying NOT NULL,
    deliver_by timestamp with time zone,
    body bytea NOT NULL,
    attempts integer DEFAULT 0,
    message_type character varying NOT NULL
);

ALTER TABLE ONLY catalog."__EFMigrationsHistory"
    ADD CONSTRAINT "PK___EFMigrationsHistory" PRIMARY KEY ("MigrationId");

ALTER TABLE ONLY catalog.categories
    ADD CONSTRAINT "PK_categories" PRIMARY KEY ("Id");

ALTER TABLE ONLY catalog.product_attributes
    ADD CONSTRAINT "PK_product_attributes" PRIMARY KEY ("ProductId", "Key");

ALTER TABLE ONLY catalog.product_images
    ADD CONSTRAINT "PK_product_images" PRIMARY KEY ("Id");

ALTER TABLE ONLY catalog.products
    ADD CONSTRAINT "PK_products" PRIMARY KEY ("Id");

ALTER TABLE ONLY catalog.wolverine_incoming_envelopes
    ADD CONSTRAINT pkey_wolverine_incoming_envelopes_id_received_at PRIMARY KEY (id, received_at);

ALTER TABLE ONLY catalog.wolverine_outgoing_envelopes
    ADD CONSTRAINT pkey_wolverine_outgoing_envelopes_id PRIMARY KEY (id);

ALTER TABLE ONLY finance."ReconciliationDiscrepancies"
    ADD CONSTRAINT "PK_ReconciliationDiscrepancies" PRIMARY KEY ("ReconciliationSessionId", "ExternalTxnId", "DetectedAt");

ALTER TABLE ONLY finance."ReconciliationSessions"
    ADD CONSTRAINT "PK_ReconciliationSessions" PRIMARY KEY ("Id");

ALTER TABLE ONLY finance."__EFMigrationsHistory"
    ADD CONSTRAINT "PK___EFMigrationsHistory" PRIMARY KEY ("MigrationId");

ALTER TABLE ONLY finance.financial_audit_log
    ADD CONSTRAINT "PK_financial_audit_log" PRIMARY KEY ("Id");

ALTER TABLE ONLY finance.pii_vault_entries
    ADD CONSTRAINT "PK_pii_vault_entries" PRIMARY KEY ("Id");

ALTER TABLE ONLY finance.processed_webhook_events
    ADD CONSTRAINT "PK_processed_webhook_events" PRIMARY KEY ("Id");

ALTER TABLE ONLY finance.wolverine_incoming_envelopes
    ADD CONSTRAINT pkey_wolverine_incoming_envelopes_id_received_at PRIMARY KEY (id, received_at);

ALTER TABLE ONLY finance.wolverine_outgoing_envelopes
    ADD CONSTRAINT pkey_wolverine_outgoing_envelopes_id PRIMARY KEY (id);

ALTER TABLE ONLY inventory."__EFMigrationsHistory"
    ADD CONSTRAINT "PK___EFMigrationsHistory" PRIMARY KEY ("MigrationId");

ALTER TABLE ONLY inventory.stock_reservations
    ADD CONSTRAINT "PK_stock_reservations" PRIMARY KEY (id);

ALTER TABLE ONLY inventory.stocks
    ADD CONSTRAINT "PK_stocks" PRIMARY KEY (id);

ALTER TABLE ONLY inventory.wolverine_incoming_envelopes
    ADD CONSTRAINT pkey_wolverine_incoming_envelopes_id_received_at PRIMARY KEY (id, received_at);

ALTER TABLE ONLY inventory.wolverine_outgoing_envelopes
    ADD CONSTRAINT pkey_wolverine_outgoing_envelopes_id PRIMARY KEY (id);

ALTER TABLE ONLY ordering."__EFMigrationsHistory"
    ADD CONSTRAINT "PK___EFMigrationsHistory" PRIMARY KEY ("MigrationId");

ALTER TABLE ONLY ordering.order_items
    ADD CONSTRAINT "PK_order_items" PRIMARY KEY (id);

ALTER TABLE ONLY ordering.orders
    ADD CONSTRAINT "PK_orders" PRIMARY KEY (id);

ALTER TABLE ONLY ordering.wolverine_incoming_envelopes
    ADD CONSTRAINT pkey_wolverine_incoming_envelopes_id_received_at PRIMARY KEY (id, received_at);

ALTER TABLE ONLY ordering.wolverine_outgoing_envelopes
    ADD CONSTRAINT pkey_wolverine_outgoing_envelopes_id PRIMARY KEY (id);

ALTER TABLE ONLY payments."__EFMigrationsHistory"
    ADD CONSTRAINT "PK___EFMigrationsHistory" PRIMARY KEY ("MigrationId");

ALTER TABLE ONLY payments.payment_transactions
    ADD CONSTRAINT "PK_payment_transactions" PRIMARY KEY (id);

ALTER TABLE ONLY payments.wolverine_incoming_envelopes
    ADD CONSTRAINT pkey_wolverine_incoming_envelopes_id_received_at PRIMARY KEY (id, received_at);

ALTER TABLE ONLY payments.wolverine_outgoing_envelopes
    ADD CONSTRAINT pkey_wolverine_outgoing_envelopes_id PRIMARY KEY (id);

ALTER TABLE ONLY shipping."__EFMigrationsHistory"
    ADD CONSTRAINT "PK___EFMigrationsHistory" PRIMARY KEY ("MigrationId");

ALTER TABLE ONLY shipping.shipments
    ADD CONSTRAINT "PK_shipments" PRIMARY KEY ("Id");

ALTER TABLE ONLY shipping.wolverine_incoming_envelopes
    ADD CONSTRAINT pkey_wolverine_incoming_envelopes_id_received_at PRIMARY KEY (id, received_at);

ALTER TABLE ONLY shipping.wolverine_outgoing_envelopes
    ADD CONSTRAINT pkey_wolverine_outgoing_envelopes_id PRIMARY KEY (id);

CREATE INDEX "IX_categories_ParentCategoryId" ON catalog.categories USING btree ("ParentCategoryId");

CREATE UNIQUE INDEX "IX_categories_Slug" ON catalog.categories USING btree ("Slug");

CREATE INDEX "IX_product_images_ProductId" ON catalog.product_images USING btree ("ProductId");

CREATE INDEX "IX_products_CategoryId" ON catalog.products USING btree ("CategoryId");

CREATE UNIQUE INDEX "IX_products_Sku" ON catalog.products USING btree ("Sku");

CREATE INDEX "IX_products_Slug" ON catalog.products USING btree ("Slug");

CREATE INDEX "IX_products_search_vector" ON catalog.products USING gin (search_vector);

CREATE INDEX idx_wolverine_incoming_envelopes_keep_until ON catalog.wolverine_incoming_envelopes USING btree (keep_until) WHERE ((status)::text = 'Handled'::text);

CREATE INDEX idx_wolverine_incoming_envelopes_owner ON catalog.wolverine_incoming_envelopes USING btree (owner_id) WHERE (owner_id <> 0);

CREATE INDEX idx_wolverine_incoming_envelopes_recover ON catalog.wolverine_incoming_envelopes USING btree (received_at) WHERE (((status)::text = 'Incoming'::text) AND (owner_id = 0));

CREATE INDEX idx_wolverine_outgoing_envelopes_owner ON catalog.wolverine_outgoing_envelopes USING btree (owner_id) WHERE (owner_id <> 0);

CREATE INDEX idx_wolverine_outgoing_envelopes_recover ON catalog.wolverine_outgoing_envelopes USING btree (destination) WHERE (owner_id = 0);

CREATE INDEX "IX_ReconciliationSessions_CalculatedForDate" ON finance."ReconciliationSessions" USING btree ("CalculatedForDate");

CREATE INDEX "IX_ReconciliationSessions_Status" ON finance."ReconciliationSessions" USING btree ("Status");

CREATE INDEX idx_wolverine_incoming_envelopes_keep_until ON finance.wolverine_incoming_envelopes USING btree (keep_until) WHERE ((status)::text = 'Handled'::text);

CREATE INDEX idx_wolverine_incoming_envelopes_owner ON finance.wolverine_incoming_envelopes USING btree (owner_id) WHERE (owner_id <> 0);

CREATE INDEX idx_wolverine_incoming_envelopes_recover ON finance.wolverine_incoming_envelopes USING btree (received_at) WHERE (((status)::text = 'Incoming'::text) AND (owner_id = 0));

CREATE INDEX idx_wolverine_outgoing_envelopes_owner ON finance.wolverine_outgoing_envelopes USING btree (owner_id) WHERE (owner_id <> 0);

CREATE INDEX idx_wolverine_outgoing_envelopes_recover ON finance.wolverine_outgoing_envelopes USING btree (destination) WHERE (owner_id = 0);

CREATE INDEX ix_financial_audit_correlation ON finance.financial_audit_log USING btree (correlation_id) WHERE (correlation_id IS NOT NULL);

CREATE INDEX ix_financial_audit_entity ON finance.financial_audit_log USING btree ("EntityType", "EntityId");

CREATE INDEX ix_financial_audit_external_txn ON finance.financial_audit_log USING btree (external_transaction_id) WHERE (external_transaction_id IS NOT NULL);

CREATE INDEX ix_financial_audit_occurred_at ON finance.financial_audit_log USING btree ("OccurredAt");

CREATE INDEX ix_financial_audit_type ON finance.financial_audit_log USING btree ("AuditType");

CREATE UNIQUE INDEX ix_pii_vault_entries_profile_id ON finance.pii_vault_entries USING btree ("ProfileId");

CREATE UNIQUE INDEX ix_pii_vault_entries_tenant_email_index ON finance.pii_vault_entries USING btree ("TenantId", "EmailBlindIndex");

CREATE UNIQUE INDEX ix_pii_vault_entries_tenant_phone_index ON finance.pii_vault_entries USING btree ("TenantId", "PhoneBlindIndex");

CREATE INDEX ix_pii_vault_entries_user_id ON finance.pii_vault_entries USING btree ("UserId");

CREATE INDEX ix_processed_webhook_events_payment_intent_id ON finance.processed_webhook_events USING btree ("PaymentIntentId");

CREATE INDEX ix_processed_webhook_events_received_at ON finance.processed_webhook_events USING btree ("ReceivedAt");

CREATE UNIQUE INDEX ix_processed_webhook_events_stripe_event_id ON finance.processed_webhook_events USING btree ("StripeEventId");

CREATE INDEX "IX_stock_reservations_order_id" ON inventory.stock_reservations USING btree (order_id);

CREATE INDEX "IX_stock_reservations_status_expires_at" ON inventory.stock_reservations USING btree (status, expires_at) WHERE ((status)::text = 'Active'::text);

CREATE INDEX "IX_stock_reservations_status_updated_at" ON inventory.stock_reservations USING btree (status, updated_at) WHERE ((status)::text = 'PendingPayment'::text);

CREATE INDEX "IX_stock_reservations_stock_id" ON inventory.stock_reservations USING btree (stock_id);

CREATE UNIQUE INDEX "IX_stocks_product_id" ON inventory.stocks USING btree (product_id);

CREATE UNIQUE INDEX "IX_stocks_sku" ON inventory.stocks USING btree (sku);

CREATE INDEX idx_wolverine_incoming_envelopes_keep_until ON inventory.wolverine_incoming_envelopes USING btree (keep_until) WHERE ((status)::text = 'Handled'::text);

CREATE INDEX idx_wolverine_incoming_envelopes_owner ON inventory.wolverine_incoming_envelopes USING btree (owner_id) WHERE (owner_id <> 0);

CREATE INDEX idx_wolverine_incoming_envelopes_recover ON inventory.wolverine_incoming_envelopes USING btree (received_at) WHERE (((status)::text = 'Incoming'::text) AND (owner_id = 0));

CREATE INDEX idx_wolverine_outgoing_envelopes_owner ON inventory.wolverine_outgoing_envelopes USING btree (owner_id) WHERE (owner_id <> 0);

CREATE INDEX idx_wolverine_outgoing_envelopes_recover ON inventory.wolverine_outgoing_envelopes USING btree (destination) WHERE (owner_id = 0);

CREATE INDEX "IX_Orders_Status_CreatedAt" ON ordering.orders USING btree (status, created_at);

CREATE INDEX "IX_Orders_TenantId" ON ordering.orders USING btree (tenant_id);

CREATE INDEX "IX_order_items_order_id" ON ordering.order_items USING btree (order_id);

CREATE INDEX "IX_orders_customer_id" ON ordering.orders USING btree (customer_id);

CREATE UNIQUE INDEX "IX_orders_idempotency_key_customer_id" ON ordering.orders USING btree (idempotency_key, customer_id) WHERE (idempotency_key IS NOT NULL);

CREATE UNIQUE INDEX "IX_orders_order_number" ON ordering.orders USING btree (order_number);

CREATE INDEX idx_wolverine_incoming_envelopes_keep_until ON ordering.wolverine_incoming_envelopes USING btree (keep_until) WHERE ((status)::text = 'Handled'::text);

CREATE INDEX idx_wolverine_incoming_envelopes_owner ON ordering.wolverine_incoming_envelopes USING btree (owner_id) WHERE (owner_id <> 0);

CREATE INDEX idx_wolverine_incoming_envelopes_recover ON ordering.wolverine_incoming_envelopes USING btree (received_at) WHERE (((status)::text = 'Incoming'::text) AND (owner_id = 0));

CREATE INDEX idx_wolverine_outgoing_envelopes_owner ON ordering.wolverine_outgoing_envelopes USING btree (owner_id) WHERE (owner_id <> 0);

CREATE INDEX idx_wolverine_outgoing_envelopes_recover ON ordering.wolverine_outgoing_envelopes USING btree (destination) WHERE (owner_id = 0);

CREATE INDEX "IX_payment_transactions_external_transaction_id" ON payments.payment_transactions USING btree (external_transaction_id) WHERE (external_transaction_id IS NOT NULL);

CREATE UNIQUE INDEX "IX_payment_transactions_idempotency_key" ON payments.payment_transactions USING btree (idempotency_key) WHERE (idempotency_key IS NOT NULL);

CREATE INDEX "IX_payment_transactions_order_id" ON payments.payment_transactions USING btree (order_id);

CREATE INDEX idx_wolverine_incoming_envelopes_keep_until ON payments.wolverine_incoming_envelopes USING btree (keep_until) WHERE ((status)::text = 'Handled'::text);

CREATE INDEX idx_wolverine_incoming_envelopes_owner ON payments.wolverine_incoming_envelopes USING btree (owner_id) WHERE (owner_id <> 0);

CREATE INDEX idx_wolverine_incoming_envelopes_recover ON payments.wolverine_incoming_envelopes USING btree (received_at) WHERE (((status)::text = 'Incoming'::text) AND (owner_id = 0));

CREATE INDEX idx_wolverine_outgoing_envelopes_owner ON payments.wolverine_outgoing_envelopes USING btree (owner_id) WHERE (owner_id <> 0);

CREATE INDEX idx_wolverine_outgoing_envelopes_recover ON payments.wolverine_outgoing_envelopes USING btree (destination) WHERE (owner_id = 0);

CREATE INDEX "IX_shipments_CourierProvider" ON shipping.shipments USING btree ("CourierProvider");

CREATE UNIQUE INDEX "IX_shipments_OrderId" ON shipping.shipments USING btree ("OrderId");

CREATE INDEX "IX_shipments_Status" ON shipping.shipments USING btree ("Status");

CREATE INDEX "IX_shipments_Status_CreatedAt" ON shipping.shipments USING btree ("Status", "CreatedAt");

CREATE UNIQUE INDEX "IX_shipments_TrackingNumber" ON shipping.shipments USING btree ("TrackingNumber");

CREATE INDEX idx_wolverine_incoming_envelopes_keep_until ON shipping.wolverine_incoming_envelopes USING btree (keep_until) WHERE ((status)::text = 'Handled'::text);

CREATE INDEX idx_wolverine_incoming_envelopes_owner ON shipping.wolverine_incoming_envelopes USING btree (owner_id) WHERE (owner_id <> 0);

CREATE INDEX idx_wolverine_incoming_envelopes_recover ON shipping.wolverine_incoming_envelopes USING btree (received_at) WHERE (((status)::text = 'Incoming'::text) AND (owner_id = 0));

CREATE INDEX idx_wolverine_outgoing_envelopes_owner ON shipping.wolverine_outgoing_envelopes USING btree (owner_id) WHERE (owner_id <> 0);

CREATE INDEX idx_wolverine_outgoing_envelopes_recover ON shipping.wolverine_outgoing_envelopes USING btree (destination) WHERE (owner_id = 0);

ALTER TABLE ONLY catalog.categories
    ADD CONSTRAINT "FK_categories_categories_ParentCategoryId" FOREIGN KEY ("ParentCategoryId") REFERENCES catalog.categories("Id") ON DELETE RESTRICT;

ALTER TABLE ONLY catalog.product_attributes
    ADD CONSTRAINT "FK_product_attributes_products_ProductId" FOREIGN KEY ("ProductId") REFERENCES catalog.products("Id") ON DELETE CASCADE;

ALTER TABLE ONLY catalog.product_images
    ADD CONSTRAINT "FK_product_images_products_ProductId" FOREIGN KEY ("ProductId") REFERENCES catalog.products("Id") ON DELETE CASCADE;

ALTER TABLE ONLY finance."ReconciliationDiscrepancies"
    ADD CONSTRAINT "FK_ReconciliationDiscrepancies_ReconciliationSessions_Reconcil~" FOREIGN KEY ("ReconciliationSessionId") REFERENCES finance."ReconciliationSessions"("Id") ON DELETE CASCADE;

ALTER TABLE ONLY inventory.stock_reservations
    ADD CONSTRAINT "FK_stock_reservations_stocks_stock_id" FOREIGN KEY (stock_id) REFERENCES inventory.stocks(id) ON DELETE CASCADE;

ALTER TABLE ONLY ordering.order_items
    ADD CONSTRAINT "FK_order_items_orders_order_id" FOREIGN KEY (order_id) REFERENCES ordering.orders(id) ON DELETE CASCADE;
