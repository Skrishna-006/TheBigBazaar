package com.siva.shopsphere.diagnostics;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 * Startup listener that logs non-sensitive datasource diagnostic information during
 * the environment preparation phase, guaranteed to execute BEFORE HikariCP and Hibernate
 * attempt to open a database connection or execute an SSL handshake.
 */
public class DatasourceDiagnosticsListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(DatasourceDiagnosticsListener.class);
    private static final AtomicBoolean logged = new AtomicBoolean(false);

    @Override
    public int getOrder() {
        // Ordered after LoggingApplicationListener so SLF4J / Logback is fully initialized
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        if (!logged.compareAndSet(false, true)) {
            return;
        }

        DatasourceDiagnosticInfo info = DatasourceDiagnostics.extract(event.getEnvironment());
        logDiagnostics(info);
    }

    /**
     * Emits formatted diagnostic logs containing only safe, non-sensitive connection parameters.
     *
     * @param info non-secret diagnostic info
     */
    public static void logDiagnostics(DatasourceDiagnosticInfo info) {
        logger.info("================================================================================");
        logger.info("DATASOURCE STARTUP DIAGNOSTICS (NON-SECRET)");
        logger.info("  DATABASE_URL present      : {}", info.databaseUrlPresent());
        logger.info("  DATABASE_USERNAME present : {}", info.databaseUsernamePresent());
        logger.info("  DATABASE_PASSWORD present : {}", info.databasePasswordPresent());
        logger.info("  Parsed JDBC Hostname      : {}", info.host());
        logger.info("  Parsed JDBC Port          : {}", info.port());
        logger.info("  Parsed JDBC Database Name : {}", info.databaseName());
        logger.info("  Parsed JDBC SSL Mode      : {}", info.sslMode());
        logger.info("  PostgreSQL Driver Version : {}", info.driverVersion());
        logger.info("================================================================================");
    }

    /**
     * Resets the logged flag (primarily for testing purposes).
     */
    public static void resetLoggedFlag() {
        logged.set(false);
    }
}
