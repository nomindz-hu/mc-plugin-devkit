package hu.nomindz.devkit.command;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public final class ParamTypes {
    private ParamTypes() {
    }

    public static final ParamType<String> STRING = (server, sender, input) -> input;
    public static final ParamType<Integer> INT = (server, sender, input) -> {
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
            throw new IllegalArgumentException("Expected integer, got '" + input + "'");
        }
    };
    public static final ParamType<Boolean> BOOL = (server, sender, input) -> {
        String r = input.toLowerCase(Locale.ROOT);
        if (r.matches("true|yes|y|1"))
            return true;
        if (r.matches("false|no|n|0"))
            return false;
        throw new IllegalArgumentException("Expected boolean (true/false), got '" + input + "'");
    };
    public static final ParamType<Player> PLAYER = (server, sender, input) -> {
        Player p = server.getPlayerExact(input);
        if (p == null)
            throw new IllegalArgumentException("Player not online: " + input);
        return p;
    };

    public static <E extends Enum<E>> ParamType<E> ofEnum(Class<E> e) {
        return (server, sender, input) -> {
            try {
                return Enum.valueOf(e, input.toUpperCase(Locale.ROOT));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Expected one of: " + String.join(", ",
                        java.util.Arrays.stream(e.getEnumConstants()).map(x -> x.name().toLowerCase()).toList()));
            }
        };
    }

    public static final MultiParamType<Location> LOCATION_XYZ = new MultiParamType<>() {
        @Override
        public int arg_count() {
            return 3;
        }

        @Override
        public Location parse(Server srv, CommandSender snd, List<String> args, int start) {
            try {
                double x = Double.parseDouble(args.get(start));
                double y = Double.parseDouble(args.get(start + 1));
                double z = Double.parseDouble(args.get(start + 2));
                World w = (snd instanceof Player p) ? p.getWorld() : srv.getWorlds().get(0);
                return new Location(w, x, y, z);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Expected <x y z> as numbers.");
            }
        }
    };
}
