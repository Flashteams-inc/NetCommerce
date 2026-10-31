package com.netcommerce.kernel.core.application;

/** The tenant the current request acts for (ITenantContext); none outside a tenant. */
public interface TenantContext {

    /** The tenant's id; null when there is none. */
    String getTenantId();

    default boolean hasTenant() {
        String tenantId = getTenantId();
        return tenantId != null && !tenantId.isEmpty();
    }
}
