package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
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
            sender.sendMessage(Component.text(plugin.getConfigManager().translate(
                    plugin.getConfigManager().getMessage("general.no-permission", "&cYou don't have permission."))));
            return true;
        }

        try {
            plugin.reload();
            sender.sendMessage(Component.text(plugin.getConfigManager().translate(
                    plugin.getConfigManager().getPrefixedMessage("reload.success"))));
        } catch (Exception e) {
            sender.sendMessage(Component.text(plugin.getConfigManager().translate(
                    plugin.getConfigManager().getPrefixedMessage("reload.failed"))));
            plugin.getPluginLogger().error("Error reloading plugin", e);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
