package com.netcommerce.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import com.netcommerce.NetCommerceApplication;

/**
 * Flyway takes the schema over from the EF Core migrations. A database the
 * .NET migrations built (src/test/resources/schema/*.sql, `dotnet ef
 * migrations script` for each context, run with psql as written) is marked
 * baselined at version 1 and gets only the later versions; an empty one is
 * built from V1. Both end with the same schema, and the app starts on each
 * with ddl-auto=validate.
 */
class FlywayBaselineTest {

    private static final String[] CONTEXTS = {"catalog", "finance", "inventory", "ordering", "payments", "shipping"};

    private static final PostgreSQLContainer<?> EF_BUILT = new PostgreSQLContainer<>("postgres:17.6-alpine");
    private static final PostgreSQLContainer<?> EMPTY = new PostgreSQLContainer<>("postgres:17.6-alpine");

    private static String efBefore;

    @BeforeAll
    static void databases() throws Exception {
        EF_BUILT.start();
        EMPTY.start();
        for (String context : CONTEXTS) {
            EF_BUILT.copyFileToContainer(MountableFile.forHostPath(Path.of("src/test/resources/schema/" + context + ".sql")),
                    "/tmp/" + context + ".sql");
            var result = EF_BUILT.execInContainer("psql", "-v", "ON_ERROR_STOP=1", "-U", EF_BUILT.getUsername(),
                    "-d", EF_BUILT.getDatabaseName(), "-f", "/tmp/" + context + ".sql");
            assertThat(result.getExitCode()).as(context + ".sql: " + result.getStderr()).isZero();
        }
        efBefore = schema(EF_BUILT);
        startAppOn(EF_BUILT);
        startAppOn(EMPTY);
    }

    @AfterAll
    static void stop() {
        EF_BUILT.stop();
        EMPTY.stop();
    }

    @Test
    void anExistingDatabaseIsBaselinedAndGetsOnlyTheLaterVersions() throws Exception {
        assertThat(history(EF_BUILT)).containsExactly("1 BASELINE EF Core schema", "2 SQL event publication", "3 SQL audit logs");
    }

    @Test
    void anEmptyDatabaseIsBuiltFromV1() throws Exception {
        assertThat(history(EMPTY)).containsExactly("1 SQL ef core schema", "2 SQL event publication", "3 SQL audit logs");
    }

    @Test
    void bothEndWithTheSameSchema() throws Exception {
        assertThat(schema(EMPTY)).isEqualTo(schema(EF_BUILT));
    }

    @Test
    void theExistingSchemaIsLeftAsItWas() throws Exception {
        Set<String> after = Set.copyOf(schema(EF_BUILT).lines().toList());
        assertThat(efBefore.lines().filter(line -> !after.contains(line))).isEmpty();
        assertThat(tables(EF_BUILT)).containsExactlyInAnyOrderElementsOf(
                plus(tablesIn(efBefore), "public.audit_logs", "public.event_publication", "public.flyway_schema_history"));
        for (String context : CONTEXTS) {
            assertThat(query(EF_BUILT, "SELECT count(*) FROM " + context + ".\"__EFMigrationsHistory\"").get(0))
                    .as(context + " keeps its EF migration history").isNotEqualTo("0");
        }
    }

    private static void startAppOn(PostgreSQLContainer<?> db) {
        // As arguments, which outrank application.yml (builder properties are only defaults).
        new SpringApplicationBuilder(NetCommerceApplication.class).run(
                "--spring.datasource.url=" + db.getJdbcUrl(), "--spring.datasource.username=" + db.getUsername(),
                "--spring.datasource.password=" + db.getPassword(), "--spring.main.web-application-type=none")
                .close();
    }

    private static List<String> history(PostgreSQLContainer<?> db) throws Exception {
        return query(db, "SELECT version || ' ' || type || ' ' || description FROM public.flyway_schema_history"
                + " WHERE success ORDER BY installed_rank");
    }

    private static List<String> tables(PostgreSQLContainer<?> db) throws Exception {
        return query(db, "SELECT table_schema || '.' || table_name FROM information_schema.tables"
                + " WHERE table_schema NOT IN ('pg_catalog', 'information_schema')");
    }

    private static List<String> tablesIn(String schema) {
        return schema.lines().filter(line -> line.startsWith("CREATE TABLE "))
                .map(line -> line.substring("CREATE TABLE ".length(), line.indexOf(" (")).replace("\"", ""))
                .collect(Collectors.toList());
    }

    private static List<String> plus(List<String> list, String... more) {
        var out = new ArrayList<>(list);
        out.addAll(List.of(more));
        return out;
    }

    private static List<String> query(PostgreSQLContainer<?> db, String sql) throws Exception {
        try (var c = DriverManager.getConnection(db.getJdbcUrl(), db.getUsername(), db.getPassword());
                var rs = c.createStatement().executeQuery(sql)) {
            var out = new ArrayList<String>();
            while (rs.next()) {
                out.add(rs.getString(1));
            }
            return out;
        }
    }

    /** The schema as pg_dump writes it, without owners or the dump's own session lines. */
    private static String schema(PostgreSQLContainer<?> db) throws Exception {
        var result = db.execInContainer("pg_dump", "-U", db.getUsername(), "-d", db.getDatabaseName(), "--schema-only", "--no-owner");
        assertThat(result.getExitCode()).as(result.getStderr()).isZero();
        return result.getStdout().lines().filter(line -> !line.startsWith("\\restrict") && !line.startsWith("\\unrestrict")
                && !line.startsWith("-- Dumped")).collect(Collectors.joining("\n"));
    }
}
