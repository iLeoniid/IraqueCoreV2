package gg.leo.IraqueCore.rank;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RankCommand implements TabExecutor {

    private final IraqueCore plugin;

    public RankCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    private String msg(String path) {
        return plugin.getConfigManager().translate(
                plugin.getConfigManager().getMessage(path, "&c" + path));
    }

    private Component txt(String path) {
        return plugin.getConfigManager().getMessageComponent(path);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set" -> handleSet(sender, args);
            case "create" -> handleCreate(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender, args);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.rank.set")) {
            sender.sendMessage(txt("rank.no-permission-set"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(txt("rank.usage-set"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(txt("rank.player-not-found"));
            return;
        }

        String rankName = args[2];
        if (plugin.getRankManager().getRank(rankName).isEmpty()) {
            sender.sendMessage(txt("rank.not-found"));
            return;
        }

        plugin.getRankManager().setRank(target.getUniqueId(), rankName);
        plugin.getRankManager().applyPermissions(target);

        sender.sendMessage(plugin.getConfigManager().deserialize(
                msg("rank.set")
                        .replace("{player}", target.getName())
                        .replace("{rank}", rankName)));
        target.sendMessage(plugin.getConfigManager().deserialize(
                msg("rank.given")
                        .replace("{rank}", rankName)));
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.rank.create")) {
            sender.sendMessage(txt("rank.no-permission"));
            return;
        }
        sender.sendMessage(txt("rank.create-hint"));
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.rank.create")) {
            sender.sendMessage(txt("rank.no-permission"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(txt("rank.usage-remove"));
            return;
        }
        plugin.getRankManager().removeRank(args[1]);
        sender.sendMessage(plugin.getConfigManager().deserialize(
                msg("rank.removed")
                        .replace("{rank}", args[1])));
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(txt("rank.list-header"));
        for (Rank rank : plugin.getRankManager().getRanks().values()) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("rank.list-entry")
                            .replace("{name}", rank.name())
                            .replace("{weight}", String.valueOf(rank.weight()))));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (sender instanceof Player player && args.length < 2) {
            Optional<Rank> rank = plugin.getRankManager().getPlayerRank(player.getUniqueId());
            if (rank.isPresent()) {
                sender.sendMessage(plugin.getConfigManager().deserialize(
                        msg("rank.your-rank")
                                .replace("{rank}", rank.get().name())));
            }
            return;
        }
        if (args.length >= 2) {
            plugin.getRankManager().getRank(args[1]).ifPresentOrElse(
                    rank -> sender.sendMessage(plugin.getConfigManager().deserialize(
                            msg("rank.info-header")
                                    .replace("{name}", rank.name())
                                    .replace("{prefix}", rank.prefix())
                                    .replace("{weight}", String.valueOf(rank.weight())))),
                    () -> sender.sendMessage(txt("rank.not-found-info"))
            );
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(txt("rank.help-header"));
        sender.sendMessage(txt("rank.help-list"));
        sender.sendMessage(txt("rank.help-info"));
        sender.sendMessage(txt("rank.help-set"));
        sender.sendMessage(txt("rank.help-create"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("set", "create", "remove", "list", "info");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return new ArrayList<>(plugin.getRankManager().getRanks().keySet());
        }
        return List.of();
    }
}
