package hu.nomindz.devkit.particles.effects;

public class Effects {
    private Effects() {
    }

    public static BurstEffectBuilder burst() {
        return new BurstEffectBuilder();
    }

    public static DiskEffectBuilder disk() {
        return new DiskEffectBuilder();
    }

    public static SphereEffectBuilder sphere() {
        return new SphereEffectBuilder();
    }

    public static SmokeEffectBuilder smoke() {
        return new SmokeEffectBuilder();
    }
}
