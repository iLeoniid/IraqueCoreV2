package gg.leo.IraqueCore.playtime;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaytimeCommand implements TabExecutor {

    private final IraqueCore plugin;

    public PlaytimeCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        PlaytimeManager ptm = plugin.getPlaytimeManager();
        if (ptm == null) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate("&cPlaytime is disabled.")));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().translate(
                                plugin.getConfigManager().getMessage("general.player-only", "&cOnly players can use this command."))));
                return true;
            }
            long time = ptm.getPlaytime(player.getUniqueId());
            String msg = plugin.getConfigManager().getMessage("playtime.your", "&aYour playtime: &e{time}")
                    .replace("{time}", PlaytimeManager.formatTime(time));
            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(msg)));
            return true;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("iraquecore.playtime.other")) {
                sender.sendMessage(plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().translate(
                                plugin.getConfigManager().getMessage("general.no-permission", "&cYou don't have permission."))));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().translate(
                                plugin.getConfigManager().getMessage("general.player-not-found", "&cPlayer not found."))));
                return true;
            }

            long time = ptm.getPlaytime(target.getUniqueId());
            String msg = plugin.getConfigManager().getMessage("playtime.other", "&a{player}'s playtime: &e{time}")
                    .replace("{player}", target.getName())
                    .replace("{time}", PlaytimeManager.formatTime(time));
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(msg)));
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("iraquecore.playtime.other")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
