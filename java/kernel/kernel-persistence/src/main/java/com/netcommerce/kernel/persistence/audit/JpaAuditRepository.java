package com.netcommerce.kernel.persistence.audit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.netcommerce.kernel.compliance.audit.AuditEntry;
import com.netcommerce.kernel.compliance.audit.AuditRepository;

/**
 * The audit trail over audit_logs, as AuditRepository.cs: entries are only ever
 * added; a resource's timeline reads oldest first; a query filters by any of
 * time range, user, module and action, newest first, up to a limit.
 */
public class JpaAuditRepository implements AuditRepository {

    private final AuditLogRecords records;

    public JpaAuditRepository(AuditLogRecords records) {
        this.records = records;
    }

    @Override
    public void store(AuditEntry entry) {
        records.save(AuditLogRecord.of(entry));
    }

    @Override
    public List<AuditEntry> getTimeline(String resourceId, String module) {
        Specification<AuditLogRecord> spec = equal("resourceId", resourceId);
        if (module != null && !module.isEmpty()) {
            spec = spec.and(equal("module", module));
        }
        return records.findAll(spec, Sort.by("timestamp")).stream().map(AuditLogRecord::toEntry).toList();
    }

    @Override
    public List<AuditEntry> query(Instant startDate, Instant endDate, String userId, String module, String action, int limit) {
        List<Specification<AuditLogRecord>> parts = new ArrayList<>();
        if (startDate != null) {
            parts.add((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), startDate));
        }
        if (endDate != null) {
            parts.add((root, q, cb) -> cb.lessThanOrEqualTo(root.get("timestamp"), endDate));
        }
        if (userId != null && !userId.isEmpty()) {
            parts.add(equal("userId", userId));
        }
        if (module != null && !module.isEmpty()) {
            parts.add(equal("module", module));
        }
        if (action != null && !action.isEmpty()) {
            parts.add(equal("action", action));
        }
        Specification<AuditLogRecord> spec = Specification.allOf(parts);
        return records.findAll(spec, PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp")))
                .stream().map(AuditLogRecord::toEntry).toList();
    }

    private static Specification<AuditLogRecord> equal(String attribute, String value) {
        return (root, q, cb) -> cb.equal(root.get(attribute), value);
    }
}
