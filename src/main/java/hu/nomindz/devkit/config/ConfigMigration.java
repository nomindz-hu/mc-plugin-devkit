package hu.nomindz.devkit.config;

import org.bukkit.configuration.file.YamlConfiguration;

public interface ConfigMigration {
    /** Mutate Yaml tree from 'from' to 'to'. Implement stepwise migrations as needed. */
    void migrate(YamlConfiguration yaml, int from, int to);
}
