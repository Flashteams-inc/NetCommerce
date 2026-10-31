package com.netcommerce.kernel.persistence;

/**
 * The query filters every tenant-scoped and every soft-deletable entity
 * carries, as EF Core's global query filters (MultiTenancyExtensions): an
 * entity annotated {@code @Filter(name = KernelFilters.TENANT)} is read only for
 * the current tenant (every tenant's rows when there is none), one annotated
 * {@code @Filter(name = KernelFilters.NOT_DELETED)} only while not deleted. Both
 * are always on.
 */
public final class KernelFilters {

    public static final String TENANT = "kernelTenant";
    public static final String TENANT_PARAMETER = "currentTenantId";
    public static final String NOT_DELETED = "kernelNotDeleted";

    private KernelFilters() {
    }
}
