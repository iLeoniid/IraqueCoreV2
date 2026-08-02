package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.punishment.PunishmentManager;
import gg.leo.IraqueCore.utils.SchedulerUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameModeCommand implements TabExecutor {

    private final IraqueCore plugin;

    private static final String PERM = "iraquecore.gamemode";
    private static final String PERM_OTHER = "iraquecore.gamemode.other";

    private final Map<UUID, TempMode> tempModes = new HashMap<>();
    private BukkitTask tempTask;

    private record TempMode(GameMode mode, GameMode previous, long expireAt) {}

    public GameModeCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void startTask() {
        tempTask = SchedulerUtil.runTimer(plugin, this::tick, 20L, 20L);
    }

    public void shutdown() {
        if (tempTask != null) {
            tempTask.cancel();
            tempTask = null;
        }
        tempModes.clear();
    }

    private void tick() {
        if (tempModes.isEmpty()) return;
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, TempMode>> it = tempModes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, TempMode> entry = it.next();
            TempMode tm = entry.getValue();
            if (now < tm.expireAt()) continue;
            it.remove();
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline() && player.getGameMode() == tm.mode()) {
                player.setGameMode(tm.previous());
                player.sendMessage(plugin.getConfigManager().deserialize(
                        msg("gamemode.expired").replace("{mode}", tm.previous().name().toLowerCase())));
            }
        }
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
                sender.sendMessage(plugin.getConfigManager().deserialize(
                        msg("gamemode.usage").replace("{label}", label)));
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

        String modeName = mode.name().toLowerCase();

        if (args.length > playerArgIndex + 1) {
            long millis = PunishmentManager.parseDuration(args[playerArgIndex + 1]);
            if (millis <= 0) {
                sender.sendMessage(txt("gamemode.invalid-duration"));
                return true;
            }
            applyTemp(target, mode, millis, sender, modeName);
        } else {
            tempModes.remove(target.getUniqueId());
            target.setGameMode(mode);
            target.sendMessage(plugin.getConfigManager().deserialize(
                    msg("gamemode.set").replace("{mode}", modeName)));
            if (!target.equals(sender)) {
                sender.sendMessage(plugin.getConfigManager().deserialize(
                        msg("gamemode.set-other")
                                .replace("{player}", target.getName())
                                .replace("{mode}", modeName)));
            }
        }
        return true;
    }

    private void applyTemp(Player target, GameMode mode, long millis, CommandSender sender, String modeName) {
        String timeStr = PunishmentManager.formatDuration(millis);
        tempModes.remove(target.getUniqueId());
        tempModes.put(target.getUniqueId(),
                new TempMode(mode, target.getGameMode(), System.currentTimeMillis() + millis));
        target.setGameMode(mode);

        target.sendMessage(plugin.getConfigManager().deserialize(
                msg("gamemode.set-temp")
                        .replace("{mode}", modeName)
                        .replace("{time}", timeStr)));
        if (!target.equals(sender)) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("gamemode.set-other-temp")
                            .replace("{player}", target.getName())
                            .replace("{mode}", modeName)
                            .replace("{time}", timeStr)));
        }
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
            if (args.length == 2 && sender.hasPermission(PERM_OTHER)) {
                return List.of("30s", "1m", "5m", "30m", "1h", "12h", "1d", "7d", "30d").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
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

        if (args.length == 3 && sender.hasPermission(PERM_OTHER)) {
            return List.of("30s", "1m", "5m", "30m", "1h", "12h", "1d", "7d", "30d").stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
