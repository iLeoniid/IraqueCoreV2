package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WhitelistCommand implements TabExecutor {

    private final IraqueCore plugin;

    private static final String PERM = "iraquecore.whitelist";

    public WhitelistCommand(IraqueCore plugin) {
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
        if (!sender.hasPermission(PERM)) {
            sender.sendMessage(txt("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(txt("whitelist.usage"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on" -> handleOn(sender);
            case "off" -> handleOff(sender);
            case "add" -> handleAdd(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list" -> handleList(sender);
            case "status" -> handleStatus(sender);
            default -> sender.sendMessage(txt("whitelist.usage"));
        }
        return true;
    }

    private void handleOn(CommandSender sender) {
        Bukkit.setWhitelist(true);
        sender.sendMessage(msg("whitelist.enabled"));
        plugin.getPluginLogger().info("Whitelist enabled by {}", sender.getName());
    }

    private void handleOff(CommandSender sender) {
        Bukkit.setWhitelist(false);
        sender.sendMessage(msg("whitelist.disabled"));
        plugin.getPluginLogger().info("Whitelist disabled by {}", sender.getName());
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(txt("whitelist.usage-add"));
            return;
        }

        String name = args[1];
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);

        if (offline.isWhitelisted()) {
            sender.sendMessage(msg("whitelist.already").replace("{player}", name));
            return;
        }

        offline.setWhitelisted(true);
        sender.sendMessage(msg("whitelist.added").replace("{player}", name));
        plugin.getPluginLogger().info("{} whitelisted {} (added by {})", sender.getName(), name, sender.getName());
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(txt("whitelist.usage-remove"));
            return;
        }

        String name = args[1];
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);

        if (!offline.isWhitelisted()) {
            sender.sendMessage(msg("whitelist.not-whitelisted").replace("{player}", name));
            return;
        }

        offline.setWhitelisted(false);
        sender.sendMessage(msg("whitelist.removed").replace("{player}", name));
        plugin.getPluginLogger().info("{} unwhitelisted {} (removed by {})", sender.getName(), name, sender.getName());
    }

    private void handleList(CommandSender sender) {
        var whitelisted = Bukkit.getWhitelistedPlayers();
        if (whitelisted.isEmpty()) {
            sender.sendMessage(txt("whitelist.list-empty"));
            return;
        }

        String names = whitelisted.stream()
                .map(OfflinePlayer::getName)
                .filter(n -> n != null)
                .collect(Collectors.joining(", "));

        sender.sendMessage(msg("whitelist.list-header")
                .replace("{count}", String.valueOf(whitelisted.size())));
        sender.sendMessage(plugin.getConfigManager().deserialize("<gray>" + names));
    }

    private void handleStatus(CommandSender sender) {
        boolean on = Bukkit.hasWhitelist();
        sender.sendMessage(msg(on ? "whitelist.status-on" : "whitelist.status-off"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERM)) return List.of();

        if (args.length == 1) {
            return List.of("on", "off", "add", "remove", "list", "status").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            List<String> suggestions = new ArrayList<>();
            if (args[0].equalsIgnoreCase("remove")) {
                for (OfflinePlayer p : Bukkit.getWhitelistedPlayers()) {
                    String name = p.getName();
                    if (name != null && name.toLowerCase().startsWith(args[1].toLowerCase())) {
                        suggestions.add(name);
                    }
                }
            }
            return suggestions;
        }

        return List.of();
    }
}
