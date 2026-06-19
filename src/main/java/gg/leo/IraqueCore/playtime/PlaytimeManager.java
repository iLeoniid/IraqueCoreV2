package gg.leo.IraqueCore.playtime;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlaytimeManager implements Listener {

    private final IraqueCore plugin;
    private final Map<UUID, Long> playtime = new HashMap<>();
    private final Map<UUID, Long> sessionStart = new HashMap<>();

    private File dataFile;
    private YamlConfiguration data;

    private boolean enabled;

    public PlaytimeManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        enabled = plugin.getConfig().getBoolean("playtime.enabled", true);
        dataFile = new File(plugin.getDataFolder(), "playtime.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getPluginLogger().error("Could not create playtime.yml", e);
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        loadPlaytime();
    }

    public void startTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::savePlaytime, 6000L, 6000L);
    }

    private void loadPlaytime() {
        if (data == null || !data.contains("players")) return;
        for (String key : data.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                long time = data.getLong("players." + key + ".time", 0);
                playtime.put(uuid, time);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void savePlaytime() {
        if (data == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            tickPlayer(player.getUniqueId());
        }

        data.set("players", null);
        for (Map.Entry<UUID, Long> entry : playtime.entrySet()) {
            String path = "players." + entry.getKey().toString();
            data.set(path + ".time", entry.getValue());
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save playtime.yml", e);
        }
    }

    public long getPlaytime(UUID uuid) {
        return playtime.getOrDefault(uuid, 0L);
    }

    public Map<UUID, Long> getPlaytimeMap() {
        return Collections.unmodifiableMap(playtime);
    }

    public static String formatTime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(secs).append("s");
        return sb.toString().trim();
    }

    private void tickPlayer(UUID uuid) {
        Long start = sessionStart.get(uuid);
        if (start == null) return;
        long elapsed = (System.currentTimeMillis() - start) / 1000;
        playtime.merge(uuid, elapsed, Long::sum);
        sessionStart.put(uuid, System.currentTimeMillis());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!enabled) return;
        UUID id = event.getPlayer().getUniqueId();
        playtime.putIfAbsent(id, 0L);
        sessionStart.put(id, System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!enabled) return;
        UUID id = event.getPlayer().getUniqueId();
        tickPlayer(id);
        sessionStart.remove(id);
        savePlaytime();
    }
}
