package com.netcommerce.kernel.compliance.pii;

import java.time.Instant;
import java.util.UUID;

import com.netcommerce.kernel.core.domain.Entity;
import com.netcommerce.kernel.core.domain.MultiTenant;
import com.netcommerce.kernel.core.domain.SoftDelete;

/** What every PII vault entry has: owner, tenant, key version, audit times and soft deletion. */
public abstract class PiiVaultEntryBase extends Entity<UUID> implements MultiTenant, SoftDelete {

    protected UUID profileId;
    protected String userId = "";
    protected String tenantId = "";
    protected String deletedBy;
    protected Instant createdAt;
    protected Instant updatedAt;
    protected Instant lastAccessedAt;
    protected int keyVersion;
    protected boolean deleted;
    protected Instant deletedAt;

    protected PiiVaultEntryBase() {
    }

    protected PiiVaultEntryBase(UUID profileId, String userId) {
        super(UUID.randomUUID());
        Instant now = Instant.now();
        this.profileId = profileId;
        this.userId = userId;
        this.createdAt = now;
        this.updatedAt = now;
        this.lastAccessedAt = now;
    }

    public UUID getProfileId() {
        return profileId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String getDeletedBy() {
        return deletedBy;
    }

    @Override
    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastAccessedAt() {
        return lastAccessedAt;
    }

    public int getKeyVersion() {
        return keyVersion;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public Instant getDeletedAt() {
        return deletedAt;
    }

    @Override
    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void recordAccess() {
        lastAccessedAt = Instant.now();
    }

    public void markAsDeleted() {
        softDelete("system");
    }

    @Override
    public void softDelete(String deletedBy) {
        deleted = true;
        deletedAt = Instant.now();
        this.deletedBy = deletedBy;
        updatedAt = Instant.now();
    }

    @Override
    public void restore() {
        deleted = false;
        deletedAt = null;
        deletedBy = null;
        updatedAt = Instant.now();
    }

    public void updateKeyVersion(int newVersion) {
        keyVersion = newVersion;
        updatedAt = Instant.now();
    }

    protected void markAsUpdated() {
        updatedAt = Instant.now();
    }
}
