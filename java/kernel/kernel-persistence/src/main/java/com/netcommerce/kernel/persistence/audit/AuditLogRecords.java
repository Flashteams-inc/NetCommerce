package com.netcommerce.kernel.persistence.audit;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Spring Data access to audit_logs, for JpaAuditRepository only. */
public interface AuditLogRecords extends JpaRepository<AuditLogRecord, UUID>, JpaSpecificationExecutor<AuditLogRecord> {
}
