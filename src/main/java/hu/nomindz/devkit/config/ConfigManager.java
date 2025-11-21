package hu.nomindz.devkit.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ConfigManager<T> {
    private final JavaPlugin plugin;
    private final String fileName;
    private final String versionKey;
    private final int currentVersion;
    private final Class<T> type;
    private final AutoConfigBinder binder;
    private final ConfigMigration migration;
    private final List<ConfigListener<T>> listeners = new CopyOnWriteArrayList<>();

    private List<String> defaultsResources;
    private File file;
    private YamlConfiguration yaml;
    private volatile T snapshot;

    public ConfigManager(
            JavaPlugin plugin,
            Class<T> type,
            String fileName,
            List<String> defaultsResources,
            String versionKey,
            int currentVersion,
            ConfigMigration migration,
            AutoConfigBinder binder) {
        this.plugin = plugin;
        this.type = type;
        this.fileName = fileName;
        this.versionKey = versionKey;
        this.currentVersion = currentVersion;
        this.migration = migration;
        this.binder = binder;

        defaultsResources.addFirst("devkit-config.yml");
        this.defaultsResources = defaultsResources;
    }

    /** Load or create the file, run migrations, build typed snapshot. */
    public synchronized void loadOrCreate() {
        ensureFileExists();
        loadYaml();
        migrateIfNeeded(yaml);
        this.snapshot = binder.bind(yaml, type);
        plugin.getLogger().info(fileName + " loaded (version " + yaml.getInt(versionKey, currentVersion) + ")");
    }

    /** Reload the yaml and rebuild the typed snapshot. */
    public synchronized void reload() {
        loadYaml();
        migrateIfNeeded(yaml);
        this.snapshot = binder.bind(yaml, type);
        listeners.forEach(l -> l.onReload(snapshot));
        plugin.getLogger().info(fileName + " reloaded.");
    }

    public void addListener(ConfigListener<T> listener) {
        listeners.add(listener);
    }

    public T get() {
        return snapshot;
    }

    public FileConfiguration raw() {
        return yaml;
    }

    private void ensureFileExists() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.file = new File(plugin.getDataFolder(), fileName);
        if (this.file.exists())
            return;

        for (int i = defaultsResources.size() - 1; i >= 0; i--) {
            String res = defaultsResources.get(i);
            try (InputStream in = plugin.getResource(res)) {
                if (in != null) {
                    java.nio.file.Files.copy(
                            in,
                            this.file.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    plugin.getLogger().info("Saved default config from resource: " + res);
                    return;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save default config from " + res + ": " + e.getMessage());
            }
        }

        try {
            if (this.file.createNewFile()) {
                plugin.getLogger().info("Created empty config.yml (defaults will be applied in memory)");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create empty config.yml: " + e.getMessage());
        }
    }

    private void loadYaml() {
        if (this.file == null) {
            throw new IllegalStateException("Config file is null. Did you call ensureFileExists()?");
        }

        this.yaml = new YamlConfiguration();
        try {
            yaml.load(this.file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException("Failed to load " + fileName + ": " + e.getMessage(), e);
        }

        for (String res : defaultsResources) {
            try (InputStream in = plugin.getResource(res) != null
                    ? plugin.getResource(res)
                    : getClass().getClassLoader().getResourceAsStream(res)) {

                if (in == null) {
                    plugin.getLogger().warning("Default resource not found: " + res);
                    return;
                }

                YamlConfiguration defaults = new YamlConfiguration();
                defaults.loadFromString(new String(in.readAllBytes()));
                yaml.addDefaults(defaults);
            } catch (Exception e) {
                plugin.getLogger().warning("Couldn't apply defaults from " + res + ": " + e.getMessage());
            }
        }

        yaml.options().copyDefaults(true);
    }

    private void migrateIfNeeded(YamlConfiguration y) {
        int fileVersion = y.getInt(versionKey, -1);
        if (fileVersion == -1) {
            y.set(versionKey, currentVersion);
            saveQuietly(y);
            return;
        }
        if (fileVersion < currentVersion && migration != null) {
            plugin.getLogger()
                    .info("Migrating " + fileName + " from v" + fileVersion + " to v" + currentVersion + "...");
            migration.migrate(y, fileVersion, currentVersion);
            y.set(versionKey, currentVersion);
            saveQuietly(y);
        }
    }

    private void saveQuietly(YamlConfiguration y) {
        try {
            y.save(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save " + fileName + ": " + e.getMessage(), e);
        }
    }
}
