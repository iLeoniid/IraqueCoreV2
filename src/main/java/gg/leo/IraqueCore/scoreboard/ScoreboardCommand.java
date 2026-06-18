package gg.leo.IraqueCore.scoreboard;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScoreboardCommand implements TabExecutor {

    private final ScoreboardManager manager;
    private final IraqueCore plugin;

    public ScoreboardCommand(ScoreboardManager manager, IraqueCore plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    private String msg(String path) {
        return plugin.getConfigManager().translate(
                plugin.getConfigManager().getMessage(path, "&c" + path));
    }

    private Component txt(String path) {
        return plugin.getConfigManager().getMessageComponent(path);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(txt("scoreboard.player-only"));
            return true;
        }

        if (args.length == 0) {
            boolean current = manager.isPlayerEnabled(player);
            player.sendMessage(plugin.getConfigManager().deserialize(
                    msg("scoreboard.status")
                            .replace("{status}", current ? "ON" : "OFF")));
            player.sendMessage(txt("scoreboard.usage"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on" -> {
                manager.setPlayerEnabled(player, true);
                player.sendMessage(txt("scoreboard.enabled"));
            }
            case "off" -> {
                manager.setPlayerEnabled(player, false);
                player.sendMessage(txt("scoreboard.disabled"));
            }
            default -> {
                player.sendMessage(txt("scoreboard.usage"));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return List.of("on", "off");
        return List.of();
    }
}
