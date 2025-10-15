package hu.nomindz.devkit.config;

import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigFactory {
    private ConfigFactory() {
    }

    public static AutoConfigBinder defaultBinder() {
        var mapper = AutoConfigBinder.defaultMapper();
        jakarta.validation.Validator validator = null;
        try {
            validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        } catch (Throwable ignored) {
        }
        return new AutoConfigBinder(mapper, validator);
    }

    public static <T> ConfigManager<T> create(
            JavaPlugin plugin,
            Class<T> type,
            String fileName,
            java.util.List<String> defaultsResources,
            String versionKey,
            int currentVersion,
            ConfigMigration migration) {
        return new ConfigManager<>(
                plugin, type, fileName, defaultsResources, versionKey, currentVersion, migration, defaultBinder());
    }
}
