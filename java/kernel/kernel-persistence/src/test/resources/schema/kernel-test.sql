-- Tables the kernel tests need beside the EF Core schemas. audit_logs is
-- AuditLogConfiguration's mapping (no context's migrations create it); the
-- other two hold test entities for the audit times and PII encryption.
CREATE TABLE audit_logs (
    "Id" uuid NOT NULL PRIMARY KEY,
    "timestamp" timestamp with time zone NOT NULL,
    user_id character varying(100) NOT NULL,
    user_role character varying(50) NOT NULL,
    action character varying(200) NOT NULL,
    resource_id character varying(100) NOT NULL,
    module character varying(50) NOT NULL,
    context jsonb NOT NULL,
    correlation_id character varying(100) NOT NULL,
    ip_address character varying(45),
    user_agent character varying(500)
);
CREATE INDEX ix_audit_logs_resource_id ON audit_logs (resource_id);
CREATE TABLE notes ("Id" uuid PRIMARY KEY, "Text" text NOT NULL, "CreatedAt" timestamp with time zone NOT NULL, "ModifiedAt" timestamp with time zone);
CREATE TABLE customers ("Id" uuid PRIMARY KEY, "Email" text NOT NULL, "FullName" text NOT NULL);
