package gg.leo.IraqueCore.alerts;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AlertCommand implements TabExecutor {

    private final IraqueCore plugin;
    private final AlertManager alertManager;

    public AlertCommand(IraqueCore plugin, AlertManager alertManager) {
        this.plugin = plugin;
        this.alertManager = alertManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            msg(sender, "&cUsage: /alert <name> | /alert list | /alert reload | /alert test <name>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            case "test" -> handleTest(sender, args);
            default -> handleSend(sender, args[0]);
        }
        return true;
    }

    private void handleSend(CommandSender sender, String alertId) {
        if (!sender.hasPermission("alert.use")) {
            msg(sender, "&cYou don't have permission to send alerts.");
            return;
        }

        Alert alert = alertManager.getAlert(alertId);
        if (alert == null) {
            msg(sender, "&cAlert '&e" + alertId + "&c' not found. Use &e/alert list");
            return;
        }

        Player playerSource = sender instanceof Player ? (Player) sender : null;
        alertManager.sendToAll(playerSource, alert);
        msg(sender, "&aAlert '&e" + alertId + "&a' sent to all players.");
    }

    private void handleList(CommandSender sender) {
        if (!sender.hasPermission("alert.list")) {
            msg(sender, "&cYou don't have permission to list alerts.");
            return;
        }

        if (alertManager.getAlerts().isEmpty()) {
            msg(sender, "&eNo alerts configured.");
            return;
        }

        msg(sender, "&6─── Available Alerts (" + alertManager.getAlerts().size() + ") ───");
        for (Alert alert : alertManager.getAlerts().values()) {
            String sound = alert.sound() != null && alert.sound().enabled() ? alert.sound().id() : "none";
            msg(sender, "&7  &e" + alert.id() + " &8- &7sound: " + sound);
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("alert.reload")) {
            msg(sender, "&cYou don't have permission to reload alerts.");
            return;
        }

        alertManager.reload();
        msg(sender, "&aAlerts reloaded successfully.");
    }

    private void handleTest(CommandSender sender, String[] args) {
        if (!sender.hasPermission("alert.test")) {
            msg(sender, "&cYou don't have permission to test alerts.");
            return;
        }

        if (!(sender instanceof Player player)) {
            msg(sender, "&cOnly players can test alerts.");
            return;
        }

        if (args.length < 2) {
            msg(sender, "&cUsage: /alert test <name>");
            return;
        }

        Alert alert = alertManager.getAlert(args[1]);
        if (alert == null) {
            msg(sender, "&cAlert '&e" + args[1] + "&c' not found.");
            return;
        }

        alertManager.sendToPlayer(player, alert);
        msg(sender, "&aAlert '&e" + args[1] + "&a' sent to you.");
    }

    private void msg(CommandSender sender, String text) {
        sender.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(text)));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.addAll(alertManager.getAlerts().keySet());
            if (sender.hasPermission("alert.list")) suggestions.add("list");
            if (sender.hasPermission("alert.reload")) suggestions.add("reload");
            if (sender.hasPermission("alert.test")) suggestions.add("test");
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("test")) {
            return alertManager.getAlerts().keySet().stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
