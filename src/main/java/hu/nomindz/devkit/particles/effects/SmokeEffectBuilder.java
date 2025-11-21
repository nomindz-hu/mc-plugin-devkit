package hu.nomindz.devkit.particles.effects;

import org.bukkit.Location;

import com.destroystokyo.paper.ParticleBuilder;

import hu.nomindz.devkit.particles.ParticleAnimation;

public class SmokeEffectBuilder extends Effect<SmokeEffectBuilder> {
    private double radius = 3.0;
    private double height = 4.0;

    protected SmokeEffectBuilder self() {
        return this;
    }

    public SmokeEffectBuilder radius(double radius) {
        this.radius = radius;
        return this;
    }

    public SmokeEffectBuilder height(double height) {
        this.height = height;
        return this;
    }

    public ParticleAnimation build() {
        return (ctx, tick) -> {
            if (tick >= this.durationInTicks) {
                return true;
            }
            Location fxCenter = ctx.origin();

            double baseYOffset = (tick / (double) this.durationInTicks) * this.height * 0.5;
            double volume = Math.PI * radius * radius * height;

            int samples = (int) Math.round(volume * 6.0 * this.fill);
            samples = (int) Math.clamp(samples, 8, 512);

            for (int i = 0; i < samples; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double r = Math.sqrt(random.nextDouble()) * this.radius;

                double dx = Math.cos(angle) * r;
                double dz = Math.sin(angle) * r;
                double dy = random.nextDouble() * (this.height * 0.5) + baseYOffset;

                Location fxLocation = fxCenter.clone().add(dx, dy, dz);

                ParticleBuilder builder = this.baseBuilder(fxLocation);

                this.send(ctx, builder);
                this.handleHit(ctx, fxLocation);
            }

            return false;
        };
    }
}
