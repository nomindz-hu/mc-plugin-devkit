package hu.nomindz.devkit.managers;

import hu.nomindz.devkit.config.DatabaseConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Generic DatabaseManager for Bukkit plugins.
 * Works with DatabaseConfig (from config.database()).
 *
 * Example:
 * DatabaseManager db = new DatabaseManager(plugin, () ->
 * plugin.config().database());
 * try (Connection conn = db.getConnection()) {
 * // Use conn
 * }
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private final JavaPlugin plugin;
    private final Supplier<DatabaseConfig> config;
    private Connection connection;

    private DatabaseManager(JavaPlugin plugin, Supplier<DatabaseConfig> configSupplier) {
        this.plugin = Objects.requireNonNull(plugin);
        this.config = Objects.requireNonNull(configSupplier);
    }

    public static DatabaseManager getInstance(JavaPlugin plugin, Supplier<DatabaseConfig> configSupplier) {
        if (instance == null) {
            instance = new DatabaseManager(plugin, configSupplier);
        }

        return instance;
    }

    /**
     * Returns a valid Connection.
     * Lazily creates or reopens it if closed.
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            DatabaseConfig cfg = config.get();

            if (cfg == null) {
                throw new SQLException("Database config not loaded (cfg == null)");
            }

            String path = plugin.getDataFolder().getAbsolutePath() + "/" +
                    (cfg.file() == null || cfg.file().isEmpty() ? "plugin.db" : cfg.file());

            connection = DriverManager.getConnection("jdbc:sqlite:" + path);

            // Apply useful SQLite pragmas
            try (Statement s = connection.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON;");
                s.execute("PRAGMA busy_timeout = 5000;");
                s.execute("PRAGMA journal_mode = WAL;");
            }

            plugin.getLogger().info("Connected to SQLite database: " + path);
        }

        return connection;
    }

    /**
     * Forces closing the connection.
     */
    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
        } finally {
            connection = null;
        }
    }

    /**
     * Called when the config reloads (closes and reconnects lazily).
     */
    public synchronized void onConfigReload() {
        plugin.getLogger().info("Database configuration reloaded. Resetting connection...");
        close(); // lazy reconnect on next getConnection()
    }

    /**
     * Each statement is executed independently so failure in one doesn't stop the others.
     */
    public void initializeTables(List<String> tableStatements) {
        Objects.requireNonNull(tableStatements, "tableStatements");

        try (Connection conn = getConnection()) {
            for (String sql : tableStatements) {
                String trimmed = sql == null ? "" : sql.trim();
                if (trimmed.isEmpty()) continue;

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(trimmed);
                    plugin.getLogger().info("Executed schema statement: " + getFirstLine(trimmed));
                } catch (SQLException e) {
                    plugin.getLogger().severe("Failed to execute SQL: " + getFirstLine(trimmed));
                    plugin.getLogger().severe(" → " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize tables: " + e.getMessage());
        }
    }

    private String getFirstLine(String sql) {
        String[] lines = sql.split("\n");
        return lines.length > 0 ? lines[0] : sql;
    }

    public void initializeFromResource(String resourceName) {
        try (InputStream in = plugin.getResource(resourceName)) {
            if (in == null) {
                plugin.getLogger().warning("Schema resource not found: " + resourceName);
                return;
            }
            String content = new String(in.readAllBytes());
            // Split by semicolons to support multiple statements
            List<String> statements = Arrays.stream(content.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            initializeTables(statements);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load schema resource: " + e.getMessage());
        }
    }
}
