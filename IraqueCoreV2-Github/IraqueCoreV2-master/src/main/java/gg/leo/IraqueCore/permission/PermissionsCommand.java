package gg.leo.IraqueCore.permission;

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
import java.util.Set;
import java.util.stream.Collectors;

public class PermissionsCommand implements TabExecutor {

    private final IraqueCore plugin;

    public PermissionsCommand(IraqueCore plugin) {
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
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> handleAdd(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list" -> handleList(sender, args);
            case "clear" -> handleClear(sender, args);
            case "check" -> handleCheck(sender, args);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.permission.add")) {
            sender.sendMessage(txt("permission.no-permission"));
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("permission.usage-add") + " &7Usage: /perm add <player/rank> <name> <permission>"));
            return;
        }

        String type = args[1].toLowerCase();
        String name = args[2];
        String perm = args[3];

        if (type.equals("player")) {
            Player target = Bukkit.getPlayer(name);
            if (target == null) {
                sender.sendMessage(txt("permission.player-not-found"));
                return;
            }
            plugin.getPermissionManager().addPermission(target.getUniqueId(), perm);
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("permission.added")
                            .replace("{player}", target.getName())
                            .replace("{permission}", perm)));
        } else if (type.equals("rank")) {
            if (plugin.getRankManager().getRank(name).isEmpty()) {
                sender.sendMessage(txt("rank.not-found"));
                return;
            }
            plugin.getPermissionManager().addRankPermission(name, perm);
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("permission.rank-added")
                            .replace("{rank}", name)
                            .replace("{permission}", perm)));
            for (Player online : Bukkit.getOnlinePlayers()) {
                plugin.getRankManager().applyPermissions(online);
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    "&cTipo invalido. Usa: player o rank"));
        }
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.permission.remove")) {
            sender.sendMessage(txt("permission.no-permission"));
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("permission.usage-remove") + " &7Usage: /perm remove <player/rank> <name> <permission>"));
            return;
        }

        String type = args[1].toLowerCase();
        String name = args[2];
        String perm = args[3];

        if (type.equals("player")) {
            Player target = Bukkit.getPlayer(name);
            if (target == null) {
                sender.sendMessage(txt("permission.player-not-found"));
                return;
            }
            plugin.getPermissionManager().removePermission(target.getUniqueId(), perm);
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("permission.removed")
                            .replace("{player}", target.getName())
                            .replace("{permission}", perm)));
        } else if (type.equals("rank")) {
            if (plugin.getRankManager().getRank(name).isEmpty()) {
                sender.sendMessage(txt("rank.not-found"));
                return;
            }
            plugin.getPermissionManager().removeRankPermission(name, perm);
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("permission.rank-removed")
                            .replace("{rank}", name)
                            .replace("{permission}", perm)));
            for (Player online : Bukkit.getOnlinePlayers()) {
                plugin.getRankManager().applyPermissions(online);
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    "&cTipo invalido. Usa: player o rank"));
        }
    }

    private void handleList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.permission.list")) {
            sender.sendMessage(txt("permission.no-permission"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("permission.usage-list") + " &7Usage: /perm list <player/rank> <name>"));
            return;
        }

        String type = args[1].toLowerCase();
        String name = args[2];

        if (type.equals("player")) {
            Player target = Bukkit.getPlayer(name);
            if (target == null) {
                sender.sendMessage(txt("permission.player-not-found"));
                return;
            }
            Set<String> perms = plugin.getPermissionManager().getPermissions(target.getUniqueId());
            sendPermList(sender, target.getName(), perms);
        } else if (type.equals("rank")) {
            if (plugin.getRankManager().getRank(name).isEmpty()) {
                sender.sendMessage(txt("rank.not-found"));
                return;
            }
            Set<String> perms = plugin.getPermissionManager().getRankPermissions(name);
            sendPermList(sender, "Rank: " + name, perms);
        } else {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    "&cTipo invalido. Usa: player o rank"));
        }
    }

    private void sendPermList(CommandSender sender, String targetName, Set<String> perms) {
        sender.sendMessage(plugin.getConfigManager().deserialize(
                msg("permission.list-header")
                        .replace("{player}", targetName)
                        .replace("{count}", String.valueOf(perms.size()))));

        if (perms.isEmpty()) {
            sender.sendMessage(txt("permission.list-empty"));
        } else {
            for (String perm : perms) {
                sender.sendMessage(plugin.getConfigManager().deserialize(
                        msg("permission.list-entry")
                                .replace("{permission}", perm)));
            }
        }
    }

    private void handleClear(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.permission.clear")) {
            sender.sendMessage(txt("permission.no-permission"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("permission.usage-clear") + " &7Usage: /perm clear <player/rank> <name>"));
            return;
        }

        String type = args[1].toLowerCase();
        String name = args[2];

        if (type.equals("player")) {
            Player target = Bukkit.getPlayer(name);
            if (target == null) {
                sender.sendMessage(txt("permission.player-not-found"));
                return;
            }
            plugin.getPermissionManager().clearPermissions(target.getUniqueId());
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("permission.cleared")
                            .replace("{player}", target.getName())));
        } else if (type.equals("rank")) {
            if (plugin.getRankManager().getRank(name).isEmpty()) {
                sender.sendMessage(txt("rank.not-found"));
                return;
            }
            plugin.getPermissionManager().clearRankPermissions(name);
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("permission.rank-cleared")
                            .replace("{rank}", name)));
            for (Player online : Bukkit.getOnlinePlayers()) {
                plugin.getRankManager().applyPermissions(online);
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    "&cTipo invalido. Usa: player o rank"));
        }
    }

    private void handleCheck(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.permission.check")) {
            sender.sendMessage(txt("permission.no-permission"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("permission.usage-check") + " &7Usage: /perm check <player> <permission>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(txt("permission.player-not-found"));
            return;
        }

        String perm = args[2];
        boolean has = target.hasPermission(perm);

        sender.sendMessage(plugin.getConfigManager().deserialize(
                msg(has ? "permission.check-true" : "permission.check-false")
                        .replace("{player}", target.getName())
                        .replace("{permission}", perm)));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(txt("permission.help-header"));
        sender.sendMessage(txt("permission.help-add"));
        sender.sendMessage(txt("permission.help-remove"));
        sender.sendMessage(txt("permission.help-list"));
        sender.sendMessage(txt("permission.help-clear"));
        sender.sendMessage(txt("permission.help-check"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("add", "remove", "list", "clear", "check");
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (List.of("add", "remove", "list", "clear").contains(sub)) {
                return List.of("player", "rank").stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            String type = args[1].toLowerCase();
            if (type.equals("player")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (type.equals("rank")) {
                return plugin.getRankManager().getRanks().keySet().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }
}
