package gg.leo.IraqueCore.rank;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RankManager {

    private final IraqueCore plugin;
    private final Map<String, Rank> ranks = new LinkedHashMap<>();
    private final Map<UUID, String> playerRanks = new HashMap<>();
    private File dataFile;

    public RankManager(IraqueCore plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "player-ranks.yml");
    }

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

    public void loadPlayer(UUID uuid) {
        if (dataFile.exists()) {
            YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
            String rankName = data.getString(uuid.toString());
            if (rankName != null && ranks.containsKey(rankName)) {
                playerRanks.put(uuid, rankName);
            } else {
                playerRanks.put(uuid, plugin.getConfigManager().getDefaultRankName());
            }
        }
    }

    public void loadPlayerData() {
        playerRanks.clear();
        if (!dataFile.exists()) return;

        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : data.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String rankName = data.getString(key);
            if (ranks.containsKey(rankName)) {
                playerRanks.put(uuid, rankName);
            }
        }
    }

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

    public void setRank(UUID uuid, String rankName) {
        if (!ranks.containsKey(rankName)) return;
        playerRanks.put(uuid, rankName);
        saveAll();
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

    private String translateColors(String text) {
        return text.replace("&", "§");
    }

    public void applyPermissions(Player player) {
        getPlayerRank(player.getUniqueId()).ifPresent(rank -> {
            for (String perm : rank.permissions()) {
                if (perm.equals("*")) continue;
                player.addAttachment(plugin, perm, true);
            }
        });
    }
}
