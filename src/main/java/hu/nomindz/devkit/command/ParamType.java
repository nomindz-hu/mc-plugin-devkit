package hu.nomindz.devkit.command;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public interface ParamType<T> {
    T parse(Server server, CommandSender sender, String input) throws IllegalArgumentException;
}
