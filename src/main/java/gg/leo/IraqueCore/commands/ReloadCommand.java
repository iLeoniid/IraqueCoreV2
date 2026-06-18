package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReloadCommand implements TabExecutor {

    private final IraqueCore plugin;

    public ReloadCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("iraquecore.reload")) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("general.no-permission", "&cYou don't have permission."))));
            return true;
        }

        try {
            plugin.reload();
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().getPrefixedMessage("reload.success")));
        } catch (Exception e) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().getPrefixedMessage("reload.failed")));
            plugin.getPluginLogger().error("Error reloading plugin", e);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
