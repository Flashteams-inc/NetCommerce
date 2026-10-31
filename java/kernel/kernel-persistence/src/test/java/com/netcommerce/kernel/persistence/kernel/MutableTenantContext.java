package com.netcommerce.kernel.persistence.kernel;

import com.netcommerce.kernel.core.application.TenantContext;

/** A tenant context the tests set. */
public class MutableTenantContext implements TenantContext {

    private String tenantId;

    @Override
    public String getTenantId() {
        return tenantId;
    }

    public void set(String tenantId) {
        this.tenantId = tenantId;
    }
}
