package gg.leo.IraqueCore.permission;

import gg.leo.IraqueCore.IraqueCore;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PermissionManager {

    private final IraqueCore plugin;
    private final Map<UUID, Set<String>> playerPermissions = new HashMap<>();
    private final Map<String, Set<String>> rankPermissions = new HashMap<>();
    private File dataFile;
    private File rankPermFile;

    public PermissionManager(IraqueCore plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "player-permissions.yml");
        this.rankPermFile = new File(plugin.getDataFolder(), "rank-permissions.yml");
    }

    public void load() {
        playerPermissions.clear();
        rankPermissions.clear();

        if (dataFile.exists()) {
            YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
            for (String key : data.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    List<String> perms = data.getStringList(key);
                    playerPermissions.put(uuid, new LinkedHashSet<>(perms));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        if (rankPermFile.exists()) {
            YamlConfiguration data = YamlConfiguration.loadConfiguration(rankPermFile);
            for (String rank : data.getKeys(false)) {
                List<String> perms = data.getStringList(rank);
                rankPermissions.put(rank.toLowerCase(), new LinkedHashSet<>(perms));
            }
        }
    }

    public void saveAll() {
        YamlConfiguration data = new YamlConfiguration();
        for (Map.Entry<UUID, Set<String>> entry : playerPermissions.entrySet()) {
            data.set(entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save player permissions", e);
        }

        YamlConfiguration rankData = new YamlConfiguration();
        for (Map.Entry<String, Set<String>> entry : rankPermissions.entrySet()) {
            rankData.set(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        try {
            rankData.save(rankPermFile);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save rank permissions", e);
        }
    }

    public Set<String> getPermissions(UUID uuid) {
        return playerPermissions.getOrDefault(uuid, Collections.emptySet());
    }

    public boolean hasPermission(UUID uuid, String permission) {
        Set<String> perms = playerPermissions.get(uuid);
        if (perms == null) return false;
        if (perms.contains("*")) return true;
        if (perms.contains(permission)) return true;
        for (String p : perms) {
            if (p.endsWith(".*")) {
                String base = p.substring(0, p.length() - 2);
                if (permission.startsWith(base + ".") || permission.equals(base)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addPermission(UUID uuid, String permission) {
        playerPermissions.computeIfAbsent(uuid, k -> new LinkedHashSet<>()).add(permission);
        saveAll();
        applyToPlayer(uuid);
    }

    public void removePermission(UUID uuid, String permission) {
        Set<String> perms = playerPermissions.get(uuid);
        if (perms != null) {
            perms.remove(permission);
            if (perms.isEmpty()) {
                playerPermissions.remove(uuid);
            }
            saveAll();
        }
        applyToPlayer(uuid);
    }

    public void clearPermissions(UUID uuid) {
        playerPermissions.remove(uuid);
        saveAll();
        applyToPlayer(uuid);
    }

    public Set<String> getRankPermissions(String rankName) {
        return rankPermissions.getOrDefault(rankName.toLowerCase(), Collections.emptySet());
    }

    public void addRankPermission(String rankName, String permission) {
        rankPermissions.computeIfAbsent(rankName.toLowerCase(), k -> new LinkedHashSet<>()).add(permission);
        saveAll();
    }

    public void removeRankPermission(String rankName, String permission) {
        Set<String> perms = rankPermissions.get(rankName.toLowerCase());
        if (perms != null) {
            perms.remove(permission);
            if (perms.isEmpty()) {
                rankPermissions.remove(rankName.toLowerCase());
            }
            saveAll();
        }
    }

    public void clearRankPermissions(String rankName) {
        rankPermissions.remove(rankName.toLowerCase());
        saveAll();
    }

    public void applyToPlayer(UUID uuid) {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            plugin.getRankManager().applyPermissions(player);
        }
    }

    public void applyToPlayer(Player player) {
        plugin.getRankManager().applyPermissions(player);
    }

    public void removePlayer(Player player) {
        Set<String> perms = playerPermissions.get(player.getUniqueId());
        if (perms != null) {
            for (String perm : perms) {
                if (perm.equals("*")) continue;
                player.addAttachment(plugin, perm, false);
            }
        }
    }
}
