package com.netcommerce.kernel.persistence.audit;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.netcommerce.kernel.compliance.audit.AuditEntry;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** One row of audit_logs, mapped as AuditLogConfiguration maps AuditEntry. Rows are never changed. */
@jakarta.persistence.Entity
@Table(name = "audit_logs")
public class AuditLogRecord {

    @Id
    private UUID id;
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;
    @Column(name = "user_role", nullable = false, length = 50)
    private String userRole;
    @Column(name = "action", nullable = false, length = 200)
    private String action;
    @Column(name = "resource_id", nullable = false, length = 100)
    private String resourceId;
    @Column(name = "module", nullable = false, length = 50)
    private String module;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context", nullable = false, columnDefinition = "jsonb")
    private String context;
    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    protected AuditLogRecord() {
    }

    static AuditLogRecord of(AuditEntry e) {
        AuditLogRecord r = new AuditLogRecord();
        r.id = e.id();
        r.timestamp = e.timestamp();
        r.userId = e.userId();
        r.userRole = e.userRole();
        r.action = e.action();
        r.resourceId = e.resourceId();
        r.module = e.module();
        r.context = e.context();
        r.correlationId = e.correlationId();
        r.ipAddress = e.ipAddress();
        r.userAgent = e.userAgent();
        return r;
    }

    AuditEntry toEntry() {
        return new AuditEntry(id, timestamp, userId, userRole, action, resourceId, module, context, correlationId,
                ipAddress, userAgent);
    }
}
