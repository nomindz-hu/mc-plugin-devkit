package hu.nomindz.devkit.particles;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public final class ParticleHit {
    private final ParticleContext context;
    private final Location location;
    private final Collection<? extends Entity> entities;
    private final Collection<Block> blocks;

    public ParticleHit(
            ParticleContext context,
            Location location,
            Collection<? extends Entity> entities,
            Collection<Block> blocks) {
        this.context = context;
        this.location = location;
        this.entities = entities;
        this.blocks = blocks;
    }

    public ParticleContext context() {
        return context;
    }

    public Location location() {
        return location;
    }

    /** Nearby entities that were considered "hit" (can be empty). */
    public Collection<? extends Entity> entities() {
        return entities;
    }

    /** First solid block at the sample position, or null if none / not solid. */
    public Collection<Block> blocks() {
        return blocks;
    }

    public boolean hitAnything() {
        return (blocks != null && !blocks.isEmpty()) || (entities != null && !entities.isEmpty());
    }
}
