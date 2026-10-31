package com.netcommerce.kernel.persistence.kernel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import com.netcommerce.brokenmappings.tenant.UnfilteredTenantApplication;
import com.netcommerce.kernel.compliance.audit.AuditEntry;
import com.netcommerce.kernel.compliance.audit.AuditRepository;
import com.netcommerce.kernel.compliance.encryption.CryptoProvider;
import com.netcommerce.kernel.core.encryption.EncryptedData;
import com.netcommerce.kernel.persistence.EfCoreSchema;

import jakarta.persistence.EntityManager;

/**
 * The behaviours EF Core's interceptors, query filters and converters gave
 * every entity, against the finance schema the .NET migrations create.
 * Ciphertexts marked .NET were produced by the .NET DevelopmentCryptoProvider.
 */
@SpringBootTest(classes = KernelTestApplication.class)
class KernelBehavioursTest {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", EfCoreSchema.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", EfCoreSchema.POSTGRES::getUsername);
        registry.add("spring.datasource.password", EfCoreSchema.POSTGRES::getPassword);
    }

    @Autowired private EntityManager em;
    @Autowired private TransactionTemplate tx;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private MutableTenantContext tenant;
    @Autowired private CryptoProvider crypto;
    @Autowired private AuditRepository audit;

    @BeforeEach
    @AfterEach
    void clean() {
        tenant.set(null);
        jdbc.update("DELETE FROM finance.pii_vault_entries");
        jdbc.update("DELETE FROM notes");
        jdbc.update("DELETE FROM customers");
        jdbc.update("DELETE FROM audit_logs");
    }

    private void save(Object entity) {
        tx.executeWithoutResult(s -> em.persist(entity));
    }

    @Test
    void readsSeeOnlyTheCurrentTenantsRowsAndNewRowsGetIt() {
        save(new VaultEntry("user-a", "tenant-a"));
        save(new VaultEntry("user-b", "tenant-b"));
        tenant.set("tenant-a");
        List<VaultEntry> seen = tx.execute(s -> em.createQuery("select v from VaultEntry v", VaultEntry.class).getResultList());
        assertThat(seen).extracting(VaultEntry::getUserId).containsExactly("user-a");
        save(new VaultEntry("user-new", null));
        assertThat(jdbc.queryForObject("SELECT \"TenantId\" FROM finance.pii_vault_entries WHERE \"UserId\" = 'user-new'", String.class))
                .isEqualTo("tenant-a");
        tenant.set(null);
        List<VaultEntry> everyTenant = tx.execute(s -> em.createQuery("select v from VaultEntry v", VaultEntry.class).getResultList());
        assertThat(everyTenant).hasSize(3);
    }

    @Test
    void deletingASoftDeletableEntityMarksItAndHidesItFromDefaultReads() {
        VaultEntry entry = new VaultEntry("user-a", "tenant-a");
        save(entry);
        tx.executeWithoutResult(s -> em.remove(em.find(VaultEntry.class, entry.getId())));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM finance.pii_vault_entries WHERE \"DeletedAt\" IS NOT NULL", Integer.class)).isEqualTo(1);
        List<VaultEntry> visible = tx.execute(s -> em.createQuery("select v from VaultEntry v", VaultEntry.class).getResultList());
        assertThat(visible).isEmpty();
        VaultEntry byId = tx.execute(s -> em.find(VaultEntry.class, entry.getId()));
        assertThat(byId).isNull();
    }

    @Test
    void anAuditableEntityGetsItsCreationAndModificationTimes() {
        Note note = new Note("first");
        Instant before = Instant.now();
        save(note);
        assertThat(note.getCreatedAt()).isAfterOrEqualTo(before);
        assertThat(note.getModifiedAt()).isNull();
        tx.executeWithoutResult(s -> em.find(Note.class, note.getId()).setText("second"));
        assertThat(jdbc.queryForObject("SELECT \"ModifiedAt\" IS NOT NULL FROM notes", Boolean.class)).isTrue();
    }

    @Test
    void piiWrittenByDotNetReadsInJava() {
        // .NET, deterministic and random-IV, from the DevelopmentCryptoProvider:
        assertThat(crypto.decrypt(EncryptedData.fromStorageFormat(
                "v1|aAES-256-GCM:1|kdev-crypto-v1|ATMsh2UYp5O3wbjfr21LQA==|vTQZL8I0W6YLZFNqbucIEQ==|"))).isEqualTo("Jane Doe");
        assertThat(crypto.decrypt(EncryptedData.fromStorageFormat(
                "v1|aAES-256-GCM:1|kdev-crypto-v1|zPnzkguo4+I5uUCI8oTpnA==|ttRCP+jfkH0WIbmihbp6buiByOvwnihYcm0w8tk8QWSYr4FIhQhNBVjUmzLfL54x|")))
                .isEqualTo("José Müller, 12 Rue de l'Église");
        // Deterministic encryption in Java is byte for byte what .NET stores, so .NET reads it too.
        assertThat(crypto.encrypt("Jane Doe", true).toStorageFormat())
                .isEqualTo("v1|aAES-256-GCM:1|kdev-crypto-v1|ATMsh2UYp5O3wbjfr21LQA==|vTQZL8I0W6YLZFNqbucIEQ==|");
        assertThat(crypto.computeBlindIndex("jane@example.com").value()).isEqualTo("Mf7K7pUnIV0TDZk7ugR9i8qKdTIBwJk7yb9oA99xF6w=");
    }

    @Test
    void piiFieldsAreStoredEncryptedAndReadBackPlain() throws Exception {
        Customer customer = new Customer("jane@example.com", "José Müller");
        save(customer);
        String storedEmail = jdbc.queryForObject("SELECT \"Email\" FROM customers", String.class);
        String storedName = jdbc.queryForObject("SELECT \"FullName\" FROM customers", String.class);
        assertThat(storedEmail).startsWith("v1|aAES-256-GCM:1|kdev-crypto-v1|").doesNotContain("jane");
        assertThat(storedEmail).isEqualTo(crypto.encrypt("jane@example.com", true).toStorageFormat());
        assertThat(storedName).startsWith("v1|").isNotEqualTo(crypto.encrypt("José Müller", false).toStorageFormat());
        em.clear();
        Customer back = tx.execute(s -> em.find(Customer.class, customer.getId()));
        assertThat(back.getEmail()).isEqualTo("jane@example.com");
        assertThat(back.getFullName()).isEqualTo("José Müller");
        // A random-IV value written by Java, for the .NET side to decrypt (cross-runtime check).
        Files.createDirectories(Path.of("target"));
        Files.writeString(Path.of("target/java-encrypted-name.txt"), storedName);
    }

    @Test
    void theAuditTrailStoresReadsInTimeOrderAndQueries() {
        Instant t = Instant.parse("2026-10-31T00:00:00Z");
        audit.store(new AuditEntry(java.util.UUID.randomUUID(), t.plusSeconds(2), "u1", "admin", "Catalog.UpdatePrice", "p-1", "Catalog", "{\"price\":20}", "c1", null, null));
        audit.store(new AuditEntry(java.util.UUID.randomUUID(), t.plusSeconds(1), "u1", "admin", "Catalog.CreateProduct", "p-1", "Catalog", "{}", "c0", null, null));
        audit.store(new AuditEntry(java.util.UUID.randomUUID(), t.plusSeconds(3), "u2", "clerk", "Ordering.Place", "o-9", "Ordering", "{}", "c2", null, null));
        assertThat(audit.getTimeline("p-1", null)).extracting(AuditEntry::action).containsExactly("Catalog.CreateProduct", "Catalog.UpdatePrice");
        assertThat(audit.query(null, null, "u1", "Catalog", null, 1)).extracting(AuditEntry::action).containsExactly("Catalog.UpdatePrice");
        assertThat(audit.query(t.plusSeconds(3), null, null, null, null, 100)).extracting(AuditEntry::resourceId).containsExactly("o-9");
    }

    @Test
    void aTenantScopedEntityWithoutTheTenantFilterRefusesToStart() {
        assertThatThrownBy(() -> new SpringApplicationBuilder(UnfilteredTenantApplication.class).properties(EfCoreSchema.properties()).run().close())
                .hasStackTraceContaining("is tenant-scoped but has no @Filter(name = KernelFilters.TENANT)");
    }
}
