package gg.leo.IraqueCore.rank;

import gg.leo.IraqueCore.IraqueCore;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RankManager {

    private final IraqueCore plugin;
    private final Map<String, Rank>               ranks       = new LinkedHashMap<>();
    private final Map<UUID, String>               playerRanks = new HashMap<>();
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    private File dataFile;

    public RankManager(IraqueCore plugin) {
        this.plugin   = plugin;
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

        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            removePermissions(player);
            applyPermissions(player);
            Bukkit.getScheduler().runTaskLater(plugin, () -> updatePlayerRankVisuals(player), 2L);
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

    public void addRank(Rank rank)         { ranks.put(rank.name(), rank); }
    public void removeRank(String name)    { ranks.remove(name); }

    public void applyPermissions(Player player) {
        removePermissions(player);

        PermissionAttachment attachment = player.addAttachment(plugin);

        var permManager = plugin.getPermissionManager();

        // Apply rank permissions (config + custom)
        getPlayerRank(player.getUniqueId()).ifPresent(rank -> {
            for (String perm : rank.permissions()) {
                if (perm.equals("*")) continue;
                attachment.setPermission(perm, true);
            }
            if (permManager != null) {
                for (String perm : permManager.getRankPermissions(rank.name())) {
                    if (perm.equals("*")) continue;
                    attachment.setPermission(perm, true);
                }
            }
        });

        // Apply player-specific permissions from PermissionManager
        if (permManager != null) {
            for (String perm : permManager.getPermissions(player.getUniqueId())) {
                if (perm.equals("*")) continue;
                attachment.setPermission(perm, true);
            }
        }

        attachments.put(player.getUniqueId(), attachment);
        player.recalculatePermissions();
    }

    public void removePermissions(Player player) {
        PermissionAttachment attachment = attachments.remove(player.getUniqueId());
        if (attachment != null) {
            player.removeAttachment(attachment);
            player.recalculatePermissions();
        }
    }

    public void updatePlayerRankVisuals(Player player) {
        getPlayerRank(player.getUniqueId()).ifPresent(rank -> {
            String tag    = plugin.getTagManager().getPlayerTagDisplay(player);
            String tagStr = tag.isEmpty() ? "" : tag + " ";

            String displayPrefix;
            if (plugin.getAfkManager() != null && plugin.getAfkManager().isAfk(player.getUniqueId())) {
                displayPrefix = plugin.getAfkManager().getAfkPrefix();
            } else {
                displayPrefix = rank.prefix();
            }

            String listNameRaw = rank.color() + player.getName();
            player.playerListName(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(listNameRaw)));

            String prefix = buildPrefixFrom(displayPrefix, tagStr);
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                applyTeamForViewer(viewer, player, rank, prefix);
            }
        });
    }

    public void initVisuals(Player joining) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(joining)) continue;
            getPlayerRank(other.getUniqueId()).ifPresent(rank -> {
                String tag    = plugin.getTagManager().getPlayerTagDisplay(other);
                String tagStr = tag.isEmpty() ? "" : tag + " ";

                String displayPrefix;
                if (plugin.getAfkManager() != null && plugin.getAfkManager().isAfk(other.getUniqueId())) {
                    displayPrefix = plugin.getAfkManager().getAfkPrefix();
                } else {
                    displayPrefix = rank.prefix();
                }

                String prefix = buildPrefixFrom(displayPrefix, tagStr);
                applyTeamForViewer(joining, other, rank, prefix);
            });
        }
        updatePlayerRankVisuals(joining);
    }

    private String buildPrefixFrom(String prefix, String tagStr) {
        String full = prefix + " " + tagStr;
        return full.length() > 64 ? full.substring(0, 64) : full;
    }

    private void applyTeamForViewer(Player viewer, Player target, Rank rank, String prefix) {
        Scoreboard board = viewer.getScoreboard();
        if (board == null) board = Bukkit.getScoreboardManager().getMainScoreboard();

        String teamName = "irq" + String.format("%03d", rank.weight());

        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
        }

        for (Team t : board.getTeams()) {
            if (t.getName().startsWith("irq") && !t.getName().equals(teamName)
                    && t.hasEntry(target.getName())) {
                t.removeEntry(target.getName());
            }
        }

        team.prefix(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(prefix)));
        if (!rank.suffix().isEmpty()) {
            team.suffix(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(rank.suffix())));
        }

        String colorStr = rank.color();
        if (colorStr != null && colorStr.length() >= 2 && colorStr.charAt(0) == '\u00A7') {
            char code = colorStr.charAt(1);
            if (code != 'x') {
                ChatColor cc = ChatColor.getByChar(code);
                if (cc != null && cc.isColor()) {
                    NamedTextColor named = switch (cc) {
                        case BLACK -> NamedTextColor.BLACK;
                        case DARK_BLUE -> NamedTextColor.DARK_BLUE;
                        case DARK_GREEN -> NamedTextColor.DARK_GREEN;
                        case DARK_AQUA -> NamedTextColor.DARK_AQUA;
                        case DARK_RED -> NamedTextColor.DARK_RED;
                        case DARK_PURPLE -> NamedTextColor.DARK_PURPLE;
                        case GOLD -> NamedTextColor.GOLD;
                        case GRAY -> NamedTextColor.GRAY;
                        case DARK_GRAY -> NamedTextColor.DARK_GRAY;
                        case BLUE -> NamedTextColor.BLUE;
                        case GREEN -> NamedTextColor.GREEN;
                        case AQUA -> NamedTextColor.AQUA;
                        case RED -> NamedTextColor.RED;
                        case LIGHT_PURPLE -> NamedTextColor.LIGHT_PURPLE;
                        case YELLOW -> NamedTextColor.YELLOW;
                        case WHITE -> NamedTextColor.WHITE;
                        default -> null;
                    };
                    if (named != null) {
                        team.color(named);
                    }
                }
            }
        }

        team.addEntry(target.getName());
    }

    public void removePlayerFromTeams(Player player) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard board = viewer.getScoreboard();
            if (board == null) continue;
            for (Team t : board.getTeams()) {
                if (t.getName().startsWith("irq") && t.hasEntry(player.getName())) {
                    t.removeEntry(player.getName());
                }
            }
        }
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Team t : main.getTeams()) {
            if (t.getName().startsWith("irq") && t.hasEntry(player.getName())) {
                t.removeEntry(player.getName());
            }
        }
    }

    public void updateAllVisuals() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerRankVisuals(player);
        }
    }

    private String translateColors(String text) {
        if (text == null) return "";
        text = text.replaceAll("&#([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})",
                "\u00A7x\u00A7$1\u00A7$2\u00A7$3");
        text = text.replaceAll("&([0-9a-fk-orA-FK-OR])", "\u00A7$1");
        return text;
    }
}
