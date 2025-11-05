package hu.nomindz.devkit.command;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface MultiParamType<T> extends ParamType<T> {
    int arg_count();

    T parse(Server srv, CommandSender snd, List<String> args, int start) throws IllegalArgumentException;

    @Override
    default T parse(Server srv, CommandSender snd, String raw) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Single-token parse is not supported for MultiParamType");
    }
}