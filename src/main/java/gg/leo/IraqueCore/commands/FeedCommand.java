package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class FeedCommand implements TabExecutor {

    private final IraqueCore plugin;

    public FeedCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("iraquecore.feed")) {
            sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.no-permission"));
            return true;
        }

        Player target;
        if (args.length > 0) {
            if (!sender.hasPermission("iraquecore.feed.other")) {
                sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.no-permission"));
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.player-not-found"));
                return true;
            }
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.player-only"));
                return true;
            }
            target = player;
        }

        target.setFoodLevel(20);
        target.setSaturation(10f);

        target.sendMessage(plugin.getConfigManager().getMessageComponent("feed.target"));
        if (!target.equals(sender)) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("feed.other", "&aFed {player}.")
                                    .replace("{player}", target.getName()))));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("iraquecore.feed.other")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
