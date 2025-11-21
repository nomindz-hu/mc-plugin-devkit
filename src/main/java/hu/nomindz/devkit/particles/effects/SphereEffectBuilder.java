package hu.nomindz.devkit.particles.effects;

import org.bukkit.Location;

import com.destroystokyo.paper.ParticleBuilder;

import hu.nomindz.devkit.particles.ParticleAnimation;

public class SphereEffectBuilder extends Effect<SphereEffectBuilder> {
    private double radius = 1.2;
    private boolean pulsate = true;
    private double heightOffset = 1.0;

    protected SphereEffectBuilder self() {
        return this;
    }

    public SphereEffectBuilder count(int count) {
        this.count = count;
        return this;
    }

    public SphereEffectBuilder radius(double radius) {
        this.radius = radius;
        return this;
    }

    public SphereEffectBuilder pulsate(boolean pulsate) {
        this.pulsate = pulsate;
        return this;
    }

    public SphereEffectBuilder heightOffset(double heightOffset) {
        this.heightOffset = heightOffset;
        return this;
    }

    public ParticleAnimation build() {
        return (ctx, tick) -> {
            if (tick >= this.durationInTicks) {
                return true;
            }

            double effectiveRadius = this.radius;

            if (this.pulsate) {
                double phase = tick / 5.0;
                effectiveRadius = this.radius * (0.85 + 0.15 * Math.sin(phase));
            }

            double area = 4.0 * Math.PI * radius * radius;

            int samples = (int) Math.round(area * 6.0 * fill);
            samples = (int) Math.clamp(samples, 8, 512);

            for (int i = 0; i < samples; i++) {
                double u = random.nextDouble();
                double v = random.nextDouble();

                double theta = 2 * Math.PI * u;
                double phi = Math.acos(2 * v - 1);

                double sinPhi = Math.sin(phi);
                double dx = effectiveRadius * sinPhi * Math.cos(theta);
                double dy = effectiveRadius * Math.cos(phi);
                double dz = effectiveRadius * sinPhi * Math.sin(theta);

                Location fxLocation = ctx.origin().add(0, this.heightOffset, 0).clone();
                fxLocation.add(dx, dy, dz);

                ParticleBuilder builder = this.baseBuilder(fxLocation);

                this.send(ctx, builder);
                this.handleHit(ctx, fxLocation);
            }

            return false;
        };
    }
}
