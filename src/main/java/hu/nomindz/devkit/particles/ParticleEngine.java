package hu.nomindz.devkit.particles;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class ParticleEngine {
    private static ParticleEngine instance;
    private final JavaPlugin plugin;
    private final Map<Integer, BukkitTask> running;

    private ParticleEngine(JavaPlugin plugin) {
        this.plugin = plugin;
        this.running = new ConcurrentHashMap<>();
    }

    public static ParticleEngine getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new ParticleEngine(plugin);
        }

        return instance;
    }

    public BukkitTask play(ParticleAnimation animation, ParticleSource source) {
        Location initialLocation = source.originSupplier().get();
        if (initialLocation == null || initialLocation.getWorld() == null) {
            return null;
        }

        ParticleContext ctx = new ParticleContext(initialLocation.getWorld(), source);

        BukkitTask task = new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                boolean finished = false;

                try {
                    finished = animation.tick(ctx, tick);
                } catch (Throwable throwable) {
                    plugin.getLogger().warning("Error in animation: " + throwable.getMessage());
                    throwable.printStackTrace();
                    finished = true;
                }

                if (finished && !this.isCancelled()) {
                    this.cancel();
                }

                tick++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);

        this.running.put(task.getTaskId(), task);

        return task;
    }
}
