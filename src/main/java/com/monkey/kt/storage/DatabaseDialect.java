package com.monkey.kt.storage;

public enum DatabaseDialect {
    SQLITE("org.sqlite.JDBC", 0, false, true),

    MYSQL("com.mysql.cj.jdbc.Driver", 3306, true, false),
    MARIADB("org.mariadb.jdbc.Driver", 3306, true, false),

    POSTGRES("org.postgresql.Driver", 5432, true, false),
    COCKROACHDB("org.postgresql.Driver", 26257, true, false),

    SQLSERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver", 1433, true, false),

    ORACLE("oracle.jdbc.driver.OracleDriver", 1521, true, false),

    H2("org.h2.Driver", 9092, false, true),
    H2_SERVER("org.h2.Driver", 9092, true, false),

    HSQLDB("org.hsqldb.jdbc.JDBCDriver", 9001, false, true),
    DERBY("org.apache.derby.jdbc.EmbeddedDriver", 1527, false, true);

    private final String driverClass;
    private final int defaultPort;
    private final boolean requiresCredentials;
    private final boolean isFileDatabase;

    DatabaseDialect(String driverClass, int defaultPort, boolean requiresCredentials, boolean isFileDatabase) {
        this.driverClass = driverClass;
        this.defaultPort = defaultPort;
        this.requiresCredentials = requiresCredentials;
        this.isFileDatabase = isFileDatabase;
    }

    public static DatabaseDialect fromString(String type) {
        switch (type.toLowerCase()) {
            case "mysql": return MYSQL;
            case "mariadb": return MARIADB;
            case "postgres":
            case "postgresql": return POSTGRES;
            case "cockroachdb":
            case "cockroach": return COCKROACHDB;
            case "sqlserver":
            case "mssql": return SQLSERVER;
            case "oracle": return ORACLE;
            case "h2": return H2;
            case "h2-server": return H2_SERVER;
            case "hsqldb": return HSQLDB;
            case "derby": return DERBY;
            case "sqlite":
            default: return SQLITE;
        }
    }

