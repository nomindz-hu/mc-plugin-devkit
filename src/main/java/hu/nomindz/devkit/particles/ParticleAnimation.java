package hu.nomindz.devkit.particles;

@FunctionalInterface
public interface ParticleAnimation {   
    boolean tick(ParticleContext ctx, int tick);
}
