package com.netcommerce.kernel.persistence.kernel;

import java.util.UUID;

import org.hibernate.annotations.Filter;

import com.netcommerce.kernel.compliance.pii.PiiVaultEntryBase;
import com.netcommerce.kernel.persistence.KernelFilters;

import jakarta.persistence.Table;

/** finance.pii_vault_entries as EF Core created it: tenant-scoped and soft-deletable. */
@jakarta.persistence.Entity
@Table(name = "pii_vault_entries", schema = "finance")
@Filter(name = KernelFilters.TENANT)
@Filter(name = KernelFilters.NOT_DELETED)
public class VaultEntry extends PiiVaultEntryBase {

    private String encryptedFullName = "n";
    private String encryptedEmail = "e";
    private String emailBlindIndex;
    private String encryptedPhoneNumber = "p";
    private String phoneBlindIndex;
    private String encryptedAddress = "a";

    protected VaultEntry() {
    }

    public VaultEntry(String userId, String tenantId) {
        super(UUID.randomUUID(), userId);
        this.tenantId = tenantId == null ? "" : tenantId;
        this.emailBlindIndex = UUID.randomUUID().toString();
        this.phoneBlindIndex = UUID.randomUUID().toString();
    }
}
