package hu.nomindz.devkit.command;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import java.util.*;

public final class CommandBase {
    @FunctionalInterface
    public interface Exec {
        void run(Server server, CommandSender sender, Map<String, Object> args) throws Exception;
    }

    public final String name;
    public final List<String> aliases;
    public final String description;
    public final ComplexPermission perm;
    public final boolean playerOnly;
    public final List<Param<?>> params;
    public final Map<String, CommandBase> children;
    public final Exec exec;
    public final String help;

    private CommandBase(Builder b) {
        this.name = b.name;
        this.aliases = List.copyOf(b.aliases);
        this.description = b.description;
        this.perm = b.perm;
        this.playerOnly = b.playerOnly;
        this.params = List.copyOf(b.params);
        this.children = Map.copyOf(b.children);
        this.exec = b.exec;
        this.help = b.help;
    }

    public static Builder of(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String name;
        private final List<String> aliases = new ArrayList<>();
        private String description = "";
        private ComplexPermission perm = ComplexPermission.any();
        private boolean playerOnly = false;
        private final List<Param<?>> params = new ArrayList<>();
        private final Map<String, CommandBase> children = new LinkedHashMap<>();
        private Exec exec = (server, sender, args) -> sender.sendMessage("Not implemented.");
        private String help = null;

        private Builder(String name) {
            this.name = name;
        }

        public Builder alias(String... alias) {
            this.aliases.addAll(Arrays.asList(alias));
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder permission(ComplexPermission permission) {
            this.perm = permission;
            return this;
        }

        public Builder playerOnly() {
            this.playerOnly = true;
            return this;
        }

        public Builder param(Param<?> param) {
            this.params.add(param);
            return this;
        }

        public Builder child(CommandBase command) {
            this.children.put(command.name.toLowerCase(), command);
            command.aliases.forEach(alias -> children.put(alias.toLowerCase(), command));
            return this;
        }

        public Builder help(String helpMessage) {
            this.help = helpMessage;
            return this;
        }

        public Builder executor(Exec executor) {
            this.exec = executor;
            return this;
        }

        public CommandBase build() {
            return new CommandBase(this);
        }
    }
}
