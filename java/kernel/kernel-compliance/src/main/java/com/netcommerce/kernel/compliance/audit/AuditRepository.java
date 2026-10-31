package com.netcommerce.kernel.compliance.audit;

import java.time.Instant;
import java.util.List;

/** Where audit entries are kept and read back. */
public interface AuditRepository {

    void store(AuditEntry entry);

    /** A resource's entries, oldest first; module null for all modules. */
    List<AuditEntry> getTimeline(String resourceId, String module);

    /** Entries matching the filters given (null for any), newest first, at most limit of them. */
    List<AuditEntry> query(Instant startDate, Instant endDate, String userId, String module, String action, int limit);
}
