package gg.leo.IraqueCore.teleport;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {

    private final IraqueCore plugin;
    private final Map<UUID, PendingTeleport> pending = new HashMap<>();

    public TeleportManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void requestTeleport(Player player, Location destination) {
        cancelTeleport(player.getUniqueId(), false);
        int delay = plugin.getConfigManager().getTeleportDelay();
        if (delay <= 0) {
            player.teleport(destination);
            return;
        }
        PendingTeleport pt = new PendingTeleport(player, destination, delay * 20);
        pending.put(player.getUniqueId(), pt);
        pt.start();
    }

    public void cancelTeleport(UUID uuid, boolean notify) {
        PendingTeleport pt = pending.remove(uuid);
        if (pt == null) return;
        pt.stop();
        if (notify) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                send(p, "teleport.cancelled");
            }
        }
    }

    public void cancelTeleport(UUID uuid, String messageKey) {
        PendingTeleport pt = pending.remove(uuid);
        if (pt == null) return;
        pt.stop();
        Player p = Bukkit.getPlayer(uuid);
        if (p != null && p.isOnline()) {
            send(p, messageKey);
        }
    }

    public boolean isTeleporting(UUID uuid) {
        return pending.containsKey(uuid);
    }

    public boolean isCancelOnMove() {
        return plugin.getConfigManager().isTeleportCancelOnMove();
    }

    public boolean isCancelOnDamage() {
        return plugin.getConfigManager().isTeleportCancelOnDamage();
    }

    private void send(Player player, String messageKey) {
        player.sendActionBar(plugin.getConfigManager().getMessageComponent(messageKey, "&cTeleport cancelado!"));
    }

    private void sendStart(Player player, int seconds) {
        String msg = plugin.getConfigManager().getMessage("teleport.start", "&aTeleportando em &e{seconds}s&a... não se mova!")
                .replace("{seconds}", String.valueOf(seconds));
        player.sendActionBar(plugin.getConfigManager().deserialize(plugin.getConfigManager().translate(msg)));
    }

    private class PendingTeleport {
        private final Player player;
        private final Location destination;
        private int remainingTicks;
        private BukkitTask task;

        PendingTeleport(Player player, Location destination, int totalTicks) {
            this.player = player;
            this.destination = destination;
            this.remainingTicks = totalTicks;
        }

        void start() {
            sendStart(player, (int) Math.ceil(remainingTicks / 20.0));
            task = SchedulerUtil.runTimer(plugin, () -> {
                if (!player.isOnline()) {
                    cancelTeleport(player.getUniqueId(), false);
                    return;
                }
                remainingTicks -= 20;
                if (remainingTicks <= 0) {
                    PendingTeleport removed = pending.remove(player.getUniqueId());
                    if (removed != null) {
                        task.cancel();
                        player.teleport(destination);
                        player.sendActionBar(plugin.getConfigManager().getMessageComponent("teleport.complete", "&aTeleportado!"));
                    }
                } else {
                    sendStart(player, (int) Math.ceil(remainingTicks / 20.0));
                }
            }, 20L, 20L);
        }

        void stop() {
            if (task != null) {
                task.cancel();
            }
        }
    }
}
