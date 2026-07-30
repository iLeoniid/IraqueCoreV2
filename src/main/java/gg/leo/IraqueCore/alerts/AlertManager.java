package gg.leo.IraqueCore.alerts;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public class AlertManager implements Listener {

    private final IraqueCore plugin;
    private final Map<String, Alert> alerts = new LinkedHashMap<>();
    private File alertsFile;
    private YamlConfiguration alertsConfig;

    public AlertManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        alerts.clear();

        alertsFile = new File(plugin.getDataFolder(), "create-alerts.yml");
        if (!alertsFile.exists()) {
            plugin.saveResource("create-alerts.yml", false);
        }
        alertsConfig = YamlConfiguration.loadConfiguration(alertsFile);

        loadAlerts();
    }

    public void reload() {
        alertsConfig = YamlConfiguration.loadConfiguration(alertsFile);
        loadAlerts();
    }

    private void loadAlerts() {
        ConfigurationSection section = alertsConfig.getConfigurationSection("alertas");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                String path = "alertas." + key;
                String title = alertsConfig.getString(path + ".title", "");
                String description = alertsConfig.getString(path + ".description", "");

                ConfigurationSection soundSection = alertsConfig.getConfigurationSection(path + ".sound");
                Alert.SoundConfig sound;
                if (soundSection != null && soundSection.getBoolean("enabled", true)) {
                    String soundId = soundSection.getString("id", "");
                    float volume = (float) soundSection.getDouble("volume", 1.0);
                    float pitch = (float) soundSection.getDouble("pitch", 1.0);
                    String source = soundSection.getString("source", "PLAYERS");
                    sound = new Alert.SoundConfig(soundId, volume, pitch, source, !soundId.isEmpty());
                } else {
                    sound = new Alert.SoundConfig("", 1.0f, 1.0f, "PLAYERS", false);
                }

                Alert alert = new Alert(key, title, description, sound);
                alerts.put(key, alert);
            } catch (Exception e) {
                plugin.getPluginLogger().error("Failed to load alert '{}'", key, e);
            }
        }

        plugin.getPluginLogger().info("Loaded {} alerts", alerts.size());
    }

    public Alert getAlert(String id) {
        return alerts.get(id);
    }

    public Map<String, Alert> getAlerts() {
        return Collections.unmodifiableMap(alerts);
    }

    public void sendAlert(Player player, Alert alert) {
        StringBuilder sb = new StringBuilder();

        String title = setPlaceholders(player, alert.title());
        String description = setPlaceholders(player, alert.description());

        if (!title.isEmpty()) {
            sb.append(title).append("\n");
        }
        if (!description.isEmpty()) {
            sb.append(description);
        }

        String result = sb.toString().stripTrailing();
        if (!result.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(result)));
        }

        playSound(player, alert.sound());
    }

    public void sendToAll(Player sender, Alert alert) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            sendAlert(online, alert);
        }
        plugin.getPluginLogger().info("[AlertSystem] Alert '{}' sent by {}", alert.id(), sender.getName());
    }

    public void sendToPlayer(Player target, Alert alert) {
        sendAlert(target, alert);
    }

    private void playSound(Player player, Alert.SoundConfig sound) {
        if (sound == null || !sound.enabled() || sound.id().isEmpty()) return;

        try {
            SoundCategory category = SoundCategory.PLAYERS;
            if (sound.source() != null && !sound.source().isEmpty()) {
                category = SoundCategory.valueOf(sound.source().toUpperCase());
            }
            player.playSound(player, sound.id(), category, sound.volume(), sound.pitch());
        } catch (Exception e) {
            plugin.getPluginLogger().warn("Could not play sound '{}': {}", sound.id(), e.getMessage());
        }
    }

    private String setPlaceholders(Player player, String text) {
        if (text == null || text.isEmpty()) return text;
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                Method method = papi.getMethod("setPlaceholders", Player.class, String.class);
                text = (String) method.invoke(null, player, text);
            }
        } catch (Exception ignored) {}
        return text;
    }
}
