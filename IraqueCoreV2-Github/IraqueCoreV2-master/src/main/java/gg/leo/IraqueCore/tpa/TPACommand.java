package gg.leo.IraqueCore.tpa;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class TPACommand implements TabExecutor {

    private final IraqueCore plugin;
    private final TPAManager tpaManager;

    public TPACommand(IraqueCore plugin, TPAManager tpaManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("general.player-only", "&cOnly players can use this command."))));
            return true;
        }

        if (!player.hasPermission("iraquecore.tpa")) {
            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("general.no-permission", "&cYou don't have permission."))));
            return true;
        }

        if (args.length == 0) {
            sendUsage(player, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "accept" -> tpaManager.acceptRequest(player);
            case "deny" -> tpaManager.denyRequest(player);
            case "cancel" -> handleCancel(player);
            default -> handleSend(player, args[0], label);
        }

        return true;
    }

    private void handleSend(Player sender, String targetName, String label) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("general.player-not-found", "&cPlayer not found."))));
            return;
        }

        if (target.equals(sender)) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("tpa.cannot-self",
                                "&cYou cannot teleport to yourself."))));
            return;
        }

        tpaManager.sendRequest(sender, target);
    }

    private void handleCancel(Player sender) {
        // Cancel any outgoing request from this player
        // This would require TPAManager to track outgoing requests too
        // For now, just inform the player
        sender.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("tpa.no-outgoing",
                            "&cYou have no outgoing teleport requests to cancel."))));
    }

    private void sendUsage(Player player, String label) {
        player.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("tpa.usage",
                            "&cUsage: /{label} <player> | accept | deny"))));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("accept", "deny").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
