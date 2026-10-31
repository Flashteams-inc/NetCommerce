package com.netcommerce.kernel.compliance.audit;

import java.time.Instant;
import java.util.UUID;

import com.netcommerce.kernel.core.ArgumentException;

/** One entry of the audit trail: who did what to which resource, and in what context. */
public record AuditEntry(UUID id, Instant timestamp, String userId, String userRole, String action, String resourceId,
                         String module, String context, String correlationId, String ipAddress, String userAgent) {

    public static AuditEntry create(String userId, String userRole, String action, String resourceId, String module,
                                    String context, String correlationId, String ipAddress, String userAgent) {
        if (userId == null || userId.isBlank()) {
            throw new ArgumentException("UserId is required for audit trail", "userId");
        }
        if (action == null || action.isBlank()) {
            throw new ArgumentException("Action is required for audit trail", "action");
        }
        if (resourceId == null || resourceId.isBlank()) {
            throw new ArgumentException("ResourceId is required for audit trail", "resourceId");
        }
        if (module == null || module.isBlank()) {
            throw new ArgumentException("Module is required for audit trail", "module");
        }
        return new AuditEntry(UUID.randomUUID(), Instant.now(), userId, userRole, action, resourceId, module,
                context, correlationId, ipAddress, userAgent);
    }
}
