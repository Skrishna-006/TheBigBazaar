package com.siva.shopsphere.diagnostics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class DatasourceDiagnosticParserTest {

    @Test
    @DisplayName("Successfully parses Supabase Session Pooler URL with sslmode=require")
    void testParseSupabaseSessionPoolerUrl() {
        String url = "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require";
        DatasourceDiagnosticParser.ParsedJdbcUrl parsed = DatasourceDiagnosticParser.parse(url);

        assertEquals("aws-0-us-east-1.pooler.supabase.com", parsed.host());
        assertEquals("5432", parsed.port());
        assertEquals("postgres", parsed.databaseName());
        assertEquals("require", parsed.sslMode());
    }

    @Test
    @DisplayName("Guarantees that embedded credentials and user info are completely stripped and never exposed")
    void testEmbeddedCredentialsNeverLeak() {
        String secretUser = "postgres.project-ref-xyz";
        String secretPass = "Super$ecretP@ssword!123";
        String url = "jdbc:postgresql://" + secretUser + ":" + secretPass
                + "@aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require&user="
                + secretUser + "&password=" + secretPass;

        DatasourceDiagnosticParser.ParsedJdbcUrl parsed = DatasourceDiagnosticParser.parse(url);

        assertEquals("aws-0-us-east-1.pooler.supabase.com", parsed.host());
        assertEquals("5432", parsed.port());
        assertEquals("postgres", parsed.databaseName());
        assertEquals("require", parsed.sslMode());

        // Ensure secrets are not present in any parsed fields
        assertFalse(parsed.host().contains(secretUser));
        assertFalse(parsed.host().contains(secretPass));
        assertFalse(parsed.port().contains(secretPass));
        assertFalse(parsed.databaseName().contains(secretUser));
        assertFalse(parsed.databaseName().contains(secretPass));
        assertFalse(parsed.sslMode().contains(secretUser));
        assertFalse(parsed.sslMode().contains(secretPass));
        assertFalse(parsed.toString().contains(secretPass));
        assertFalse(parsed.toString().contains(secretUser));
    }

    @Test
    @DisplayName("Successfully parses default localhost URL without SSL parameters")
    void testParseLocalhostUrl() {
        String url = "jdbc:postgresql://localhost:5432/shopsphere";
        DatasourceDiagnosticParser.ParsedJdbcUrl parsed = DatasourceDiagnosticParser.parse(url);

        assertEquals("localhost", parsed.host());
        assertEquals("5432", parsed.port());
        assertEquals("shopsphere", parsed.databaseName());
        assertEquals("<not specified>", parsed.sslMode());
    }

    @Test
    @DisplayName("Successfully parses Supabase Transaction Pooler URL on port 6543")
    void testParseSupabaseTransactionPoolerPort6543() {
        String url = "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres?sslmode=require";
        DatasourceDiagnosticParser.ParsedJdbcUrl parsed = DatasourceDiagnosticParser.parse(url);

        assertEquals("aws-0-us-east-1.pooler.supabase.com", parsed.host());
        assertEquals("6543", parsed.port());
        assertEquals("postgres", parsed.databaseName());
        assertEquals("require", parsed.sslMode());
    }

    @Test
    @DisplayName("Successfully parses URL without explicit port")
    void testParseUrlWithoutPort() {
        String url = "jdbc:postgresql://db.myproject.supabase.co/postgres?sslmode=require";
        DatasourceDiagnosticParser.ParsedJdbcUrl parsed = DatasourceDiagnosticParser.parse(url);

        assertEquals("db.myproject.supabase.co", parsed.host());
        assertEquals("<not specified>", parsed.port());
        assertEquals("postgres", parsed.databaseName());
        assertEquals("require", parsed.sslMode());
    }

    @Test
    @DisplayName("Successfully parses non-jdbc postgresql:// scheme")
    void testParseDirectPostgresScheme() {
        String url = "postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=verify-full";
        DatasourceDiagnosticParser.ParsedJdbcUrl parsed = DatasourceDiagnosticParser.parse(url);

        assertEquals("aws-0-us-east-1.pooler.supabase.com", parsed.host());
        assertEquals("5432", parsed.port());
        assertEquals("postgres", parsed.databaseName());
        assertEquals("verify-full", parsed.sslMode());
    }

    @Test
    @DisplayName("Handles IPv6 address with brackets safely")
    void testParseIpv6Address() {
        String url = "jdbc:postgresql://[2001:db8::1]:5432/postgres?sslmode=require";
        DatasourceDiagnosticParser.ParsedJdbcUrl parsed = DatasourceDiagnosticParser.parse(url);

        assertEquals("2001:db8::1", parsed.host());
        assertEquals("5432", parsed.port());
        assertEquals("postgres", parsed.databaseName());
        assertEquals("require", parsed.sslMode());
    }

    @Test
    @DisplayName("Handles null and blank URLs without exceptions")
    void testParseNullAndBlank() {
        DatasourceDiagnosticParser.ParsedJdbcUrl nullResult = DatasourceDiagnosticParser.parse(null);
        assertEquals("<none>", nullResult.host());
        assertEquals("<none>", nullResult.port());
        assertEquals("<none>", nullResult.databaseName());
        assertEquals("<none>", nullResult.sslMode());

        DatasourceDiagnosticParser.ParsedJdbcUrl blankResult = DatasourceDiagnosticParser.parse("   ");
        assertEquals("<none>", blankResult.host());
        assertEquals("<none>", blankResult.port());
        assertEquals("<none>", blankResult.databaseName());
        assertEquals("<none>", blankResult.sslMode());
    }

    @Test
    @DisplayName("Handles malformed inputs gracefully without throwing or leaking")
    void testParseMalformedInput() {
        DatasourceDiagnosticParser.ParsedJdbcUrl result = DatasourceDiagnosticParser.parse(":::malformed::url??&&");
        assertNotNull(result);
        assertNotNull(result.host());
        assertNotNull(result.port());
        assertNotNull(result.databaseName());
        assertNotNull(result.sslMode());
    }

    @Test
    @DisplayName("PostgreSQL driver version is successfully resolved from classpath")
    void testPostgresDriverVersionDetected() {
        String version = DatasourceDiagnosticParser.getPostgresDriverVersion();
        assertNotNull(version);
        assertFalse(version.isBlank());
        assertNotEquals("Not found on classpath", version);
        // Expected driver version is 42.7.7
        assertTrue(version.contains("42.7.") || version.contains("42."), "Version should match PostgreSQL driver: " + version);
    }

    @Test
    @DisplayName("DatasourceDiagnostics extracts environment presence flags correctly")
    void testDiagnosticsExtractionFromEnvironment() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require");
        env.setProperty("DATABASE_USERNAME", "postgres.myproject");
        env.setProperty("DATABASE_PASSWORD", "secret123");
        env.setProperty("spring.datasource.url", "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require");

        DatasourceDiagnosticInfo info = DatasourceDiagnostics.extract(env);

        assertTrue(info.databaseUrlPresent());
        assertTrue(info.databaseUsernamePresent());
        assertTrue(info.databasePasswordPresent());
        assertEquals("aws-0-us-east-1.pooler.supabase.com", info.host());
        assertEquals("5432", info.port());
        assertEquals("postgres", info.databaseName());
        assertEquals("require", info.sslMode());
        assertNotNull(info.driverVersion());

        // Ensure toString never exposes username or password values
        assertFalse(info.toString().contains("postgres.myproject"));
        assertFalse(info.toString().contains("secret123"));
    }

    @Test
    @DisplayName("DatasourceDiagnostics correctly detects absent properties")
    void testDiagnosticsExtractionWhenAbsent() {
        MockEnvironment env = new MockEnvironment();
        // None of the variables set
        DatasourceDiagnosticInfo info = DatasourceDiagnostics.extract(env);

        assertFalse(info.databaseUrlPresent());
        assertFalse(info.databaseUsernamePresent());
        assertFalse(info.databasePasswordPresent());
        assertEquals("<none>", info.host());
        assertEquals("<none>", info.port());
        assertEquals("<none>", info.databaseName());
        assertEquals("<none>", info.sslMode());
    }

    @Test
    @DisplayName("DatasourceDiagnosticsListener logs formatted output without throwing")
    void testListenerLoggingExecution() {
        DatasourceDiagnosticInfo info = new DatasourceDiagnosticInfo(
                true,
                true,
                true,
                "aws-0-us-east-1.pooler.supabase.com",
                "5432",
                "postgres",
                "require",
                "42.7.7"
        );

        assertDoesNotThrow(() -> DatasourceDiagnosticsListener.logDiagnostics(info));
    }
}
