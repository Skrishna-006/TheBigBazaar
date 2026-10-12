package com.siva.shopsphere.diagnostics;

import java.lang.reflect.Method;

/**
 * Utility for parsing connection parameters safely from JDBC connection URLs
 * and detecting database driver metadata.
 * <p>
 * Under no circumstances does this parser store, log, or leak sensitive credentials,
 * authentication tokens, or complete raw connection strings.
 */
public final class DatasourceDiagnosticParser {

    private DatasourceDiagnosticParser() {
    }

    public record ParsedJdbcUrl(
            String host,
            String port,
            String databaseName,
            String sslMode
    ) {
    }

    /**
     * Parses the JDBC connection URL and safely extracts only host, port, database name, and sslmode.
     * Any userInfo (username:password) or secondary query parameters are completely discarded.
     *
     * @param rawUrl the configured JDBC URL (can be null or blank)
     * @return a safe ParsedJdbcUrl containing only non-secret components
     */
    public static ParsedJdbcUrl parse(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return new ParsedJdbcUrl("<none>", "<none>", "<none>", "<none>");
        }

        try {
            String working = rawUrl.trim();

            // 1. Separate query parameters (if any) and parse sslmode
            String sslMode = "<not specified>";
            int queryIdx = working.indexOf('?');
            if (queryIdx >= 0) {
                String query = working.substring(queryIdx + 1);
                working = working.substring(0, queryIdx);
                sslMode = extractSslMode(query);
            }

            // 2. Strip protocol prefixes (e.g. jdbc:postgresql://, postgresql://, postgres://)
            if (working.startsWith("jdbc:")) {
                working = working.substring(5);
            }
            if (working.startsWith("postgresql:")) {
                working = working.substring(11);
            } else if (working.startsWith("postgres:")) {
                working = working.substring(9);
            }

            if (working.startsWith("//")) {
                working = working.substring(2);
            }

            // 3. Separate authority (host:port or user:pass@host:port) from path (/database)
            String authority;
            String databaseName = "<not specified>";
            int slashIdx = working.indexOf('/');
            if (slashIdx >= 0) {
                authority = working.substring(0, slashIdx);
                String dbPart = working.substring(slashIdx + 1).trim();
                if (!dbPart.isEmpty()) {
                    databaseName = dbPart;
                }
            } else {
                authority = working;
            }

            // 4. Strip user credentials from authority if present: user:pass@host:port
            int atIdx = authority.lastIndexOf('@');
            if (atIdx >= 0) {
                authority = authority.substring(atIdx + 1);
            }

            // 5. Parse host and port from authority
            String host = "<not specified>";
            String port = "<not specified>";

            if (!authority.isBlank()) {
                if (authority.startsWith("[")) {
                    // IPv6 address format: [::1]:5432
                    int closingBracket = authority.indexOf(']');
                    if (closingBracket >= 0) {
                        host = authority.substring(1, closingBracket);
                        if (closingBracket + 1 < authority.length() && authority.charAt(closingBracket + 1) == ':') {
                            String portStr = authority.substring(closingBracket + 2).trim();
                            if (!portStr.isEmpty()) {
                                port = portStr;
                            }
                        }
                    } else {
                        host = authority;
                    }
                } else {
                    int colonIdx = authority.lastIndexOf(':');
                    if (colonIdx >= 0) {
                        host = authority.substring(0, colonIdx);
                        String portStr = authority.substring(colonIdx + 1).trim();
                        if (!portStr.isEmpty()) {
                            port = portStr;
                        }
                    } else {
                        host = authority;
                    }
                }
            }

            return new ParsedJdbcUrl(
                    host.isBlank() ? "<not specified>" : host,
                    port.isBlank() ? "<not specified>" : port,
                    databaseName,
                    sslMode
            );
        } catch (Throwable ignored) {
            // Guarantee no raw URL, credential, or sensitive exception string is ever propagated
            return new ParsedJdbcUrl("<unparseable>", "<unparseable>", "<unparseable>", "<unparseable>");
        }
    }

    /**
     * Extracts the sslmode value from the query string without preserving any other parameters.
     */
    private static String extractSslMode(String queryString) {
        if (queryString == null || queryString.isBlank()) {
            return "<not specified>";
        }

        String foundSslMode = null;
        String foundSsl = null;

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int eqIdx = pair.indexOf('=');
            if (eqIdx > 0) {
                String key = pair.substring(0, eqIdx).trim();
                String val = pair.substring(eqIdx + 1).trim();
                if ("sslmode".equalsIgnoreCase(key)) {
                    foundSslMode = val;
                } else if ("ssl".equalsIgnoreCase(key)) {
                    foundSsl = val;
                }
            }
        }

        if (foundSslMode != null && !foundSslMode.isBlank()) {
            return foundSslMode;
        }
        if (foundSsl != null && !foundSsl.isBlank()) {
            return foundSsl;
        }
        return "<not specified>";
    }

    /**
     * Safely detects the PostgreSQL JDBC driver version using reflection.
     *
     * @return driver version string, or an informative fallback message if not found
     */
    public static String getPostgresDriverVersion() {
        try {
            Class<?> driverClass = Class.forName("org.postgresql.Driver");
            try {
                Method getVersionMethod = driverClass.getMethod("getVersion");
                Object version = getVersionMethod.invoke(null);
                if (version != null && !version.toString().isBlank()) {
                    return version.toString();
                }
            } catch (ReflectiveOperationException ignored) {
            }

            Package pkg = driverClass.getPackage();
            if (pkg != null && pkg.getImplementationVersion() != null && !pkg.getImplementationVersion().isBlank()) {
                return pkg.getImplementationVersion();
            }

            return "Present on classpath (version undetermined)";
        } catch (ClassNotFoundException e) {
            return "Not found on classpath";
        } catch (Throwable t) {
            return "Unknown";
        }
    }
}
