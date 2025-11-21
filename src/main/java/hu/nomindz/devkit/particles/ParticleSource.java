package hu.nomindz.devkit.particles;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class ParticleSource {
    private final Supplier<Location> originSupplier;
    private final Collection<Player> viewers;
    private Player initiator;

    private ParticleSource(Supplier<Location> originSupplier, Collection<Player> viewers, Player initiator) {
        this.originSupplier = originSupplier;
        this.viewers = viewers;
        this.initiator = initiator;
    }

    public Supplier<Location> originSupplier() {
        return this.originSupplier;
    }

    public Collection<Player> viewers() {
        return this.viewers;
    }

    public Player initiator() {
        return this.initiator;
    }

    public ParticleSource initiator(Player player) {
        this.initiator = player;
        return this;
    }

    public static ParticleSource atLocation(Location location) {
        Location base = location.clone();
        return new ParticleSource(() -> base.clone(), Collections.emptyList(), null);
    }

    public static ParticleSource atLocation(Supplier<Location> locationSupplier) {
        return new ParticleSource(() -> locationSupplier.get(), Collections.emptyList(), null);
    }

    public static ParticleSource following(Entity entity) {
        return new ParticleSource(entity::getLocation, Collections.emptyList(),
                entity instanceof Player ? (Player) entity : null);
    }

    public static ParticleSource following(Entity entity, Collection<Player> viewers) {
        return new ParticleSource(entity::getLocation, viewers, entity instanceof Player ? (Player) entity : null);
    }

    public static ParticleSource withViewers(ParticleSource source, Collection<Player> viewers) {
        return new ParticleSource(source.originSupplier, viewers,
                source.initiator != null ? source.initiator : viewers.iterator().next());
    }
}
