package hu.nomindz.devkit.command;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;

public interface CommandProvider {
    List<CommandBase> provide(JavaPlugin plugin);
}