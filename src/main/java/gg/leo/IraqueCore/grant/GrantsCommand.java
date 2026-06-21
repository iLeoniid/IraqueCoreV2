package gg.leo.IraqueCore.grant;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class GrantsCommand implements TabExecutor {

    private final IraqueCore plugin;

    public GrantsCommand(IraqueCore plugin) {
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("iraquecore.grants")) {
            sender.sendMessage(txt("grant.no-permission"));
            return true;
        }

        Player target;
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(txt("general.player-only"));
                return true;
            }
            target = player;
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(txt("general.player-not-found"));
                return true;
            }
        }

        List<Grant> allGrants = plugin.getGrantManager().getAllGrants(target.getUniqueId());
        List<Grant> activeGrants = plugin.getGrantManager().getActiveGrants(target.getUniqueId());

        if (allGrants.isEmpty()) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("grant.no-grants")
                            .replace("{player}", target.getName())));
            return true;
        }

        sender.sendMessage(Component.text(" \u2500\u2500\u2500 Grants for " + target.getName() + " \u2500\u2500\u2500")
                .color(net.kyori.adventure.text.format.NamedTextColor.GOLD));

        sender.sendMessage(Component.text(" Active (" + activeGrants.size() + "):")
                .color(net.kyori.adventure.text.format.NamedTextColor.GREEN));

        if (activeGrants.isEmpty()) {
            sender.sendMessage(Component.text("   None").color(net.kyori.adventure.text.format.NamedTextColor.GRAY));
        } else {
            for (Grant g : activeGrants) {
                String remaining = formatRemaining(g.getRemaining());
                sender.sendMessage(Component.text("   \u25B6 " + g.getRankName()
                                + " - " + g.getReason()
                                + " (" + remaining + ")")
                        .color(net.kyori.adventure.text.format.NamedTextColor.AQUA));
            }
        }

        List<Grant> inactive = allGrants.stream().filter(g -> !g.isActive()).collect(Collectors.toList());
        if (!inactive.isEmpty()) {
            sender.sendMessage(Component.text(" History (" + inactive.size() + "):")
                    .color(net.kyori.adventure.text.format.NamedTextColor.GRAY));
            for (Grant g : inactive) {
                String status = g.isRevoked() ? "Revoked" : "Expired";
                sender.sendMessage(Component.text("   \u25B6 " + g.getRankName()
                                + " - " + g.getReason()
                                + " [" + status + "]")
                        .color(net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY));
            }
        }

        return true;
    }

    private String formatRemaining(long millis) {
        if (millis == Long.MAX_VALUE) return "Permanent";
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d";
        if (hours > 0) return hours + "h";
        if (minutes > 0) return minutes + "m";
        return seconds + "s";
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("iraquecore.grants")) return List.of();
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
