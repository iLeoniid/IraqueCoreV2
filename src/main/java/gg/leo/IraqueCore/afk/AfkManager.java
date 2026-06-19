package gg.leo.IraqueCore.afk;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AfkManager implements Listener {

    private final IraqueCore plugin;
    private final Map<UUID, Long> lastActivity = new HashMap<>();
    private final Set<UUID> afkPlayers = new HashSet<>();

    private boolean enabled;
    private long afkTimeMs;
    private String afkPrefix;

    public AfkManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        var cfg = plugin.getConfig();
        enabled = cfg.getBoolean("afk.enabled", true);
        afkTimeMs = cfg.getLong("afk.time-minutes", 5) * 60 * 1000;
        afkPrefix = cfg.getString("afk.prefix", "&7[AFK] &7");
    }

    public void startTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!enabled) return;
            long now = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID id = player.getUniqueId();
                long last = lastActivity.getOrDefault(id, now);
                boolean currentlyAfk = afkPlayers.contains(id);

                if (!currentlyAfk && (now - last) >= afkTimeMs) {
                    setAfk(player, true);
                } else if (currentlyAfk && (now - last) < afkTimeMs) {
                    setAfk(player, false);
                }
            }
        }, 200L, 200L);
    }

    private void setAfk(Player player, boolean afk) {
        if (afk) {
            afkPlayers.add(player.getUniqueId());
            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().getPrefixedMessage("afk.now-afk")));
            plugin.getServer().broadcast(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("afk.broadcast-afk", "&7{player} &eis now AFK")
                                    .replace("{player}", player.getName()))));
        } else {
            afkPlayers.remove(player.getUniqueId());
            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().getPrefixedMessage("afk.no-longer-afk")));
            plugin.getServer().broadcast(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("afk.broadcast-return", "&7{player} &eis no longer AFK")
                                    .replace("{player}", player.getName()))));
        }
        plugin.getRankManager().updatePlayerRankVisuals(player);
    }

    private void updateActivity(UUID uuid) {
        if (!enabled) return;
        lastActivity.put(uuid, System.currentTimeMillis());
    }

    public boolean isAfk(UUID uuid) {
        return enabled && afkPlayers.contains(uuid);
    }

    public String getAfkPrefix() {
        return plugin.getConfigManager().translate(afkPrefix);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        updateActivity(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        updateActivity(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        updateActivity(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        updateActivity(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        afkPlayers.remove(id);
        lastActivity.remove(id);
    }
}
