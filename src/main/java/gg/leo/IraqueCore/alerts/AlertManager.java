package gg.leo.IraqueCore.alerts;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
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
import java.time.Duration;
import java.util.*;

public class AlertManager implements Listener {

    private final IraqueCore plugin;
    private final Map<String, Alert> alerts = new LinkedHashMap<>();
    private File alertsFile;
    private File pendingFile;
    private YamlConfiguration alertsConfig;
    private YamlConfiguration pendingConfig;
    private boolean logToConsole;

    public AlertManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        alerts.clear();

        logToConsole = plugin.getConfig().getBoolean("settings.log-to-console", true);

        alertsFile = new File(plugin.getDataFolder(), "create-alerts.yml");
        if (!alertsFile.exists()) {
            plugin.saveResource("create-alerts.yml", false);
        }
        alertsConfig = YamlConfiguration.loadConfiguration(alertsFile);

        pendingFile = new File(plugin.getDataFolder(), "alerts-pending.yml");
        if (!pendingFile.exists()) {
            try {
                pendingFile.createNewFile();
            } catch (IOException e) {
                plugin.getPluginLogger().error("Could not create alerts-pending.yml", e);
            }
        }
        pendingConfig = YamlConfiguration.loadConfiguration(pendingFile);

        loadAlerts();
    }

    public void reload() {
        alertsConfig = YamlConfiguration.loadConfiguration(alertsFile);
        pendingConfig = YamlConfiguration.loadConfiguration(pendingFile);
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
                String message = alertsConfig.getString(path + ".message", "");
                String sendTypeStr = alertsConfig.getString(path + ".send-type", "CHAT");
                boolean saveForOffline = alertsConfig.getBoolean(path + ".save-for-offline", true);
                boolean removeAfterSend = alertsConfig.getBoolean(path + ".remove-after-send", true);
                int joinDelay = alertsConfig.getInt(path + ".join-delay", 0);

                Alert.SendType sendType;
                try {
                    sendType = Alert.SendType.valueOf(sendTypeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    sendType = Alert.SendType.CHAT;
                }

                ConfigurationSection soundSection = alertsConfig.getConfigurationSection(path + ".sound");
                Alert.SoundConfig sound;
                if (soundSection != null) {
                    String soundId = soundSection.getString("id", "");
                    String category = soundSection.getString("category", "PLAYERS");
                    float volume = (float) soundSection.getDouble("volume", 1.0);
                    float pitch = (float) soundSection.getDouble("pitch", 1.0);
                    boolean soundEnabled = soundSection.getBoolean("enabled", true);
                    sound = new Alert.SoundConfig(soundId, category, volume, pitch, soundEnabled);
                } else {
                    sound = new Alert.SoundConfig("", "PLAYERS", 1.0f, 1.0f, false);
                }

                Alert alert = new Alert(key, title, description, message, sound, sendType,
                        saveForOffline, removeAfterSend, joinDelay);
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
        String message = setPlaceholders(player, alert.message());
        String title = setPlaceholders(player, alert.title());
        String description = setPlaceholders(player, alert.description());

        switch (alert.sendType()) {
            case CHAT -> {
                if (!message.isEmpty()) {
                    player.sendMessage(deserialize(message));
                }
            }
            case ACTION_BAR -> {
                if (!message.isEmpty()) {
                    player.sendActionBar(deserialize(message));
                }
            }
            case TITLE -> {
                if (!title.isEmpty()) {
                    player.showTitle(Title.title(
                            deserialize(title),
                            description.isEmpty() ? Component.empty() : deserialize(description),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(1))
                    ));
                } else if (!message.isEmpty()) {
                    player.sendMessage(deserialize(message));
                }
            }
            case SUBTITLE -> {
                if (!description.isEmpty()) {
                    player.showTitle(Title.title(
                            Component.empty(),
                            deserialize(description),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(1))
                    ));
                } else if (!message.isEmpty()) {
                    player.sendMessage(deserialize(message));
                }
            }
            case COMBINED -> {
                if (!title.isEmpty() || !description.isEmpty()) {
                    player.showTitle(Title.title(
                            title.isEmpty() ? Component.empty() : deserialize(title),
                            description.isEmpty() ? Component.empty() : deserialize(description),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(1))
                    ));
                }
                if (!message.isEmpty()) {
                    player.sendMessage(deserialize(message));
                }
            }
        }

        playSound(player, alert.sound());
    }

    public void sendToAll(Player sender, Alert alert) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            sendAlert(online, alert);
        }

        if (alert.saveForOffline()) {
            savePendingAlert(alert, List.of());
        }

        if (logToConsole) {
            plugin.getPluginLogger().info("[AlertSystem] Alert '{}' sent by {}", alert.id(), sender.getName());
        }
    }

    public void sendToPlayer(Player target, Alert alert) {
        sendAlert(target, alert);
    }

    private void playSound(Player player, Alert.SoundConfig sound) {
        if (sound == null || !sound.enabled() || sound.id().isEmpty()) return;

        try {
            SoundCategory category;
            try {
                category = SoundCategory.valueOf(sound.category().toUpperCase());
            } catch (IllegalArgumentException e) {
                category = SoundCategory.PLAYERS;
            }

            player.playSound(player, sound.id(), category, sound.volume(), sound.pitch());
        } catch (Exception e) {
            plugin.getPluginLogger().warn("Could not play sound '{}' for alert: {}", sound.id(), e.getMessage());
        }
    }

    private void savePendingAlert(Alert alert, List<String> sentTo) {
        String path = "pending-alerts." + alert.id();
        pendingConfig.set(path + ".title", alert.title());
        pendingConfig.set(path + ".description", alert.description());
        pendingConfig.set(path + ".message", alert.message());

        if (alert.sound() != null) {
            pendingConfig.set(path + ".sound.id", alert.sound().id());
            pendingConfig.set(path + ".sound.category", alert.sound().category());
            pendingConfig.set(path + ".sound.volume", alert.sound().volume());
            pendingConfig.set(path + ".sound.pitch", alert.sound().pitch());
        }

        pendingConfig.set(path + ".send-type", alert.sendType().name());
        pendingConfig.set(path + ".sent-to", new ArrayList<>(sentTo));
        pendingConfig.set(path + ".timestamp", System.currentTimeMillis() / 1000);

        savePendingFile();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        if (!pendingConfig.contains("pending-alerts")) return;

        ConfigurationSection section = pendingConfig.getConfigurationSection("pending-alerts");
        if (section == null) return;

        for (String alertId : section.getKeys(false)) {
            String path = "pending-alerts." + alertId;
            List<String> sentTo = pendingConfig.getStringList(path + ".sent-to");
            if (sentTo.contains(playerName)) continue;

            String title = pendingConfig.getString(path + ".title", "");
            String description = pendingConfig.getString(path + ".description", "");
            String message = pendingConfig.getString(path + ".message", "");
            String sendTypeStr = pendingConfig.getString(path + ".send-type", "CHAT");

            Alert.SendType sendType;
            try {
                sendType = Alert.SendType.valueOf(sendTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                sendType = Alert.SendType.CHAT;
            }

            ConfigurationSection soundSection = pendingConfig.getConfigurationSection(path + ".sound");
            Alert.SoundConfig sound;
            if (soundSection != null) {
                String soundId = soundSection.getString("id", "");
                String category = soundSection.getString("category", "PLAYERS");
                float volume = (float) soundSection.getDouble("volume", 1.0);
                float pitch = (float) soundSection.getDouble("pitch", 1.0);
                sound = new Alert.SoundConfig(soundId, category, volume, pitch, true);
            } else {
                sound = new Alert.SoundConfig("", "PLAYERS", 1.0f, 1.0f, false);
            }

            Alert pendingAlert = new Alert(alertId, title, description, message, sound,
                    sendType, true, false, 0);

            int delay = pendingConfig.getInt(path + ".join-delay", 0);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    sendAlert(player, pendingAlert);
                }
            }, delay * 20L);

            sentTo.add(playerName);
            pendingConfig.set(path + ".sent-to", sentTo);
            savePendingFile();
        }
    }

    public void cleanPending(String alertId) {
        pendingConfig.set("pending-alerts." + alertId, null);
        savePendingFile();
    }

    private void savePendingFile() {
        try {
            pendingConfig.save(pendingFile);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save alerts-pending.yml", e);
        }
    }

    public void markReceived(String alertId, Player player) {
        String path = "pending-alerts." + alertId;
        if (!pendingConfig.contains(path)) return;

        List<String> sentTo = pendingConfig.getStringList(path + ".sent-to");
        if (!sentTo.contains(player.getName())) {
            sentTo.add(player.getName());
            pendingConfig.set(path + ".sent-to", sentTo);
            savePendingFile();
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

    private Component deserialize(String text) {
        return plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(text));
    }
}
