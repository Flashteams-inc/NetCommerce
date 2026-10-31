package com.netcommerce.kernel.core.domain;

import java.time.Instant;

/** An entity that records when it was created and last modified (IAuditableEntity). */
public interface AuditableEntity {

    Instant getCreatedAt();

    void setCreatedAt(Instant createdAt);

    Instant getModifiedAt();

    void setModifiedAt(Instant modifiedAt);
}
