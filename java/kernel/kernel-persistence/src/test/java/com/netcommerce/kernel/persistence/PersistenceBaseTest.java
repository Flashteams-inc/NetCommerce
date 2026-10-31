package com.netcommerce.kernel.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotSerializeTransactionException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;

import com.netcommerce.brokenmappings.schema.BrokenTestApplication;
import com.netcommerce.kernel.persistence.catalog.CatalogTestApplication;
import com.netcommerce.kernel.persistence.catalog.Category;
import com.netcommerce.kernel.persistence.catalog.CategoryId;
import com.netcommerce.kernel.persistence.catalog.CategoryRepository;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManager;

/**
 * The persistence base against the catalog schema the .NET migrations create:
 * mappings are validated at startup, ids are uuid columns, specifications
 * filter, order and page, transient failures are retried, and the pool is the
 * Npgsql one.
 */
@SpringBootTest(classes = CatalogTestApplication.class)
class PersistenceBaseTest {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", EfCoreSchema.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", EfCoreSchema.POSTGRES::getUsername);
        registry.add("spring.datasource.password", EfCoreSchema.POSTGRES::getPassword);
    }

    @Autowired private CategoryRepository categories;
    @Autowired private EntityManager em;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactions;
    @Autowired private DataSource dataSource;

    @BeforeEach
    void empty() {
        jdbc.update("DELETE FROM catalog.categories");
    }

    private static Category category(String name, int order, boolean active) {
        return new Category(new CategoryId(UUID.randomUUID()), name, name.toLowerCase().replace(' ', '-'), order, active);
    }

    @Test
    void theContextStartsWithEveryMappingValidatedAgainstTheEfSchema() {
        Category saved = categories.save(category("Shoes", 1, true));
        Category loaded = categories.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("Shoes");
        // The id is stored as its bare uuid in the "Id" column EF Core created.
        assertThat(jdbc.queryForObject("SELECT \"Id\" FROM catalog.categories", UUID.class)).isEqualTo(saved.getId().value());
    }

    @Test
    void aMappingThatDoesNotMatchTheSchemaFailsAtStartup() {
        assertThatThrownBy(() -> new SpringApplicationBuilder(BrokenTestApplication.class)
                .properties(EfCoreSchema.properties())
                .run().close())
                .hasStackTraceContaining("missing column [Title]");
    }

    @Test
    void twoCombinedSpecificationsFilterThenOrderThenPage() {
        categories.saveAll(List.of(category("Sandals", 4, true), category("Shirts", 2, true), category("Socks", 3, false),
                category("Suits", 1, true), category("Hats", 5, true), category("Scarves", 6, true)));
        Specification<Category> active = (root, query, cb) -> cb.isTrue(root.get("isActive"));
        Specification<Category> startsWithS = (root, query, cb) -> cb.like(root.get("name"), "S%");
        QuerySpecification<Category> spec = QuerySpecification.where(Specification.where(active).and(startsWithS))
                .orderBy(Sort.by("displayOrder")).page(1, 2);
        // Active and starting with S, by display order: Suits(1), Shirts(2), Sandals(4), Scarves(6); skip 1, take 2.
        assertThat(SpecificationEvaluator.list(em, Category.class, spec)).extracting(Category::getName)
                .containsExactly("Shirts", "Sandals");
        assertThat(SpecificationEvaluator.count(em, Category.class, spec)).isEqualTo(4);
        assertThat(categories.findAll(Specification.where(active).and(startsWithS))).hasSize(4);
    }

    @Test
    void aTransientFailureIsRetriedAndTheSecondAttemptCommitsOneRow() {
        AtomicInteger attempts = new AtomicInteger();
        new ResilientTransaction(transactions, 5, Duration.ofMillis(50)).execute(() -> {
            categories.save(category("Retried", 1, true));
            if (attempts.incrementAndGet() == 1) {
                throw new CannotSerializeTransactionException("could not serialize access due to concurrent update");
            }
        });
        assertThat(attempts).hasValue(2);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM catalog.categories WHERE \"Name\" = 'Retried'", Integer.class)).isEqualTo(1);
    }

    @Test
    void aFailureThatIsNotTransientIsNotRetried() {
        AtomicInteger attempts = new AtomicInteger();
        assertThatThrownBy(() -> new ResilientTransaction(transactions, 5, Duration.ofMillis(50)).execute(() -> {
            attempts.incrementAndGet();
            categories.save(category("Broken", 1, true));
            throw new IllegalArgumentException("a bug, not the database");
        })).isInstanceOf(IllegalArgumentException.class);
        assertThat(attempts).hasValue(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM catalog.categories", Integer.class)).isZero();
    }

    @Test
    void thePoolMatchesNpgsqlPoolingExtensions() {
        HikariDataSource pool = (HikariDataSource) dataSource;
        assertThat(pool.getMaximumPoolSize()).isEqualTo(20);
        assertThat(pool.getMinimumIdle()).isEqualTo(2);
        assertThat(pool.getIdleTimeout()).isEqualTo(300_000);
        assertThat(pool.getConnectionTimeout()).isEqualTo(15_000);
    }
}