    public String buildConnectionUrl(String host, int port, String database, String dataFolderPath) {
        switch (this) {
            case MYSQL:
                return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&autoReconnect=true&characterEncoding=utf8&serverTimezone=UTC",
                        host, port, database);
            case MARIADB:
                return String.format("jdbc:mariadb://%s:%d/%s?autoReconnect=true&characterEncoding=utf8",
                        host, port, database);
            case POSTGRES:
                return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            case COCKROACHDB:
                return String.format("jdbc:postgresql://%s:%d/%s?sslmode=require", host, port, database);
            case SQLSERVER:
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false;trustServerCertificate=true",
                        host, port, database);
            case ORACLE:
                return String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, database);
            case H2:
                return String.format("jdbc:h2:%s/%s;AUTO_SERVER=FALSE;FILE_LOCK=FS", dataFolderPath, database);
            case H2_SERVER:
                return String.format("jdbc:h2:tcp://%s:%d/%s", host, port, database);
            case HSQLDB:
                return String.format("jdbc:hsqldb:file:%s/%s", dataFolderPath, database);
            case DERBY:
                return String.format("jdbc:derby:%s/%s;create=true", dataFolderPath, database);
            case SQLITE:
            default:
                return String.format("jdbc:sqlite:%s/%s.db", dataFolderPath, database);
        }
    }

    public String getUpsertKillEffectQuery() {
        switch (this) {
            case MYSQL:
            case MARIADB:
                return "INSERT INTO killeffects (uuid, effect) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE effect = VALUES(effect)";
            case POSTGRES:
            case COCKROACHDB:
                return "INSERT INTO killeffects (uuid, effect) VALUES (?, ?) " +
                        "ON CONFLICT (uuid) DO UPDATE SET effect = EXCLUDED.effect";
            case SQLSERVER:
                return "MERGE killeffects AS target " +
                        "USING (VALUES (?, ?)) AS source (uuid, effect) " +
                        "ON target.uuid = source.uuid " +
                        "WHEN MATCHED THEN UPDATE SET effect = source.effect " +
                        "WHEN NOT MATCHED THEN INSERT (uuid, effect) VALUES (source.uuid, source.effect);";
            case ORACLE:
                return "MERGE INTO killeffects target " +
                        "USING (SELECT ? as uuid, ? as effect FROM dual) source " +
                        "ON (target.uuid = source.uuid) " +
                        "WHEN MATCHED THEN UPDATE SET effect = source.effect " +
                        "WHEN NOT MATCHED THEN INSERT (uuid, effect) VALUES (source.uuid, source.effect)";
            case H2:
            case H2_SERVER:
                return "MERGE INTO killeffects (uuid, effect) KEY(uuid) VALUES (?, ?)";
            case SQLITE:
            case HSQLDB:
            case DERBY:
            default:
                return "INSERT INTO killeffects (uuid, effect) VALUES (?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET effect = excluded.effect";
        }
    }

    public String getCreateKillEffectsTableQuery() {
        switch (this) {
            case MYSQL:
            case MARIADB:
                return "CREATE TABLE IF NOT EXISTS killeffects (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "effect VARCHAR(255) NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            case POSTGRES:
            case COCKROACHDB:
                return "CREATE TABLE IF NOT EXISTS killeffects (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "effect VARCHAR(255) NOT NULL" +
                        ")";
            case SQLSERVER:
                return "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='killeffects' AND xtype='U') " +
                        "CREATE TABLE killeffects (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "effect VARCHAR(255) NOT NULL" +
                        ")";
            case ORACLE:
                return "BEGIN " +
                        "EXECUTE IMMEDIATE 'CREATE TABLE killeffects (" +
                        "uuid VARCHAR2(36) PRIMARY KEY, " +
                        "effect VARCHAR2(255) NOT NULL" +
                        ")'; " +
                        "EXCEPTION WHEN OTHERS THEN " +
                        "IF SQLCODE != -955 THEN RAISE; END IF; " +
                        "END;";
            case H2:
            case H2_SERVER:
            case HSQLDB:
            case DERBY:
            case SQLITE:
            default:
                return "CREATE TABLE IF NOT EXISTS killeffects (" +
                        "uuid TEXT PRIMARY KEY, " +
                        "effect TEXT NOT NULL" +
                        ")";
        }
    }

    public String getCreateTempBlocksTableQuery() {
        switch (this) {
            case MYSQL:
            case MARIADB:
                return "CREATE TABLE IF NOT EXISTS temp_blocks (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "world VARCHAR(255) NOT NULL, " +
                        "x INT NOT NULL, " +
                        "y INT NOT NULL, " +
                        "z INT NOT NULL, " +
                        "material VARCHAR(255) NOT NULL, " +
                        "INDEX idx_location (world, x, y, z)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            case POSTGRES:
                return "CREATE TABLE IF NOT EXISTS temp_blocks (" +
                        "id SERIAL PRIMARY KEY, " +
                        "world VARCHAR(255) NOT NULL, " +
                        "x INTEGER NOT NULL, " +
                        "y INTEGER NOT NULL, " +
                        "z INTEGER NOT NULL, " +
                        "material VARCHAR(255) NOT NULL" +
                        "); " +
                        "CREATE INDEX IF NOT EXISTS idx_temp_blocks_location ON temp_blocks(world, x, y, z);";
            case COCKROACHDB:
                return "CREATE TABLE IF NOT EXISTS temp_blocks (" +
                        "id SERIAL PRIMARY KEY, " +
                        "world VARCHAR(255) NOT NULL, " +
                        "x INTEGER NOT NULL, " +
                        "y INTEGER NOT NULL, " +
                        "z INTEGER NOT NULL, " +
                        "material VARCHAR(255) NOT NULL, " +
                        "INDEX idx_location (world, x, y, z)" +
                        ")";
            case SQLSERVER:
                return "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='temp_blocks' AND xtype='U') " +
                        "BEGIN " +
                        "CREATE TABLE temp_blocks (" +
                        "id INT IDENTITY(1,1) PRIMARY KEY, " +
                        "world VARCHAR(255) NOT NULL, " +
                        "x INT NOT NULL, " +
                        "y INT NOT NULL, " +
                        "z INT NOT NULL, " +
                        "material VARCHAR(255) NOT NULL" +
                        "); " +
                        "CREATE INDEX idx_temp_blocks_location ON temp_blocks(world, x, y, z); " +
                        "END";
            case ORACLE:
                return "BEGIN " +
                        "EXECUTE IMMEDIATE 'CREATE TABLE temp_blocks (" +
                        "id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                        "world VARCHAR2(255) NOT NULL, " +
                        "x NUMBER NOT NULL, " +
                        "y NUMBER NOT NULL, " +
                        "z NUMBER NOT NULL, " +
                        "material VARCHAR2(255) NOT NULL" +
                        ")'; " +
                        "EXECUTE IMMEDIATE 'CREATE INDEX idx_temp_blocks_location ON temp_blocks(world, x, y, z)'; " +
                        "EXCEPTION WHEN OTHERS THEN " +
                        "IF SQLCODE != -955 THEN RAISE; END IF; " +
                        "END;";
            case H2:
            case H2_SERVER:
                return "CREATE TABLE IF NOT EXISTS temp_blocks (" +
                        "id IDENTITY PRIMARY KEY, " +
                        "world VARCHAR(255) NOT NULL, " +
                        "x INT NOT NULL, " +
                        "y INT NOT NULL, " +
                        "z INT NOT NULL, " +
                        "material VARCHAR(255) NOT NULL" +
                        "); " +
                        "CREATE INDEX IF NOT EXISTS idx_temp_blocks_location ON temp_blocks(world, x, y, z);";
            case HSQLDB:
                return "CREATE TABLE IF NOT EXISTS temp_blocks (" +
                        "id IDENTITY PRIMARY KEY, " +
                        "world VARCHAR(255) NOT NULL, " +
                        "x INTEGER NOT NULL, " +
                        "y INTEGER NOT NULL, " +
                        "z INTEGER NOT NULL, " +
                        "material VARCHAR(255) NOT NULL" +
                        "); " +
                        "CREATE INDEX IF NOT EXISTS idx_temp_blocks_location ON temp_blocks(world, x, y, z);";
            case DERBY:
                return "CREATE TABLE temp_blocks (" +
                        "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                        "world VARCHAR(255) NOT NULL, " +
                        "x INTEGER NOT NULL, " +
                        "y INTEGER NOT NULL, " +
                        "z INTEGER NOT NULL, " +
                        "material VARCHAR(255) NOT NULL, " +
                        "PRIMARY KEY (id)" +
                        ")";
            case SQLITE:
            default:
                return "CREATE TABLE IF NOT EXISTS temp_blocks (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "world TEXT NOT NULL, " +
                        "x INTEGER NOT NULL, " +
                        "y INTEGER NOT NULL, " +
                        "z INTEGER NOT NULL, " +
                        "material TEXT NOT NULL" +
                        "); " +
                        "CREATE INDEX IF NOT EXISTS idx_temp_blocks_location ON temp_blocks(world, x, y, z);";
        }
    }

    public String getCreateKillCoinsBalanceTableQuery() {
        switch (this) {
            case MYSQL:
            case MARIADB:
                return "CREATE TABLE IF NOT EXISTS killcoins_balance (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "balance DECIMAL(15,2) NOT NULL DEFAULT 0.00" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            case POSTGRES:
            case COCKROACHDB:
                return "CREATE TABLE IF NOT EXISTS killcoins_balance (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "balance DECIMAL(15,2) NOT NULL DEFAULT 0.00" +
                        ")";
            case SQLSERVER:
                return "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='killcoins_balance' AND xtype='U') " +
                        "CREATE TABLE killcoins_balance (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "balance DECIMAL(15,2) NOT NULL DEFAULT 0.00" +
                        ")";
            case ORACLE:
                return "BEGIN " +
                        "EXECUTE IMMEDIATE 'CREATE TABLE killcoins_balance (" +
                        "uuid VARCHAR2(36) PRIMARY KEY, " +
                        "balance NUMBER(15,2) DEFAULT 0.00 NOT NULL" +
                        ")'; " +
                        "EXCEPTION WHEN OTHERS THEN " +
                        "IF SQLCODE != -955 THEN RAISE; END IF; " +
                        "END;";
            case H2:
            case H2_SERVER:
            case HSQLDB:
            case DERBY:
            case SQLITE:
            default:
                return "CREATE TABLE IF NOT EXISTS killcoins_balance (" +
                        "uuid TEXT PRIMARY KEY, " +
                        "balance REAL NOT NULL DEFAULT 0.0" +
                        ")";
        }
    }

    public String getCreateKillCoinsPurchasesTableQuery() {
        switch (this) {
            case MYSQL:
            case MARIADB:
                return "CREATE TABLE IF NOT EXISTS killcoins_purchases (" +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "effect VARCHAR(255) NOT NULL, " +
                        "purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "PRIMARY KEY (uuid, effect), " +
                        "INDEX idx_purchases_uuid (uuid)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            case POSTGRES:
                return "CREATE TABLE IF NOT EXISTS killcoins_purchases (" +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "effect VARCHAR(255) NOT NULL, " +
                        "purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "PRIMARY KEY (uuid, effect)" +
                        "); " +
                        "CREATE INDEX IF NOT EXISTS idx_purchases_uuid ON killcoins_purchases(uuid);";
            case COCKROACHDB:
                return "CREATE TABLE IF NOT EXISTS killcoins_purchases (" +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "effect VARCHAR(255) NOT NULL, " +
                        "purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP(), " +
                        "PRIMARY KEY (uuid, effect), " +
                        "INDEX idx_purchases_uuid (uuid)" +
                        ")";
            case SQLSERVER:
                return "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='killcoins_purchases' AND xtype='U') " +
                        "BEGIN " +
                        "CREATE TABLE killcoins_purchases (" +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "effect VARCHAR(255) NOT NULL, " +
                        "purchase_date DATETIME DEFAULT GETDATE(), " +
                        "PRIMARY KEY (uuid, effect)" +
                        "); " +
                        "CREATE INDEX idx_purchases_uuid ON killcoins_purchases(uuid); " +
                        "END";
            case ORACLE:
                return "BEGIN " +
                        "EXECUTE IMMEDIATE 'CREATE TABLE killcoins_purchases (" +
                        "uuid VARCHAR2(36) NOT NULL, " +
                        "effect VARCHAR2(255) NOT NULL, " +
                        "purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "PRIMARY KEY (uuid, effect)" +
                        ")'; " +
                        "EXECUTE IMMEDIATE 'CREATE INDEX idx_purchases_uuid ON killcoins_purchases(uuid)'; " +
                        "EXCEPTION WHEN OTHERS THEN " +
                        "IF SQLCODE != -955 THEN RAISE; END IF; " +
                        "END;";
            case H2:
            case H2_SERVER:
                return "CREATE TABLE IF NOT EXISTS killcoins_purchases (" +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "effect VARCHAR(255) NOT NULL, " +
                        "purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "PRIMARY KEY (uuid, effect)" +
                        "); " +
                        "CREATE INDEX IF NOT EXISTS idx_purchases_uuid ON killcoins_purchases(uuid);";
            case HSQLDB:
                return "CREATE TABLE IF NOT EXISTS killcoins_purchases (" +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "effect VARCHAR(255) NOT NULL, " +
                        "purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "PRIMARY KEY (uuid, effect)" +
                        "); " +
                        "CREATE INDEX IF NOT EXISTS idx_purchases_uuid ON killcoins_purchases(uuid);";
            case DERBY:
                return "CREATE TABLE killcoins_purchases (" +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "effect VARCHAR(255) NOT NULL, " +
                        "purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "PRIMARY KEY (uuid, effect)" +
                        ")";
            case SQLITE:
            default:
                return "CREATE TABLE IF NOT EXISTS killcoins_purchases (" +
                        "uuid TEXT NOT NULL, " +
                        "effect TEXT NOT NULL, " +
                        "purchase_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "PRIMARY KEY (uuid, effect)" +
                        "); " +
                        "CREATE INDEX IF NOT EXISTS idx_purchases_uuid ON killcoins_purchases(uuid);";
        }
    }

    public String getUpsertBalanceQuery() {
        switch (this) {
            case MYSQL:
            case MARIADB:
                return "INSERT INTO killcoins_balance (uuid, balance) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE balance = VALUES(balance)";
            case POSTGRES:
            case COCKROACHDB:
                return "INSERT INTO killcoins_balance (uuid, balance) VALUES (?, ?) " +
                        "ON CONFLICT (uuid) DO UPDATE SET balance = EXCLUDED.balance";
            case SQLSERVER:
                return "MERGE killcoins_balance AS target " +
                        "USING (VALUES (?, ?)) AS source (uuid, balance) " +
                        "ON target.uuid = source.uuid " +
                        "WHEN MATCHED THEN UPDATE SET balance = source.balance " +
                        "WHEN NOT MATCHED THEN INSERT (uuid, balance) VALUES (source.uuid, source.balance);";
            case ORACLE:
                return "MERGE INTO killcoins_balance target " +
                        "USING (SELECT ? as uuid, ? as balance FROM dual) source " +
                        "ON (target.uuid = source.uuid) " +
                        "WHEN MATCHED THEN UPDATE SET balance = source.balance " +
                        "WHEN NOT MATCHED THEN INSERT (uuid, balance) VALUES (source.uuid, source.balance)";
            case H2:
            case H2_SERVER:
                return "MERGE INTO killcoins_balance (uuid, balance) KEY(uuid) VALUES (?, ?)";
            case SQLITE:
            case HSQLDB:
            case DERBY:
            default:
                return "INSERT INTO killcoins_balance (uuid, balance) VALUES (?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET balance = excluded.balance";
        }
    }

    public String getInsertOrIgnoreBalanceQuery() {
        switch (this) {
            case MYSQL:
            case MARIADB:
                return "INSERT IGNORE INTO killcoins_balance (uuid, balance) VALUES (?, ?)";
            case POSTGRES:
            case COCKROACHDB:
                return "INSERT INTO killcoins_balance (uuid, balance) VALUES (?, ?) ON CONFLICT (uuid) DO NOTHING";
            case SQLSERVER:
                return "IF NOT EXISTS (SELECT 1 FROM killcoins_balance WHERE uuid = ?) " +
                        "INSERT INTO killcoins_balance (uuid, balance) VALUES (?, ?)";
            case ORACLE:
                return "INSERT INTO killcoins_balance (uuid, balance) " +
                        "SELECT ?, ? FROM dual " +
                        "WHERE NOT EXISTS (SELECT 1 FROM killcoins_balance WHERE uuid = ?)";
            case H2:
            case H2_SERVER:
            case HSQLDB:
            case DERBY:
                return "MERGE INTO killcoins_balance (uuid, balance) KEY(uuid) VALUES (?, ?)";
            case SQLITE:
            default:
                return "INSERT OR IGNORE INTO killcoins_balance (uuid, balance) VALUES (?, ?)";
        }
    }

    public String getInsertOrIgnorePurchaseQuery() {
        switch (this) {
            case MYSQL:
            case MARIADB:
                return "INSERT IGNORE INTO killcoins_purchases (uuid, effect) VALUES (?, ?)";
            case POSTGRES:
            case COCKROACHDB:
                return "INSERT INTO killcoins_purchases (uuid, effect) VALUES (?, ?) ON CONFLICT (uuid, effect) DO NOTHING";
            case SQLSERVER:
                return "IF NOT EXISTS (SELECT 1 FROM killcoins_purchases WHERE uuid = ? AND effect = ?) " +
                        "INSERT INTO killcoins_purchases (uuid, effect) VALUES (?, ?)";
            case ORACLE:
                return "INSERT INTO killcoins_purchases (uuid, effect) " +
                        "SELECT ?, ? FROM dual " +
                        "WHERE NOT EXISTS (SELECT 1 FROM killcoins_purchases WHERE uuid = ? AND effect = ?)";
            case H2:
            case H2_SERVER:
            case HSQLDB:
            case DERBY:
                return "MERGE INTO killcoins_purchases (uuid, effect) KEY(uuid, effect) VALUES (?, ?)";
            case SQLITE:
            default:
                return "INSERT OR IGNORE INTO killcoins_purchases (uuid, effect) VALUES (?, ?)";
        }
    }

    public String getDriverClass() { return driverClass; }
    public int getDefaultPort() { return defaultPort; }
    public boolean requiresCredentials() { return requiresCredentials; }
    public boolean isFileDatabase() { return isFileDatabase; }
}