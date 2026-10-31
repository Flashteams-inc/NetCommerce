package com.netcommerce.kernel.persistence;

import java.time.Instant;

import org.springframework.beans.factory.ObjectProvider;

import com.netcommerce.kernel.core.application.TenantContext;
import com.netcommerce.kernel.core.domain.AuditableEntity;
import com.netcommerce.kernel.core.domain.MultiTenant;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * Runs on every entity (META-INF/orm.xml), as AuditInterceptor and
 * TenantSaveInterceptor do on every save: a new auditable entity gets its
 * creation time and a changed one its modification time; a new tenant-scoped
 * entity without a tenant gets the current one.
 */
public class KernelEntityListener {

    private final ObjectProvider<TenantContext> tenantContext;

    public KernelEntityListener(ObjectProvider<TenantContext> tenantContext) {
        this.tenantContext = tenantContext;
    }

    @PrePersist
    void beforeInsert(Object entity) {
        if (entity instanceof AuditableEntity auditable) {
            auditable.setCreatedAt(Instant.now());
        }
        if (entity instanceof MultiTenant tenantScoped && (tenantScoped.getTenantId() == null || tenantScoped.getTenantId().isEmpty())) {
            TenantContext context = tenantContext.getIfAvailable();
            if (context != null && context.hasTenant()) {
                tenantScoped.setTenantId(context.getTenantId());
            }
        }
    }

    @PreUpdate
    void beforeUpdate(Object entity) {
        if (entity instanceof AuditableEntity auditable) {
            auditable.setModifiedAt(Instant.now());
        }
    }
}
