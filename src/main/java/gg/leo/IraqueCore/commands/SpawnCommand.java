package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpawnCommand implements TabExecutor {

    private final IraqueCore plugin;

    public SpawnCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (label.equalsIgnoreCase("setspawn")) {
            return handleSet(sender);
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("set")) {
            return handleSet(sender);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(plugin.getConfigManager().translate(
                    plugin.getConfigManager().getMessage("general.player-only", "&cOnly players can use this command."))));
            return true;
        }

        Location spawn = plugin.getConfigManager().getSpawnLocation();
        if (spawn == null) {
            player.sendMessage(Component.text(plugin.getConfigManager().translate(
                    plugin.getConfigManager().getPrefixedMessage("spawn.not-set"))));
            return true;
        }

        player.teleport(spawn);
        player.sendMessage(Component.text(plugin.getConfigManager().translate(
                plugin.getConfigManager().getPrefixedMessage("spawn.teleport"))));
        return true;
    }

    private boolean handleSet(CommandSender sender) {
        if (!sender.hasPermission("iraquecore.setspawn")) {
            sender.sendMessage(Component.text(plugin.getConfigManager().translate(
                    plugin.getConfigManager().getMessage("general.no-permission", "&cYou don't have permission."))));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(plugin.getConfigManager().translate(
                    plugin.getConfigManager().getMessage("general.player-only", "&cOnly players can use this command."))));
            return true;
        }

        plugin.getConfigManager().setSpawnLocation(player.getLocation());
        player.sendMessage(Component.text(plugin.getConfigManager().translate(
                plugin.getConfigManager().getPrefixedMessage("spawn.set"))));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("iraquecore.setspawn")) {
            return List.of("set");
        }
        return List.of();
    }
}
