package com.siva.shopsphere.diagnostics;

/**
 * Immutable holder for safe, non-sensitive datasource diagnostic information.
 * All credentials, full URLs, and sensitive query parameters are strictly excluded.
 */
public record DatasourceDiagnosticInfo(
        boolean databaseUrlPresent,
        boolean databaseUsernamePresent,
        boolean databasePasswordPresent,
        String host,
        String port,
        String databaseName,
        String sslMode,
        String driverVersion
) {
    @Override
    public String toString() {
        return "DatasourceDiagnosticInfo[" +
                "databaseUrlPresent=" + databaseUrlPresent +
                ", databaseUsernamePresent=" + databaseUsernamePresent +
                ", databasePasswordPresent=" + databasePasswordPresent +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", sslMode='" + sslMode + '\'' +
                ", driverVersion='" + driverVersion + '\'' +
                ']';
    }
}
