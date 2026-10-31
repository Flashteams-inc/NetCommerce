package com.netcommerce.brokenmappings.tenant;

import com.netcommerce.kernel.compliance.pii.PiiVaultEntryBase;

import jakarta.persistence.Table;

/** Tenant-scoped, but without the tenant filter: the kernel must refuse it. */
@jakarta.persistence.Entity
@Table(name = "pii_vault_entries", schema = "finance")
public class UnfilteredVaultEntry extends PiiVaultEntryBase {
}
