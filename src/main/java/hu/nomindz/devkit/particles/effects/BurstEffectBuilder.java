package hu.nomindz.devkit.particles.effects;

import org.bukkit.Location;

import com.destroystokyo.paper.ParticleBuilder;

import hu.nomindz.devkit.particles.ParticleAnimation;

public class BurstEffectBuilder extends Effect<BurstEffectBuilder> {
    private double maxRadius;

    @Override
    protected BurstEffectBuilder self() {
        return this;
    }

    public BurstEffectBuilder radius(double radius) {
        this.maxRadius = radius;
        return this;
    }

    public ParticleAnimation build() {
        return (ctx, tick) -> {
            if (tick >= this.durationInTicks) {
                return true;
            }

            Location fxCenter = ctx.origin().clone().add(0, 0.1, 0);

            double keyframe = (double) tick / this.durationInTicks;
            double radius = this.maxRadius * keyframe;

            double circumference = 2.0 * Math.PI * radius;
            int samples = (int) Math.round(circumference * 6.0 * this.fill);
            samples = (int) Math.clamp(samples, 8, 512);

            double angleStep = (2 * Math.PI) / samples;

            for (int i = 0; i < samples; i++) {
                double theta = i * angleStep;
                double dx = radius * Math.cos(theta);
                double dz = radius * Math.sin(theta);

                Location fxLocation = fxCenter.clone().add(dx, 0, dz);

                ParticleBuilder builder = this.baseBuilder(fxLocation);

                this.send(ctx, builder);
                this.handleHit(ctx, fxLocation);
            }

            return false;
        };
    }
}