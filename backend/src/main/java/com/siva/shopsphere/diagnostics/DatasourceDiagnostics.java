package com.siva.shopsphere.diagnostics;

import org.springframework.core.env.Environment;

/**
 * Service to extract non-sensitive datasource diagnostic information from the Spring Environment
 * and System environment variables.
 */
public final class DatasourceDiagnostics {

    private DatasourceDiagnostics() {
    }

    /**
     * Extracts non-secret diagnostic metadata from the provided Environment.
     *
     * @param environment Spring Environment (can be null in standalone test/offline scenarios)
     * @return safe DatasourceDiagnosticInfo
     */
    public static DatasourceDiagnosticInfo extract(Environment environment) {
        boolean urlPresent = isPropertyPresent(environment, "DATABASE_URL");
        boolean usernamePresent = isPropertyPresent(environment, "DATABASE_USERNAME");
        boolean passwordPresent = isPropertyPresent(environment, "DATABASE_PASSWORD");

        // Spring resolves spring.datasource.url from ${DATABASE_URL:jdbc:postgresql://localhost:5432/shopsphere}
        String resolvedUrl = null;
        if (environment != null) {
            resolvedUrl = environment.getProperty("spring.datasource.url");
            if (resolvedUrl == null || resolvedUrl.isBlank()) {
                resolvedUrl = environment.getProperty("DATABASE_URL");
            }
        }
        if (resolvedUrl == null || resolvedUrl.isBlank()) {
            resolvedUrl = System.getenv("DATABASE_URL");
        }

        DatasourceDiagnosticParser.ParsedJdbcUrl parsed = DatasourceDiagnosticParser.parse(resolvedUrl);
        String driverVersion = DatasourceDiagnosticParser.getPostgresDriverVersion();

        return new DatasourceDiagnosticInfo(
                urlPresent,
                usernamePresent,
                passwordPresent,
                parsed.host(),
                parsed.port(),
                parsed.databaseName(),
                parsed.sslMode(),
                driverVersion
        );
    }

    private static boolean isPropertyPresent(Environment environment, String key) {
        if (environment != null) {
            String val = environment.getProperty(key);
            if (val != null && !val.isBlank()) {
                return true;
            }
        }
        String envVal = System.getenv(key);
        return envVal != null && !envVal.isBlank();
    }
}
