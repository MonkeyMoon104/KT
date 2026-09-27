package com.monkey.ktplus.storage.migration.connection;

import com.monkey.ktplus.storage.migration.MigrationDialect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public final class MigrationEndpoint {
    private final MigrationDialect dialect;
    private final String displaySummary;
    private final String confirmationToken;
    private final @Nullable Path sqliteFile;
    private final @Nullable String host;
    private final int port;
    private final @Nullable String databaseName;
    private final String identityMaterial;

    private MigrationEndpoint(
            MigrationDialect dialect,
            String displaySummary,
            String confirmationToken,
            String identityMaterial,
            @Nullable Path sqliteFile,
            @Nullable String host,
            int port,
            @Nullable String databaseName) {
        this.dialect = Objects.requireNonNull(dialect, "dialect");
        this.displaySummary = Objects.requireNonNull(displaySummary, "displaySummary");
        this.confirmationToken = Objects.requireNonNull(confirmationToken, "confirmationToken");
        this.identityMaterial = Objects.requireNonNull(identityMaterial, "identityMaterial");
        this.sqliteFile = sqliteFile;
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
    }

    public static MigrationEndpoint sqlite(Path absoluteFile) {
        Objects.requireNonNull(absoluteFile, "absoluteFile");
        Path normalized = absoluteFile.toAbsolutePath().normalize();
        String pathToken = normalized.toString().replace('\\', '/');
        String material = "sqlite|" + pathToken.toLowerCase(Locale.ROOT);
        return new MigrationEndpoint(
                MigrationDialect.SQLITE,
                "SQLite file: " + pathToken,
                shortToken(material),
                material,
                normalized,
                null,
                0,
                null);
    }

    public static MigrationEndpoint mysql(String host, int port, String databaseName) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(databaseName, "databaseName");
        String normalizedHost = host.trim().toLowerCase(Locale.ROOT);
        String normalizedDatabase = databaseName.trim().toLowerCase(Locale.ROOT);
        String display = normalizedHost + ":" + port + "/" + normalizedDatabase;
        String material = "mysql|" + normalizedHost + "|" + port + "|" + normalizedDatabase;
        return new MigrationEndpoint(
                MigrationDialect.MYSQL,
                "MySQL " + display,
                shortToken(material),
                material,
                null,
                normalizedHost,
                port,
                normalizedDatabase);
    }

    public MigrationDialect dialect() {
        return dialect;
    }

    public String displaySummary() {
        return displaySummary;
    }

    public String confirmationToken() {
        return confirmationToken;
    }

    public @Nullable Path sqliteFile() {
        return sqliteFile;
    }

    public @Nullable String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public @Nullable String databaseName() {
        return databaseName;
    }

    public boolean matchesConfirmation(String typed) {
        if (typed == null) {
            return false;
        }
        String normalized = normalizeConfirmation(typed);
        if (confirmationToken.equalsIgnoreCase(normalized)) {
            return true;
        }
        return matchesLegacyIdentity(normalized);
    }

    private boolean matchesLegacyIdentity(String typed) {
        if (dialect == MigrationDialect.SQLITE) {
            String path = identityMaterial.substring("sqlite|".length());
            return path.equalsIgnoreCase(typed);
        }
        if (dialect != MigrationDialect.MYSQL || host == null || databaseName == null) {
            return false;
        }
        String slashForm = host + "/" + port + "/" + databaseName;
        String colonForm = host + ":" + port + "/" + databaseName;
        String asSlash = legacyMysqlColonToSlash(typed);
        return slashForm.equalsIgnoreCase(typed)
                || colonForm.equalsIgnoreCase(typed)
                || slashForm.equalsIgnoreCase(asSlash);
    }

    private static String normalizeConfirmation(String typed) {
        return typed.trim().replace('\\', '/');
    }

    /** Maps {@code host:port/db} → {@code host/port/db}; leaves other strings unchanged. */
    private static String legacyMysqlColonToSlash(String token) {
        int colon = token.indexOf(':');
        int slash = token.indexOf('/');
        if (colon <= 0 || slash <= colon + 1) {
            return token;
        }
        String hostPart = token.substring(0, colon);
        String portAndDb = token.substring(colon + 1);
        if (!portAndDb.matches("\\d+/.*")) {
            return token;
        }
        return hostPart + "/" + portAndDb;
    }

    /** Stable 8-hex confirmation token (Brigadier-safe, short for chat). */
    static String shortToken(String material) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(material.getBytes(StandardCharsets.UTF_8));
            StringBuilder token = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                token.append(String.format(Locale.ROOT, "%02x", hash[i]));
            }
            return token.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 unavailable", error);
        }
    }
}
