package hu.nomindz.devkit.command;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class MultiParamTypes {
    private MultiParamTypes() {
    }

    /**
     * Consumes exactly 3 tokens: x y z (double). World: sender's if Player, else
     * main world.
     */
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