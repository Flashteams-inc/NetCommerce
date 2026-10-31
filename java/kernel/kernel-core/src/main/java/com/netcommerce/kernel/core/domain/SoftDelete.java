package com.netcommerce.kernel.core.domain;

import java.time.Instant;

/** An entity that is marked deleted instead of removed; default queries leave it out. */
public interface SoftDelete {

    /** When it was deleted; null when it is not. */
    Instant getDeletedAt();

    void setDeletedAt(Instant deletedAt);

    String getDeletedBy();

    void setDeletedBy(String deletedBy);

    void softDelete(String deletedBy);

    void restore();
}
