package com.netcommerce.kernel.persistence.kernel;

import java.time.Instant;
import java.util.UUID;

import com.netcommerce.kernel.core.domain.AuditableEntity;
import com.netcommerce.kernel.core.domain.Entity;

import jakarta.persistence.Table;

@jakarta.persistence.Entity
@Table(name = "notes")
public class Note extends Entity<UUID> implements AuditableEntity {

    private String text;
    private Instant createdAt;
    private Instant modifiedAt;

    protected Note() {
    }

    public Note(String text) {
        super(UUID.randomUUID());
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public Instant getModifiedAt() {
        return modifiedAt;
    }

    @Override
    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
