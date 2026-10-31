package com.netcommerce.kernel.persistence;

import java.nio.file.Path;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

/**
 * A PostgreSQL holding schemas exactly as the .NET migrations create them:
 * src/test/resources/schema/{catalog,finance}.sql are `dotnet ef migrations
 * script` for CatalogDbContext and FinanceDbContext, run with psql as written
 * (DO blocks included), then kernel-test.sql for the tests' own tables.
 */
public final class EfCoreSchema {

    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17.6-alpine");

    static {
        POSTGRES.start();
        for (String script : new String[] {"catalog.sql", "finance.sql", "kernel-test.sql"}) {
            POSTGRES.copyFileToContainer(MountableFile.forHostPath(Path.of("src/test/resources/schema/" + script)), "/tmp/" + script);
            try {
                var result = POSTGRES.execInContainer("psql", "-v", "ON_ERROR_STOP=1", "-U", POSTGRES.getUsername(),
                        "-d", POSTGRES.getDatabaseName(), "-f", "/tmp/" + script);
                if (result.getExitCode() != 0) {
                    throw new IllegalStateException(script + " did not load: " + result.getStderr());
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private EfCoreSchema() {
    }

    public static String[] properties() {
        return new String[] {"spring.datasource.url=" + POSTGRES.getJdbcUrl(), "spring.datasource.username=" + POSTGRES.getUsername(),
                "spring.datasource.password=" + POSTGRES.getPassword(), "spring.main.web-application-type=none"};
    }
}
