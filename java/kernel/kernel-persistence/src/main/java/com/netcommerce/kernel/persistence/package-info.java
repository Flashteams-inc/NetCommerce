/**
 * The persistence base the bounded contexts build on, in place of
 * NetCommerce.Kernel.EfCore: the domain base classes mapped for JPA
 * (META-INF/orm.xml), EF Core's column naming (EfCoreNamingStrategy), strongly
 * typed ids as uuid columns (StronglyTypedIdTypes), repositories with specifications, and resilient
 * transactions. Each context maps its entities into its own PostgreSQL schema
 * (@Table(schema = "catalog"), ...), as each DbContext has its own schema.
 * Hibernate validates every mapping against the existing schema at startup
 * (ddl-auto=validate); the connection pool matches NpgsqlPoolingExtensions.
 */
package com.netcommerce.kernel.persistence;
