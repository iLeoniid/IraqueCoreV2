package gg.leo.IraqueCore.grant;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class GrantCommand implements TabExecutor {

    private final IraqueCore plugin;

    public GrantCommand(IraqueCore plugin) {
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage(txt("general.player-only"));
            return true;
        }

        if (!player.hasPermission("iraquecore.grant")) {
            player.sendMessage(txt("grant.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().deserialize(
                    msg("grant.usage")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(txt("general.player-not-found"));
            return true;
        }

        if (args.length >= 3) {
            // Fast-track: /grant <player> <rank> <duration> [reason]
            String rankName = args[1];
            if (plugin.getRankManager().getRank(rankName).isEmpty()) {
                player.sendMessage(txt("rank.not-found"));
                return true;
            }

            String durationStr = args[2];
            long duration = parseDuration(durationStr);
            if (duration <= 0 && !durationStr.equalsIgnoreCase("permanent")) {
                player.sendMessage(plugin.getConfigManager().deserialize(
                        msg("grant.invalid-duration")));
                return true;
            }
            if (durationStr.equalsIgnoreCase("permanent")) {
                duration = Long.MAX_VALUE;
            }

            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                if (i > 3) reasonBuilder.append(" ");
                reasonBuilder.append(args[i]);
            }
            String reason = reasonBuilder.isEmpty() ? "No reason specified" : reasonBuilder.toString();

            plugin.getGrantManager().grant(target.getUniqueId(), player.getUniqueId(), rankName, reason, duration);

            player.sendMessage(plugin.getConfigManager().deserialize(
                    msg("grant.success")
                            .replace("{player}", target.getName())
                            .replace("{rank}", rankName)
                            .replace("{reason}", reason)));

            target.sendMessage(plugin.getConfigManager().deserialize(
                    msg("grant.received")
                            .replace("{rank}", rankName)));

            return true;
        }

        // GUI mode: /grant <player>
        plugin.getGrantListener().startGrant(player, target);
        return true;
    }

    private long parseDuration(String input) {
        if (input.equalsIgnoreCase("permanent") || input.equalsIgnoreCase("perm")) return Long.MAX_VALUE;
        try {
            char unit = input.charAt(input.length() - 1);
            long amount = Long.parseLong(input.substring(0, input.length() - 1));
            return switch (Character.toLowerCase(unit)) {
                case 's' -> amount * 1000L;
                case 'm' -> amount * 60000L;
                case 'h' -> amount * 3600000L;
                case 'd' -> amount * 86400000L;
                case 'w' -> amount * 604800000L;
                case 'y' -> amount * 31536000000L;
                default -> 0;
            };
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("iraquecore.grant")) return List.of();

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return plugin.getRankManager().getRanks().keySet().stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3) {
            return List.of("1h", "1d", "7d", "30d", "1y", "permanent").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
