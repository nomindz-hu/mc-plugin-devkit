package hu.nomindz.devkit.particles;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class ParticleContext {
    private final World world;
    private final ParticleSource source;

    public ParticleContext(World world, ParticleSource source) {
        this.world = world;
        this.source = source;
    }

    public Location origin() {
        Location location = this.source.originSupplier().get();
        if (location.getWorld() == null) {
            location.setWorld(this.world);
        }

        return location;
    }

    public World world() {
        return this.world;
    }

    public Collection<Player> viewers() {
        return this.source.viewers();
    }

    public Player initiator() {
        return this.source.initiator();
    }
}
