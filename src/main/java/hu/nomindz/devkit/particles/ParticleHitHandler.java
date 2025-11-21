package hu.nomindz.devkit.particles;

@FunctionalInterface
public interface ParticleHitHandler {

    /**
     * @param hit info about this "collision" check
     * @return true if the whole animation should stop after this hit
     */
    boolean onHit(ParticleHit hit);
}
