package com.netcommerce.kernel.persistence;

import java.util.function.Supplier;

import org.springframework.beans.factory.ObjectProvider;

import com.netcommerce.kernel.core.application.TenantContext;

/** The tenant filter's value: the current tenant, or '' (every tenant) when there is none. */
public class CurrentTenantParameter implements Supplier<String> {

    private final ObjectProvider<TenantContext> tenantContext;

    public CurrentTenantParameter(ObjectProvider<TenantContext> tenantContext) {
        this.tenantContext = tenantContext;
    }

    @Override
    public String get() {
        TenantContext context = tenantContext.getIfAvailable();
        return context != null && context.hasTenant() ? context.getTenantId() : "";
    }
}
