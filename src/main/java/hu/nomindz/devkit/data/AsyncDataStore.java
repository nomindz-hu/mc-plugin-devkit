package hu.nomindz.devkit.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.plugin.java.JavaPlugin;

public class AsyncDataStore<T> {
    private static Map<String, AsyncDataStore<?>> instances = new ConcurrentHashMap<>();

    private JavaPlugin plugin;
    private final String storeName;
    private final Path baseDir;
    private final DataSerializer<T> serializer;
    private final ExecutorService io;

    private AsyncDataStore(JavaPlugin plugin, String name, DataSerializer<T> serializer) {
        this.plugin = plugin;
        this.storeName = name;
        this.baseDir = this.plugin.getDataFolder().toPath().resolve(this.storeName);
        this.serializer = serializer;

        this.io = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, String.format("%s_IO", this.storeName));
            t.setDaemon(true);
            return t;
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> AsyncDataStore<T> get(JavaPlugin plugin, String name, DataSerializer<T> serializer) {
        if (!instances.containsKey(name)) {
            instances.put(name, new AsyncDataStore<>(plugin, name, serializer));
        }

        return (AsyncDataStore<T>) instances.get(name);
    }

    private Path fileForKey(String key) {
        return baseDir.resolve(key + ".dat");
    }

    public CompletableFuture<Void> save(String key, T value) {
        Path path = this.fileForKey(key);
        return CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(path.getParent());
                try (OutputStream out = Files.newOutputStream(path)) {
                    this.serializer.serialize(value, out);
                }
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }, this.io);
    }

    public CompletableFuture<Optional<T>> load(String key) {
        Path path = this.fileForKey(key);
        return CompletableFuture.supplyAsync(() -> {
            if (!Files.exists(path)) {
                return Optional.empty();
            }
            try (InputStream in = Files.newInputStream(path)) {
                T value = this.serializer.deserialize(in);
                return Optional.ofNullable(value);
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }, this.io);
    }
}
