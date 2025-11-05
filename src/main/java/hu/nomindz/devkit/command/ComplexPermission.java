package hu.nomindz.devkit.command;

import org.bukkit.command.CommandSender;
import java.util.Arrays;
import java.util.List;

public record ComplexPermission(List<String> perms, Mode mode) {
    public enum Mode {
        ANY, ALL
    }

    public static ComplexPermission of(String perm) {
        return new ComplexPermission(List.of(perm), Mode.ANY);
    }

    public static ComplexPermission any(String... perms) {
        return new ComplexPermission(Arrays.asList(perms), Mode.ANY);
    }

    public static ComplexPermission all(String... perms) {
        return new ComplexPermission(Arrays.asList(perms), Mode.ALL);
    }

    public boolean test(CommandSender s) {
        if (perms == null || perms.isEmpty())
            return true;
        if (mode == Mode.ALL)
            return perms.stream().allMatch(s::hasPermission);
        return perms.stream().anyMatch(s::hasPermission);
    }

    public String describe() {
        return (mode == Mode.ALL ? "ALL " : "ANY ") + perms;
    }
}
