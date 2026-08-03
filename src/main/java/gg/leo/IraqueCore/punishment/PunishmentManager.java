package gg.leo.IraqueCore.punishment;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.SchedulerUtil;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PunishmentManager implements Listener {

    private final IraqueCore plugin;
    private final Map<UUID, MuteEntry> mutes = new HashMap<>();
    private File muteFile;
    private FileConfiguration muteConfig;

    public PunishmentManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        muteFile = new File(plugin.getDataFolder(), "punishments.yml");
        if (!muteFile.exists()) {
            try {
                muteFile.getParentFile().mkdirs();
                muteFile.createNewFile();
            } catch (IOException e) {
                plugin.getPluginLogger().error("Could not create punishments.yml", e);
            }
        }
        muteConfig = YamlConfiguration.loadConfiguration(muteFile);
        loadMutes();
        startMuteCheck();
    }

    private void loadMutes() {
        mutes.clear();
        if (!muteConfig.contains("mutes")) return;
        for (String key : muteConfig.getConfigurationSection("mutes").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String reason = muteConfig.getString("mutes." + key + ".reason", "No reason");
                String source = muteConfig.getString("mutes." + key + ".source", "Console");
                long expiry = muteConfig.getLong("mutes." + key + ".expiry", -1);
                mutes.put(uuid, new MuteEntry(reason, source, expiry));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void reload() {
        muteFile = new File(plugin.getDataFolder(), "punishments.yml");
        muteConfig = YamlConfiguration.loadConfiguration(muteFile);
        loadMutes();
    }

    public void saveMutes() {
        if (muteConfig == null) return;
        muteConfig.set("mutes", null);
        for (Map.Entry<UUID, MuteEntry> entry : mutes.entrySet()) {
            String path = "mutes." + entry.getKey() + ".";
            muteConfig.set(path + "reason", entry.getValue().reason);
            muteConfig.set(path + "source", entry.getValue().source);
            muteConfig.set(path + "expiry", entry.getValue().expiry);
        }
        try {
            muteConfig.save(muteFile);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save punishments.yml", e);
        }
    }

    private void saveMute(UUID uuid, MuteEntry entry) {
        String path = "mutes." + uuid + ".";
        muteConfig.set(path + "reason", entry.reason);
        muteConfig.set(path + "source", entry.source);
        muteConfig.set(path + "expiry", entry.expiry);
        try {
            muteConfig.save(muteFile);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save punishments.yml", e);
        }
    }

    private void startMuteCheck() {
        SchedulerUtil.runTimer(plugin, () -> {
            boolean changed = false;
            Iterator<Map.Entry<UUID, MuteEntry>> it = mutes.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue().isExpired()) {
                    it.remove();
                    changed = true;
                }
            }
            if (changed) saveMutes();
        }, 600L, 600L);
    }

    public void ban(Player target, String source, String reason, long durationMillis, boolean silent) {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        Date expiry = durationMillis > 0 ? Date.from(Instant.now().plusMillis(durationMillis)) : null;
        String banReason = formatBanReason(source, reason);
        banList.addBan(target.getName(), banReason, expiry, source);

        Component kickMsg = plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(banReason));
        target.kick(kickMsg);

        broadcastBan(target, source, reason, durationMillis, silent);
    }

    public void unban(String playerName) {
        Bukkit.getBanList(BanList.Type.NAME).pardon(playerName);
    }

    public boolean isBanned(String playerName) {
        return Bukkit.getBanList(BanList.Type.NAME).isBanned(playerName);
    }

    public void mute(Player target, String source, String reason, long durationMillis) {
        long expiry = durationMillis > 0 ? System.currentTimeMillis() + durationMillis : -1;
        MuteEntry entry = new MuteEntry(reason, source, expiry);
        mutes.put(target.getUniqueId(), entry);
        saveMute(target.getUniqueId(), entry);
    }

    public void unmute(UUID uuid) {
        mutes.remove(uuid);
        muteConfig.set("mutes." + uuid, null);
        try {
            muteConfig.save(muteFile);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save punishments.yml", e);
        }
    }

    public boolean isMuted(UUID uuid) {
        MuteEntry entry = mutes.get(uuid);
        if (entry == null) return false;
        if (entry.isExpired()) {
            mutes.remove(uuid);
            saveMutes();
            return false;
        }
        return true;
    }

    public MuteEntry getMuteEntry(UUID uuid) {
        MuteEntry entry = mutes.get(uuid);
        if (entry != null && entry.isExpired()) {
            mutes.remove(uuid);
            saveMutes();
            return null;
        }
        return entry;
    }

    public void kick(Player target, String source, String reason) {
        String kickReason = formatKickReason(source, reason);
        Component kickMsg = plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(kickReason));
        target.kick(kickMsg);
    }

    private String formatBanReason(String source, String reason) {
        String template = plugin.getConfigManager().getMessage("punishment.ban.screen",
                "&cYou have been banned.\n&7Reason: &f{reason}\n&7By: &f{source}");
        return template.replace("{reason}", reason).replace("{source}", source);
    }

    private String formatKickReason(String source, String reason) {
        String template = plugin.getConfigManager().getMessage("punishment.kick.screen",
                "&cYou have been kicked.\n&7Reason: &f{reason}\n&7By: &f{source}");
        return template.replace("{reason}", reason).replace("{source}", source);
    }

    private void broadcastBan(Player target, String source, String reason, long duration, boolean silent) {
        String durStr = duration > 0 ? formatDuration(duration) : "Permanent";
        String path = silent ? "punishment.ban.silent-broadcast" : "punishment.ban.broadcast";
        String fallback = silent
                ? "&7[Silent] &c{player} &7was banned by &c{source} &7| Reason: &f{reason} &7| Duration: &f{duration}"
                : "&c{player} &7was banned by &c{source} &7| Reason: &f{reason} &7| Duration: &f{duration}";
        broadcast(path, fallback, target.getName(), source, reason, durStr, silent);
    }

    public void broadcastMute(Player target, String source, String reason, long duration, boolean silent) {
        String durStr = duration > 0 ? formatDuration(duration) : "Permanent";
        String path = silent ? "punishment.mute.silent-broadcast" : "punishment.mute.broadcast";
        String fallback = silent
                ? "&7[Silent] &c{player} &7was muted by &c{source} &7| Reason: &f{reason} &7| Duration: &f{duration}"
                : "&c{player} &7was muted by &c{source} &7| Reason: &f{reason} &7| Duration: &f{duration}";
        broadcast(path, fallback, target.getName(), source, reason, durStr, silent);
    }

    public void broadcastKick(Player target, String source, String reason, boolean silent) {
        String path = silent ? "punishment.kick.silent-broadcast" : "punishment.kick.broadcast";
        String fallback = silent
                ? "&7[Silent] &c{player} &7was kicked by &c{source} &7| Reason: &f{reason}"
                : "&c{player} &7was kicked by &c{source} &7| Reason: &f{reason}";
        String msg = plugin.getConfigManager().getMessage(path, fallback);
        msg = msg.replace("{player}", target.getName())
                .replace("{source}", source)
                .replace("{reason}", reason);
        Component component = plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(msg));
        if (silent) {
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("iraquecore.punishment.silent"))
                    .forEach(p -> p.sendMessage(component));
            Bukkit.getConsoleSender().sendMessage(component);
        } else {
            Bukkit.broadcast(component);
        }
    }

    private void broadcast(String path, String fallback, String player, String source, String reason, String duration, boolean silent) {
        String msg = plugin.getConfigManager().getMessage(path, fallback);
        msg = msg.replace("{player}", player)
                .replace("{source}", source)
                .replace("{reason}", reason)
                .replace("{duration}", duration);
        Component component = plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(msg));
        if (silent) {
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("iraquecore.punishment.silent"))
                    .forEach(p -> p.sendMessage(component));
            Bukkit.getConsoleSender().sendMessage(component);
        } else {
            Bukkit.broadcast(component);
        }
    }

    public static long parseDuration(String input) {
        if (input.equalsIgnoreCase("perm") || input.equalsIgnoreCase("permanent")) return -1;
        try {
            char unit = input.charAt(input.length() - 1);
            long amount = Long.parseLong(input.substring(0, input.length() - 1));
            return switch (Character.toLowerCase(unit)) {
                case 's' -> amount * 1000L;
                case 'm' -> amount * 60000L;
                case 'h' -> amount * 3600000L;
                case 'd' -> amount * 86400000L;
                case 'y' -> amount * 31536000000L;
                default -> -1;
            };
        } catch (Exception e) {
            return -1;
        }
    }

    public static String formatDuration(long millis) {
        if (millis <= 0) return "Permanent";
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long years = days / 365;
        if (years > 0) return years + "y " + (days % 365) + "d";
        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        BanEntry banEntry = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(event.getName());
        if (banEntry != null) {
            String banReason = banEntry.getReason() != null ? banEntry.getReason() : "Banned";
            Component kickMsg = plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(banReason));
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, kickMsg);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!isMuted(player.getUniqueId())) return;
        event.setCancelled(true);
        MuteEntry entry = getMuteEntry(player.getUniqueId());
        if (entry == null) return;
        String remaining = entry.expiry > 0
                ? " (" + formatDuration(entry.expiry - System.currentTimeMillis()) + " left)"
                : "";
        String msg = plugin.getConfigManager().getMessage("punishment.mute.notification",
                "&cYou are muted. Reason: &f{reason}&c. By: &f{source}{remaining}");
        msg = msg.replace("{reason}", entry.reason)
                .replace("{source}", entry.source)
                .replace("{remaining}", remaining);
        player.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(msg)));
    }

    public record MuteEntry(String reason, String source, long expiry) {
        public boolean isExpired() {
            return expiry > 0 && System.currentTimeMillis() >= expiry;
        }
    }
}
