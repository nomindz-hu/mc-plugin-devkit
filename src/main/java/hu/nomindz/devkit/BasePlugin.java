package hu.nomindz.devkit;

import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import hu.nomindz.devkit.config.BaseConfig;
import hu.nomindz.devkit.config.ConfigFactory;
import hu.nomindz.devkit.config.ConfigManager;
import hu.nomindz.devkit.managers.DatabaseManager;
import hu.nomindz.devkit.managers.TimerManager;
import hu.nomindz.devkit.particles.ParticleEngine;

/**
 * Base entrypoint for your plugin. This abstraction handles registering
 */
public abstract class BasePlugin<Plugin extends JavaPlugin, PluginConfig extends BaseConfig> extends JavaPlugin {
    private ConfigManager<PluginConfig> configManager;
    private DatabaseManager databaseManager;
    private TimerManager timerManager;
    private ParticleEngine particleEngine;

    protected abstract Plugin self();

    protected abstract Class<PluginConfig> configClass();

    protected String configFileName() {
        return "config.yml";
    }

    protected List<String> defaultConfigResources() {
        return List.of(this.configFileName());
    }

    protected String configVersionKey() {
        return "config_version";
    }

    protected int configVersion() {
        return 1;
    }

    protected String databaseSchemaPath() {
        return "schema.sql";
    }

    @Override
    public void onEnable() {
        // Initialize baseline requirement
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("Could not create data folder: " + getDataFolder());
            getServer().getPluginManager().disablePlugin(this.self());
            return;
        }

        this.configManager = ConfigFactory.create(
                this.self(),
                this.configClass(),
                this.configFileName(),
                this.defaultConfigResources(),
                this.configVersionKey(),
                this.configVersion(),
                null);
        this.configManager.loadOrCreate();

        this.databaseManager = DatabaseManager.getInstance(this.self(), () -> this.configManager.get().database());
        this.databaseManager.initializeFromResource(this.databaseSchemaPath());

        this.timerManager = TimerManager.getInstance(this.self());
        this.particleEngine = ParticleEngine.getInstance(this.self());
    }

    @Override
    public void onDisable() {
        if (this.timerManager != null) {
            this.timerManager.stopAll();
        }

        if (this.databaseManager != null) {
            this.databaseManager.close();
        }
    }

    public PluginConfig config() {
        return this.configManager.get();
    }

    public ConfigManager<PluginConfig> getConfigManager() {
        return this.configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    public TimerManager getTimer() {
        return this.timerManager;
    }

    public ParticleEngine getParticleEngine() {
        return this.particleEngine;
    }
}
