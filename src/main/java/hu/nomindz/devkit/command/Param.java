package hu.nomindz.devkit.command;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public record Param<T>(String name, ParamType<T> type, boolean optional, T defaultValue,
        BiFunction<Server, CommandSender, List<String>> suggestions) {
    public static <T> Builder<T> of(String name, ParamType<T> type) {
        return new Builder<>(name, type);
    }

    public static final class Builder<T> {
        private final String name;
        private final ParamType<T> type;
        private boolean opt;
        private T def;
        private BiFunction<Server, CommandSender, List<String>> sugg = (s, c) -> Collections.emptyList();

        private Builder(String n, ParamType<T> t) {
            name = n;
            type = t;
        }

        public Builder<T> optional() {
            opt = true;
            return this;
        }

        public Builder<T> defaultValue(T v) {
            def = v;
            opt = true;
            return this;
        }

        public Builder<T> suggestions(BiFunction<Server, CommandSender, List<String>> f) {
            sugg = f;
            return this;
        }

        public Param<T> build() {
            return new Param<>(name, type, opt, def, sugg);
        }
    }
}
