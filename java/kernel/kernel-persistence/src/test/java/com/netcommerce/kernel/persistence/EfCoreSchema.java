package com.netcommerce.kernel.persistence;

import java.nio.file.Path;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

/**
 * A PostgreSQL holding the catalog schema exactly as the .NET migrations create
 * it: src/test/resources/schema/catalog.sql is `dotnet ef migrations script`
 * for CatalogDbContext, run with psql as written (its DO blocks included).
 */
final class EfCoreSchema {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17.6-alpine");

    static {
        POSTGRES.start();
        POSTGRES.copyFileToContainer(MountableFile.forHostPath(Path.of("src/test/resources/schema/catalog.sql")), "/tmp/catalog.sql");
        try {
            var result = POSTGRES.execInContainer("psql", "-v", "ON_ERROR_STOP=1", "-U", POSTGRES.getUsername(),
                    "-d", POSTGRES.getDatabaseName(), "-f", "/tmp/catalog.sql");
            if (result.getExitCode() != 0) {
                throw new IllegalStateException("The EF Core schema did not load: " + result.getStderr());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private EfCoreSchema() {
    }
}
