package gg.leo.IraqueCore.motd;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MotdCommand implements TabExecutor {

    private final IraqueCore plugin;

    public MotdCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    private String msg(String path) {
        return plugin.getConfigManager().translate(
                plugin.getConfigManager().getMessage(path, "&c" + path));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("iraquecore.motd.reload")) {
            sender.sendMessage(plugin.getConfigManager().deserialize(msg("motd.no-permission")));
            return true;
        }

        if (args.length < 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(plugin.getConfigManager().deserialize(msg("motd.usage")));
            return true;
        }

        plugin.getMotdManager().reload();
        sender.sendMessage(plugin.getConfigManager().deserialize(msg("motd.reloaded")));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("iraquecore.motd.reload")) {
            return List.of("reload");
        }
        return List.of();
    }
}
