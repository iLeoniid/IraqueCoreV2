package gg.leo.IraqueCore.rank;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RankManager {

    private final IraqueCore plugin;
    private final Map<String, Rank>              ranks       = new LinkedHashMap<>();
    private final Map<UUID, String>              playerRanks = new HashMap<>();

    // Keep reference to attachments so we can remove them when changing rank
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    private File dataFile;

    public RankManager(IraqueCore plugin) {
        this.plugin   = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "player-ranks.yml");
    }

    //  Rank loading 

    public void loadRanks() {
        ranks.clear();
        List<String> names = plugin.getConfigManager().getRankNames();

        for (String name : names) {
            ConfigurationSection section = plugin.getConfigManager().getRankConfig(name);
            if (section == null) continue;

            Rank rank = new Rank(
                    name,
                    translateColors(section.getString("prefix", "")),
                    translateColors(section.getString("suffix", "")),
                    section.getInt("weight", 50),
                    translateColors(section.getString("color", "&7")),
                    section.getStringList("permissions")
            );
            ranks.put(name, rank);
        }

        loadPlayerData();
    }

    //  Player data ─

    /**
     * Loads an individual player's rank (called on onJoin).
     */
    public void loadPlayer(UUID uuid) {
        if (!dataFile.exists()) {
            playerRanks.put(uuid, plugin.getConfigManager().getDefaultRankName());
            return;
        }

        YamlConfiguration data     = YamlConfiguration.loadConfiguration(dataFile);
        String            rankName = data.getString(uuid.toString());

        if (rankName != null && ranks.containsKey(rankName)) {
            playerRanks.put(uuid, rankName);
        } else {
            playerRanks.put(uuid, plugin.getConfigManager().getDefaultRankName());
        }
    }

    /**
     * Loads all players' ranks (called on loadRanks).
     */
    public void loadPlayerData() {
        playerRanks.clear();
        if (!dataFile.exists()) return;

        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : data.getKeys(false)) {
            try {
                UUID   uuid     = UUID.fromString(key);
                String rankName = data.getString(key);
                if (rankName != null && ranks.containsKey(rankName)) {
                    playerRanks.put(uuid, rankName);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    /**
     * Saves an individual player's rank to file.
     * Called on onQuit — avoids rewriting the entire file on every quit
     * by saving only that player's entry incrementally.
     * In practice for small servers saveAll() is acceptable,
     * but kept separate for future optimization.
     */
    public void savePlayer(UUID uuid) {
        saveAll();
    }

    public void saveAll() {
        YamlConfiguration data = new YamlConfiguration();
        for (Map.Entry<UUID, String> entry : playerRanks.entrySet()) {
            data.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save player ranks", e);
        }
    }

    //  Rank assignment ──

    public void setRank(UUID uuid, String rankName) {
        if (!ranks.containsKey(rankName)) return;
        playerRanks.put(uuid, rankName);
        saveAll();

        // Re-apply permissions if the player is online
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            removePermissions(player);
            applyPermissions(player);
        }
    }

    public Optional<Rank> getPlayerRank(UUID uuid) {
        String name = playerRanks.get(uuid);
        if (name == null) {
            name = plugin.getConfigManager().getDefaultRankName();
            playerRanks.put(uuid, name);
        }
        return Optional.ofNullable(ranks.get(name));
    }

    public Optional<Rank> getRank(String name) {
        return Optional.ofNullable(ranks.get(name));
    }

    public Rank getDefaultRank() {
        return ranks.get(plugin.getConfigManager().getDefaultRankName());
    }

    public Map<String, Rank> getRanks() {
        return Collections.unmodifiableMap(ranks);
    }

    public void addRank(Rank rank) {
        ranks.put(rank.name(), rank);
    }

    public void removeRank(String name) {
        ranks.remove(name);
    }

    //  Permissions ─

    /**
     * Applies the player's rank permissions.
     * Stores the PermissionAttachment so it can be removed later.
     */
    public void applyPermissions(Player player) {
        getPlayerRank(player.getUniqueId()).ifPresent(rank -> {
            // Remove previous attachment if it exists
            removePermissions(player);

            if (rank.permissions().isEmpty()) return;

            PermissionAttachment attachment = player.addAttachment(plugin);
            for (String perm : rank.permissions()) {
                if (perm.equals("*")) continue; // wildcard not directly supported by Bukkit
                attachment.setPermission(perm, true);
            }
            attachments.put(player.getUniqueId(), attachment);
            player.recalculatePermissions();
        });
    }

    /**
     * Removes the player's PermissionAttachment.
     * Call before changing rank or on disconnect.
     */
    public void removePermissions(Player player) {
        PermissionAttachment attachment = attachments.remove(player.getUniqueId());
        if (attachment != null) {
            player.removeAttachment(attachment);
            player.recalculatePermissions();
        }
    }

    //  Helpers 

    /**
     * Translates only valid & codes → § without touching URLs or &amp;.
     */
    private String translateColors(String text) {
        if (text == null) return "";
        // Hex &#RRGGBB → §x§R§R§G§G§B§B (Spigot legacy format)
        text = text.replaceAll("&#([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})",
                "§x§$1§$2§$3");
        // Valid classic & codes
        text = text.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");
        return text;
    }
}