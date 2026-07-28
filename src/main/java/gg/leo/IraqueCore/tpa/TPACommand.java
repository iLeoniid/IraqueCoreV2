package gg.leo.IraqueCore.tpa;

import gg.leo.IraqueCore.IraqueCore;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TPACommand
implements TabExecutor {
    private final IraqueCore plugin;
    private final TPAManager tpaManager;

    public TPACommand(IraqueCore plugin, TPAManager tpaManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("general.player-only", "&cOnly players can use this command."))));
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("iraquecore.tpa")) {
            player.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("general.no-permission", "&cYou don't have permission."))));
            return true;
        }
        if (args.length == 0) {
            this.sendUsage(player, label);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "accept": {
                this.tpaManager.acceptRequest(player);
                break;
            }
            case "deny": {
                this.tpaManager.denyRequest(player);
                break;
            }
            case "cancel": {
                this.handleCancel(player);
                break;
            }
            default: {
                this.handleSend(player, args[0], label);
            }
        }
        return true;
    }

    private void handleSend(Player sender, String targetName, String label) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("general.player-not-found", "&cPlayer not found."))));
            return;
        }
        if (target.equals(sender)) {
            sender.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("tpa.cannot-self", "&cYou cannot teleport to yourself."))));
            return;
        }
        this.tpaManager.sendRequest(sender, target);
    }

    private void handleCancel(Player sender) {
        sender.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("tpa.no-outgoing", "&cYou have no outgoing teleport requests to cancel."))));
    }

    private void sendUsage(Player player, String label) {
        player.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("tpa.usage", "&cUsage: /{label} <player> | accept | deny"))));
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("accept", "deny").stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}
