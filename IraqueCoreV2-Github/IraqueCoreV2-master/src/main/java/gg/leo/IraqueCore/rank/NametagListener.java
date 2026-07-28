package gg.leo.IraqueCore.rank;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class NametagListener implements Listener {

    private final IraqueCore plugin;

    public NametagListener(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        plugin.getNametagDisplayManager().remove(e.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        rebuild(e.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        rebuild(e.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getFrom().getWorld() != e.getTo().getWorld()) {
            rebuild(e.getPlayer());
        }
    }

    private void rebuild(Player player) {
        plugin.getNametagDisplayManager().remove(player);
        plugin.getRankManager().updatePlayerRankVisuals(player);
    }
}