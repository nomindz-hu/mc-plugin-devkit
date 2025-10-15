package hu.nomindz.devkit.config;

import org.bukkit.configuration.file.FileConfiguration;

@FunctionalInterface
public interface ConfigParser<T> {
    /** Build a typed config from Bukkit's FileConfiguration. Throw IllegalArgumentException on validation errors. */
    T parse(FileConfiguration cfg);
}
