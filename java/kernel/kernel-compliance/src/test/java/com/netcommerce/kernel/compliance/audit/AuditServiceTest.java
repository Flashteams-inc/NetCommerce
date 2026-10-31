package com.netcommerce.kernel.compliance.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netcommerce.kernel.core.application.UserContext;

/** An audited command is recorded as AuditService.cs records it. */
class AuditServiceTest {

    record UpdatePriceCommand(String productId, String newPrice) implements AuditableCommand {
        @Override
        public String module() {
            return "Catalog";
        }

        @Override
        public String resourceId() {
            return productId;
        }
    }

    private final List<AuditEntry> stored = new ArrayList<>();
    private final AuditRepository repository = new AuditRepository() {
        @Override
        public void store(AuditEntry entry) {
            stored.add(entry);
        }

        @Override
        public List<AuditEntry> getTimeline(String resourceId, String module) {
            return List.of();
        }

        @Override
        public List<AuditEntry> query(Instant start, Instant end, String userId, String module, String action, int limit) {
            return List.of();
        }
    };
    private final UserContext user = new UserContext() {
        private final Map<String, String> claims = Map.of("ip_address", "10.0.0.7", "user_agent", "probe/1.0");

        public String getUserId() { return "user-1"; }
        public String getTenantId() { return "tenant-a"; }
        public boolean isAuthenticated() { return true; }
        public String getClaim(String type) { return claims.get(type); }
        public Collection<String> getRoles() { return List.of("admin", "catalog-editor"); }
        public boolean isInRole(String role) { return getRoles().contains(role); }
    };

    @Test
    void aCommandIsRecordedUnderItsModuleAndActionWithItsContext() {
        new AuditService(repository, user, new ObjectMapper()).audit(new UpdatePriceCommand("p-42", "19.99"), "corr-1");
        AuditEntry entry = stored.get(0);
        assertThat(entry.action()).isEqualTo("Catalog.UpdatePrice");
        assertThat(entry.module()).isEqualTo("Catalog");
        assertThat(entry.resourceId()).isEqualTo("p-42");
        assertThat(entry.context()).isEqualTo("{\"productId\":\"p-42\",\"newPrice\":\"19.99\"}");
        assertThat(entry.userId()).isEqualTo("user-1");
        assertThat(entry.userRole()).isEqualTo("admin,catalog-editor");
        assertThat(entry.correlationId()).isEqualTo("corr-1");
        assertThat(entry.ipAddress()).isEqualTo("10.0.0.7");
        assertThat(entry.userAgent()).isEqualTo("probe/1.0");
    }

    @Test
    void aCustomEntryWithoutContextOrCorrelationGetsDefaults() {
        new AuditService(repository, user, new ObjectMapper()).audit("Orders.Export", "batch-9", "Ordering", null, null);
        AuditEntry entry = stored.get(0);
        assertThat(entry.context()).isEqualTo("{}");
        assertThat(entry.correlationId()).hasSize(36);
    }
}
