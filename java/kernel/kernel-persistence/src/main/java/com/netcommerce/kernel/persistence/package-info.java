/**
 * The persistence base the bounded contexts build on, in place of
 * NetCommerce.Kernel.EfCore: the domain base classes mapped for JPA
 * (META-INF/orm.xml), EF Core's column naming (EfCoreNamingStrategy), strongly
 * typed ids as uuid columns (StronglyTypedIdTypes), repositories with
 * specifications, resilient transactions, the audit columns, tenant stamping and
 * soft deletion (KernelEntityListener, SoftDeletion), the tenant and soft-delete
 * query filters declared here, which every tenant-scoped or soft-deletable
 * entity carries (KernelMappingCheck refuses one that does not), PII
 * encryption (PiiEncryptionConverter) and the audit trail (JpaAuditRepository).
 * Each context maps its entities into its own PostgreSQL schema
 * (@Table(schema = "catalog"), ...), as each DbContext has its own schema.
 * Hibernate validates every mapping against the existing schema at startup
 * (ddl-auto=validate); the connection pool matches NpgsqlPoolingExtensions.
 */
@FilterDef(name = KernelFilters.TENANT,
        defaultCondition = "(:" + KernelFilters.TENANT_PARAMETER + " = '' or \"TenantId\" = :" + KernelFilters.TENANT_PARAMETER + ")",
        parameters = @ParamDef(name = KernelFilters.TENANT_PARAMETER, type = String.class, resolver = CurrentTenantParameter.class),
        autoEnabled = true, applyToLoadByKey = true)
@FilterDef(name = KernelFilters.NOT_DELETED, defaultCondition = "\"DeletedAt\" is null",
        autoEnabled = true, applyToLoadByKey = true)
package com.netcommerce.kernel.persistence;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
