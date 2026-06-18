package gg.leo.IraqueCore.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScoreboardCommand implements TabExecutor {

    private final ScoreboardManager manager;

    public ScoreboardCommand(ScoreboardManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            boolean current = manager.isPlayerEnabled(player);
            player.sendMessage(Component.text("Scoreboard: ", NamedTextColor.GRAY)
                    .append(Component.text(current ? "ON" : "OFF", current ? NamedTextColor.GREEN : NamedTextColor.RED)));
            player.sendMessage(Component.text("Usage: /scoreboard on|off", NamedTextColor.YELLOW));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on" -> {
                manager.setPlayerEnabled(player, true);
                player.sendMessage(Component.text("Scoreboard enabled.", NamedTextColor.GREEN));
            }
            case "off" -> {
                manager.setPlayerEnabled(player, false);
                player.sendMessage(Component.text("Scoreboard disabled.", NamedTextColor.RED));
            }
            default -> {
                player.sendMessage(Component.text("Usage: /scoreboard on|off", NamedTextColor.YELLOW));
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
