package com.netcommerce.kernel.core.domain;

/** An entity that belongs to one tenant; queries see only the current tenant's rows. */
public interface MultiTenant {

    String getTenantId();

    void setTenantId(String tenantId);
}
