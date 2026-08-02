package gg.leo.IraqueCore.profile;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class ProfileCommand implements TabExecutor {

    private final IraqueCore plugin;

    public ProfileCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("general.player-only", "&cOnly players can use this command."))));
            return true;
        }

        OfflinePlayer target;
        if (args.length == 0) {
            target = player;
        } else {
            target = Bukkit.getOfflinePlayer(args[0]);
            boolean known = target.hasPlayedBefore()
                    || plugin.getPlaytimeManager().getPlaytime(target.getUniqueId()) > 0;
            if (!known || target.getName() == null) {
                player.sendMessage(plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().translate(
                                plugin.getConfigManager().getMessage("profile.player-not-found", "&cPlayer not found."))));
                return true;
            }
        }

        sendProfile(player, target);
        return true;
    }

    private void sendProfile(Player viewer, OfflinePlayer target) {
        var cm = plugin.getConfigManager();
        var rankManager = plugin.getRankManager();
        var tagManager = plugin.getTagManager();

        String name = target.getName();
        Optional<Rank> rankOpt = rankManager.getPlayerRank(target.getUniqueId());
        Rank rank = rankOpt.orElseGet(rankManager::getDefaultRank);

        String rankPrefix = rank != null ? rank.prefix() : "";
        String rankColor = rank != null ? rank.color() : "&7";
        String rankName = rank != null ? rank.name() : cm.getDefaultRankName();

        String tagId = tagManager.getPlayerTagId(target.getUniqueId());
        String tagText = tagManager.getPlayerTagDisplay(target.getUniqueId());

        String tagDisplay;
        if (tagId != null) {
            var tag = tagManager.getTag(tagId);
            if (tag != null) {
                tagDisplay = tagText.isEmpty() ? tag.getDisplayName() : tagText + " &7(&f" + tag.getDisplayName() + "&7)";
            } else {
                tagDisplay = tagText;
            }
        } else {
            tagDisplay = cm.getMessage("profile.tag-none", "&7Nenhuma tag equipada");
        }

        boolean online = target.isOnline();
        String playtimeStr = gg.leo.IraqueCore.playtime.PlaytimeManager.formatTime(
                plugin.getPlaytimeManager().getPlaytime(target.getUniqueId()));

        long firstJoin = plugin.getPlaytimeManager().getFirstJoin(target.getUniqueId());
        if (firstJoin <= 0) firstJoin = target.getFirstPlayed();
        String joinDate = cm.getMessage("profile.date-unknown", "&7N/A");
        if (firstJoin > 0) {
            joinDate = new SimpleDateFormat(cm.getDateFormat()).format(new Date(firstJoin));
        }

        String status = online
                ? cm.getMessage("profile.status-online", "&aOnline")
                : cm.getMessage("profile.status-offline", "&cOffline");

        String achievement = pickAchievement();

        String display = rankPrefix + (tagText.isEmpty() ? "" : tagText + " ")
                + rankColor + name;

        viewer.sendMessage(cm.deserialize(cm.translate(cm.getMessage("profile.header", "&8&m&l--------------------------------"))));
        viewer.sendMessage(cm.deserialize(cm.translate(cm.getMessage("profile.title", "&6{display} &8- &7Perfil")
                .replace("{display}", display))));
        viewer.sendMessage(cm.deserialize(cm.translate(cm.getMessage("profile.rank", " &8\u25b8 &7Rango: &f{rank}")
                .replace("{rank}", rankName))));
        viewer.sendMessage(cm.deserialize(cm.translate(cm.getMessage("profile.tag", " &8\u25b8 &7Tag: {tag}")
                .replace("{tag}", tagDisplay))));
        viewer.sendMessage(cm.deserialize(cm.translate(cm.getMessage("profile.playtime", " &8\u25b8 &7Tiempo de juego: &e{playtime}")
                .replace("{playtime}", playtimeStr))));
        viewer.sendMessage(cm.deserialize(cm.translate(cm.getMessage("profile.first-join", " &8\u25b8 &7Se unió: &e{date}")
                .replace("{date}", joinDate))));
        viewer.sendMessage(cm.deserialize(cm.translate(cm.getMessage("profile.status", " &8\u25b8 &7Estado: {status}")
                .replace("{status}", status))));
        viewer.sendMessage(cm.deserialize(cm.translate(cm.getMessage("profile.achievement", " &8\u25b8 &7Logro destacado: &f{achievement}")
                .replace("{achievement}", achievement))));
        viewer.sendMessage(cm.deserialize(cm.translate(cm.getMessage("profile.footer", "&8&m&l--------------------------------"))));
    }

    private String pickAchievement() {
        List<String> list = plugin.getConfigManager().getMessageList("profile.achievements");
        if (list.isEmpty()) {
            return plugin.getConfigManager().getMessage("profile.achievement-fallback", "&7Consiguió el logro 'Online'");
        }
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
