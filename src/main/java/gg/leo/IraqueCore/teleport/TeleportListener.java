package gg.leo.IraqueCore.teleport;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TeleportListener implements Listener {

    private final TeleportManager manager;

    public TeleportListener(TeleportManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!manager.isTeleporting(player.getUniqueId())) return;
        if (!manager.isCancelOnMove()) return;
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        if (from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            manager.cancelTeleport(player.getUniqueId(), "teleport.cancelled-move");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!manager.isTeleporting(player.getUniqueId())) return;
        if (!manager.isCancelOnDamage()) return;
        manager.cancelTeleport(player.getUniqueId(), "teleport.cancelled-damage");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.cancelTeleport(event.getPlayer().getUniqueId(), false);
    }
}
