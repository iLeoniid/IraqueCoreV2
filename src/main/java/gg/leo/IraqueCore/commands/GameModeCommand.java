package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class GameModeCommand implements TabExecutor {

    private final IraqueCore plugin;

    private static final String PERM = "iraquecore.gamemode";
    private static final String PERM_OTHER = "iraquecore.gamemode.other";

    public GameModeCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    private String msg(String path) {
        return plugin.getConfigManager().translate(
                plugin.getConfigManager().getMessage(path, "&c" + path));
    }

    private Component txt(String path) {
        return Component.text(msg(path));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERM)) {
            sender.sendMessage(txt("gamemode.no-permission"));
            return true;
        }

        boolean isShortcut = switch (label.toLowerCase()) {
            case "gmc", "gms", "gma", "gmsp" -> true;
            default -> false;
        };

        GameMode mode;
        int playerArgIndex;

        if (isShortcut) {
            mode = switch (label.toLowerCase()) {
                case "gmc" -> GameMode.CREATIVE;
                case "gms" -> GameMode.SURVIVAL;
                case "gma" -> GameMode.ADVENTURE;
                default -> GameMode.SPECTATOR;
            };
            playerArgIndex = 0;
        } else {
            if (args.length == 0) {
                sender.sendMessage(Component.text(msg("gamemode.usage")
                        .replace("{label}", label)));
                return true;
            }
            mode = parseMode(args[0]);
            if (mode == null) {
                sender.sendMessage(txt("gamemode.invalid"));
                return true;
            }
            playerArgIndex = 1;
        }

        Player target;
        if (args.length > playerArgIndex) {
            if (!sender.hasPermission(PERM_OTHER)) {
                sender.sendMessage(txt("gamemode.no-permission-other"));
                return true;
            }
            target = Bukkit.getPlayer(args[playerArgIndex]);
            if (target == null) {
                sender.sendMessage(txt("gamemode.player-not-found"));
                return true;
            }
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(txt("gamemode.player-only"));
                return true;
            }
            target = player;
        }

        target.setGameMode(mode);
        String modeName = mode.name().toLowerCase();
        target.sendMessage(Component.text(msg("gamemode.set")
                .replace("{mode}", modeName)));
        if (!target.equals(sender)) {
            sender.sendMessage(Component.text(msg("gamemode.set-other")
                    .replace("{player}", target.getName())
                    .replace("{mode}", modeName)));
        }
        return true;
    }

    private GameMode parseMode(String arg) {
        return switch (arg.toLowerCase()) {
            case "0", "survival" -> GameMode.SURVIVAL;
            case "1", "creative" -> GameMode.CREATIVE;
            case "2", "adventure" -> GameMode.ADVENTURE;
            case "3", "spectator" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERM)) return List.of();

        boolean isShortcut = switch (label.toLowerCase()) {
            case "gmc", "gms", "gma", "gmsp" -> true;
            default -> false;
        };

        if (isShortcut) {
            if (args.length == 1 && sender.hasPermission(PERM_OTHER)) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return List.of();
        }

        if (args.length == 1) {
            return List.of("survival", "creative", "adventure", "spectator", "0", "1", "2", "3").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && sender.hasPermission(PERM_OTHER)) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}