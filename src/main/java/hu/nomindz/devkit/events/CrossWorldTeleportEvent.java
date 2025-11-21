package hu.nomindz.devkit.events;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class CrossWorldTeleportEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private final Player player;
    private final Location from;
    private final Location to;
    private final World fromWorld;
    private final World toWorld;
    private final Environment fromEnvironment;
    private final Environment toEnvironment;
    private final TeleportCause cause;

    private boolean cancelled = false;

    public CrossWorldTeleportEvent(
            Player player,
            Location from,
            Location to,
            World fromWorld,
            World toWorld,
            Environment fromEnvironment,
            Environment toEnvironment,
            TeleportCause cause) {
        super();
        this.player = player;
        this.from = from;
        this.to = to;
        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
        this.fromEnvironment = fromEnvironment;
        this.toEnvironment = toEnvironment;
        this.cause = cause;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Location getFrom() {
        return this.from;
    }

    public Location getTo() {
        return this.to;
    }

    public World getFromWorld() {
        return this.fromWorld;
    }

    public World getToWorld() {
        return this.toWorld;
    }

    public World.Environment getFromEnvironment() {
        return this.fromEnvironment;
    }

    public World.Environment getToEnvironment() {
        return this.toEnvironment;
    }

    public TeleportCause getCause() {
        return this.cause;
    }

    public boolean isSameWorld() {
        return this.fromWorld == this.toWorld;
    }

    public boolean isSameEnvironment() {
        return this.fromEnvironment == this.toEnvironment;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
