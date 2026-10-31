-- V3: the audit trail's table, as the .NET AuditLogConfiguration maps it. The
-- .NET code configures this table but no context's migrations create it, so
-- its audit trail had nowhere to write; the Java kernel's audit repository
-- (kernel-persistence JpaAuditRepository) writes here. Rows are only ever added.
CREATE TABLE IF NOT EXISTS public.audit_logs (
    "Id" uuid NOT NULL,
    "timestamp" timestamp with time zone NOT NULL,
    user_id character varying(100) NOT NULL,
    user_role character varying(50) NOT NULL,
    action character varying(200) NOT NULL,
    resource_id character varying(100) NOT NULL,
    module character varying(50) NOT NULL,
    context jsonb NOT NULL,
    correlation_id character varying(100) NOT NULL,
    ip_address character varying(45),
    user_agent character varying(500),
    CONSTRAINT "PK_audit_logs" PRIMARY KEY ("Id")
);
COMMENT ON TABLE public.audit_logs IS 'Immutable business event store for legal compliance. NO UPDATE or DELETE permissions for application user.';
COMMENT ON COLUMN public.audit_logs."timestamp" IS 'UTC timestamp when the action occurred.';
CREATE INDEX IF NOT EXISTS ix_audit_logs_resource_id ON public.audit_logs (resource_id);
CREATE INDEX IF NOT EXISTS ix_audit_logs_module ON public.audit_logs (module);
CREATE INDEX IF NOT EXISTS ix_audit_logs_timestamp ON public.audit_logs ("timestamp");
CREATE INDEX IF NOT EXISTS ix_audit_logs_module_action ON public.audit_logs (module, action);
CREATE INDEX IF NOT EXISTS ix_audit_logs_resource_module ON public.audit_logs (resource_id, module);
