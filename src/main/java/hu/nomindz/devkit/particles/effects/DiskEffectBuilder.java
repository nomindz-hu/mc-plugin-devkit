package hu.nomindz.devkit.particles.effects;

import org.bukkit.Location;

import com.destroystokyo.paper.ParticleBuilder;

import hu.nomindz.devkit.particles.ParticleAnimation;

public class DiskEffectBuilder extends Effect<DiskEffectBuilder> {
    private double radius;

    protected DiskEffectBuilder self() {
        return this;
    }

    public DiskEffectBuilder radius(double radius) {
        this.radius = radius;
        return this;
    }

    public ParticleAnimation build() {
        return (ctx, tick) -> {
            if (tick >= this.durationInTicks) {
                return true;
            }

            Location fxCenter = ctx.origin().clone().add(0, 0.1, 0);

            double keyframe = (double) tick / this.durationInTicks;
            double radius = this.radius * keyframe;

            int density = (int) Math.round(radius * radius * 20 * this.fill);

            for (int i = 0; i < density; i++) {
                double u = Math.random();
                double v = Math.random();

                double r = radius * Math.sqrt(u);
                double theta = 2 * Math.PI * v;

                double dx = r * Math.cos(theta);
                double dz = r * Math.sin(theta);

                Location fxLocation = fxCenter.clone().add(dx, 0, dz);

                ParticleBuilder builder = this.baseBuilder(fxLocation);

                this.send(ctx, builder);
                this.handleHit(ctx, fxLocation);
            }

            return false;
        };
    }
}
