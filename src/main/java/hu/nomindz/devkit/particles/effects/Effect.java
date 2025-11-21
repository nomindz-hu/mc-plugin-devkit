package hu.nomindz.devkit.particles.effects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.destroystokyo.paper.ParticleBuilder;

import hu.nomindz.devkit.particles.ParticleAnimation;
import hu.nomindz.devkit.particles.ParticleContext;
import hu.nomindz.devkit.particles.ParticleHit;
import hu.nomindz.devkit.particles.ParticleHitHandler;

public abstract class Effect<EffectBuilder extends Effect<EffectBuilder>> {
    protected static final Random random = new Random();

    protected Particle particle;
    protected Color dustColor = Color.WHITE;
    protected float dustSize = 1.0f;
    protected int durationInTicks = 20;
    protected int count = 0;
    protected double extra = 0.0;

    protected double fill = 1.0;

    protected ParticleHitHandler hitHandler;
    protected double hitRadius = 0.5;

    protected abstract EffectBuilder self();

    public abstract ParticleAnimation build();

    public EffectBuilder particle(Particle particle) {
        this.particle = particle;
        return this.self();
    }

    public EffectBuilder dustColor(Color dustColor) {
        this.dustColor = dustColor;
        return this.self();
    }

    public EffectBuilder dustSize(float dustSize) {
        this.dustSize = dustSize;
        return this.self();
    }

    public EffectBuilder duration(int durationInTicks) {
        this.durationInTicks = durationInTicks;
        return this.self();
    }

    public EffectBuilder count(int count) {
        this.count = count;
        return this.self();
    }

    public EffectBuilder extra(double extra) {
        this.extra = Math.max(0.0, extra);
        return this.self();
    }

    public EffectBuilder fill(int percent) {
        double p = Math.max(0, Math.min(100, percent));
        this.fill = p / 100.0;
        return self();
    }

    public EffectBuilder hitRadius(double hitRadius) {
        this.hitRadius = Math.max(0.0, hitRadius);
        return this.self();
    }

    public EffectBuilder onHit(ParticleHitHandler onHit) {
        this.hitHandler = onHit;
        return this.self();
    }

    protected ParticleBuilder baseBuilder(Location fxLocation) {
        ParticleBuilder builder = this.particle
                .builder()
                .location(fxLocation)
                .count(this.count)
                .extra(this.extra)
                .offset(0, 0, 0);

        if (this.particle == Particle.DUST) {
            builder.data(new Particle.DustOptions(this.dustColor, this.dustSize));
        }

        return builder;
    }

    protected void send(ParticleContext ctx, ParticleBuilder builder) {
        Collection<Player> viewers = ctx.viewers();

        if (viewers != null && !viewers.isEmpty()) {
            builder.receivers(viewers).spawn();
        } else {
            builder.receivers(32, true).spawn();
        }
    }

    protected boolean handleHit(ParticleContext ctx, Location location) {
        Collection<Block> blockHits = new ArrayList<>();
        Collection<Entity> entityHits = new ArrayList<>();

        Block block = location.getBlock();
        if (block.getType().isSolid()) {
            blockHits.add(block);
        }

        Collection<Entity> nearbyEntities = ctx.world().getNearbyEntities(location, 0.5, 0.5, 0.5);

        if (!nearbyEntities.isEmpty()) {
            nearbyEntities.remove(ctx.initiator());
            entityHits.addAll(nearbyEntities);
        }

        if (blockHits.isEmpty() && entityHits.isEmpty()) {
            return false;
        }

        ParticleHit hit = new ParticleHit(ctx, location, entityHits, blockHits);
        boolean hitHookResult = false;

        if (this.hitHandler != null) {
            hitHookResult = this.hitHandler.onHit(hit);
        }

        if (!blockHits.isEmpty() || hitHookResult) {
            return true;
        }

        return false;
    }
}
