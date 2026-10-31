package com.netcommerce.kernel.compliance.audit;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netcommerce.kernel.core.application.UserContext;

/**
 * Creates and stores audit entries (AuditService.cs). An auditable command is
 * recorded as "{module}.{action}", its action the command's type name without
 * "Command" or "Query", its context the command as compact camel-case JSON.
 */
public class AuditService {

    private final AuditRepository auditRepository;
    private final UserContext userContext;
    private final ObjectMapper json;

    public AuditService(AuditRepository auditRepository, UserContext userContext, ObjectMapper json) {
        this.auditRepository = auditRepository;
        this.userContext = userContext;
        this.json = json;
    }

    public void audit(AuditableCommand command, String correlationId) {
        String actionName = command.getClass().getSimpleName().replace("Command", "").replace("Query", "");
        store(command.module() + "." + actionName, command.resourceId(), command.module(), toJson(command), correlationId);
    }

    public void audit(String action, String resourceId, String module, Object context, String correlationId) {
        store(action, resourceId, module, context != null ? toJson(context) : "{}", correlationId);
    }

    private void store(String action, String resourceId, String module, String contextJson, String correlationId) {
        auditRepository.store(AuditEntry.create(
                userContext.getUserId(),
                String.join(",", userContext.getRoles()),
                action,
                resourceId,
                module,
                contextJson,
                correlationId != null ? correlationId : UUID.randomUUID().toString(),
                userContext.getClaim("ip_address"),
                userContext.getClaim("user_agent")));
    }

    private String toJson(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialise the audit context of " + value.getClass().getSimpleName(), e);
        }
    }
}
