package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.settings.SettingsMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SettingsCommand implements TabExecutor {

    private final IraqueCore plugin;

    public SettingsCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            send(sender, "general.player-only", "&cOnly players can use this command.");
            return true;
        }
        if (!player.hasPermission("iraquecore.admin")) {
            send(sender, "general.no-permission", "&cYou don't have permission.");
            return true;
        }
        new SettingsMenu(plugin, player).openMenu();
        return true;
    }

    private void send(CommandSender sender, String key, String fallback) {
        var cfg = plugin.getConfigManager();
        sender.sendMessage(cfg.deserialize(cfg.translate(cfg.getMessage(key, fallback))));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
