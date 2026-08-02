package gg.leo.IraqueCore.papi;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.playtime.PlaytimeManager;
import gg.leo.IraqueCore.rank.Rank;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class IraqueCoreExpansion extends PlaceholderExpansion {

    private final IraqueCore plugin;

    public IraqueCoreExpansion(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "iraquecore";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Iraque";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        UUID id = player.getUniqueId();
        String idLower = identifier.toLowerCase();

        return switch (idLower) {
            case "rank" -> {
                Rank rank = getRank(id);
                yield rank != null ? rank.prefix() + rank.name() : "";
            }
            case "rank_name" -> {
                Rank rank = getRank(id);
                yield rank != null ? rank.name() : "";
            }
            case "rank_prefix" -> {
                Rank rank = getRank(id);
                yield rank != null ? rank.prefix() : "";
            }
            case "rank_suffix" -> {
                Rank rank = getRank(id);
                yield rank != null ? rank.suffix() : "";
            }
            case "rank_color" -> {
                Rank rank = getRank(id);
                yield rank != null ? rank.color() : "";
            }
            case "tag" -> plugin.getTagManager().getPlayerTagDisplay(id);
            case "tag_id" -> {
                String tagId = plugin.getTagManager().getPlayerTagId(id);
                yield tagId != null ? tagId : "";
            }
            case "playtime" -> PlaytimeManager.formatTime(plugin.getPlaytimeManager().getPlaytime(id));
            case "playtime_raw" -> String.valueOf(plugin.getPlaytimeManager().getPlaytime(id));
            case "afk" -> plugin.getAfkManager().isAfk(id) ? "yes" : "no";
            case "afk_prefix" -> plugin.getAfkManager().isAfk(id) ? plugin.getAfkManager().getAfkPrefix() : "";
            case "chatcolor" -> plugin.getChatColorManager().getActiveColorCode(id);
            case "deaths" -> String.valueOf(plugin.getScoreboardManager().getDeaths().getOrDefault(id, 0));
            case "blocks_broken" -> String.valueOf(plugin.getScoreboardManager().getBlocksBroken().getOrDefault(id, 0));
            case "blocks_placed" -> String.valueOf(plugin.getScoreboardManager().getBlocksPlaced().getOrDefault(id, 0));
            case "online" -> String.valueOf(Bukkit.getOnlinePlayers().size());
            default -> null;
        };
    }

    private Rank getRank(UUID id) {
        return plugin.getRankManager().getPlayerRank(id)
                .orElseGet(plugin.getRankManager()::getDefaultRank);
    }
}
