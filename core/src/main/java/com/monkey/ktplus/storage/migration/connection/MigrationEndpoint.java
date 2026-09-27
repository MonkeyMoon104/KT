package com.monkey.ktplus.storage.migration.connection;

import com.monkey.ktplus.storage.migration.MigrationDialect;
import java.nio.file.Path;
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

    private MigrationEndpoint(
            MigrationDialect dialect,
            String displaySummary,
            String confirmationToken,
            @Nullable Path sqliteFile,
            @Nullable String host,
            int port,
            @Nullable String databaseName) {
        this.dialect = Objects.requireNonNull(dialect, "dialect");
        this.displaySummary = Objects.requireNonNull(displaySummary, "displaySummary");
        this.confirmationToken = Objects.requireNonNull(confirmationToken, "confirmationToken");
        this.sqliteFile = sqliteFile;
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
    }

    public static MigrationEndpoint sqlite(Path absoluteFile) {
        Objects.requireNonNull(absoluteFile, "absoluteFile");
        Path normalized = absoluteFile.toAbsolutePath().normalize();
        String token = normalized.toString().replace('\\', '/');
        return new MigrationEndpoint(
                MigrationDialect.SQLITE,
                "SQLite file: " + token,
                token,
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
        // Avoid ':' in the token — Brigadier/Lamp treat it as an argument separator in chat.
        String token = normalizedHost + "/" + port + "/" + normalizedDatabase;
        String display = normalizedHost + ":" + port + "/" + normalizedDatabase;
        return new MigrationEndpoint(
                MigrationDialect.MYSQL,
                "MySQL " + display,
                token,
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
        if (confirmationToken.equals(normalized)) {
            return true;
        }
        // Accept legacy MySQL tokens that used host:port/db.
        return confirmationToken.equals(legacyMysqlColonToSlash(normalized));
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
        String host = token.substring(0, colon);
        String portAndDb = token.substring(colon + 1);
        if (!portAndDb.matches("\\d+/.*")) {
            return token;
        }
        return host + "/" + portAndDb;
    }
}
