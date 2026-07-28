package gg.leo.IraqueCore.scoreboard;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreboardCommand implements TabExecutor {

    private final ScoreboardManager manager;
    private final IraqueCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ScoreboardCommand(ScoreboardManager manager, IraqueCore plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    private Component msg(String path, String fallback) {
        String raw = plugin.getConfigManager().getMessage(path, fallback);
        return plugin.getConfigManager().deserialize(plugin.getConfigManager().translate(raw));
    }

    private Component msg(String path) {
        return msg(path, "<red>Mensaje no encontrado: " + path);
    }

    private String msgString(String path, String fallback) {
        return plugin.getConfigManager().translate(plugin.getConfigManager().getMessage(path, fallback));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(msg("scoreboard.player-only", "<red>Solo jugadores pueden usar este comando."));
            return true;
        }

        if (args.length == 0) {
            sendStatus(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on", "enable" -> toggleScoreboard(player, true);
            case "off", "disable" -> toggleScoreboard(player, false);
            case "toggle" -> toggleScoreboard(player, !manager.isPlayerEnabled(player));
            case "stats" -> sendStats(player);
            case "top" -> sendTop(player, args);
            case "help", "?" -> sendHelp(player, label);
            default -> player.sendMessage(msg("scoreboard.unknown", "<red>Subcomando desconocido. Usa /{label} help"));
        }
        return true;
    }

    private void toggleScoreboard(Player player, boolean enabled) {
        manager.setPlayerEnabled(player, enabled);
        player.sendMessage(enabled
            ? msg("scoreboard.enabled", "<green>Scoreboard activado.")
            : msg("scoreboard.disabled", "<red>Scoreboard desactivado."));
    }

    private void sendStatus(Player player) {
        boolean enabled = manager.isPlayerEnabled(player);
        Component status = enabled
            ? miniMessage.deserialize("<green>ACTIVADO")
            : miniMessage.deserialize("<red>DESACTIVADO");

        player.sendMessage(msg("scoreboard.status", "<gray>Estado: {status}")
            .replaceText(builder -> builder.matchLiteral("{status}").replacement(status)));
        player.sendMessage(msg("scoreboard.usage", "<gray>Uso: /sb <on|off|toggle|stats|top|help>"));
    }

    private void sendStats(Player player) {
        int broken = manager.getBlocksBroken().getOrDefault(player.getUniqueId(), 0);
        int placed = manager.getBlocksPlaced().getOrDefault(player.getUniqueId(), 0);
        int deaths = manager.getDeaths().getOrDefault(player.getUniqueId(), 0);

        player.sendMessage(miniMessage.deserialize("<dark_gray>╔══════════════════════╗"));
        player.sendMessage(miniMessage.deserialize("<gold>     📊 Tus Estadísticas"));
        player.sendMessage(miniMessage.deserialize("<dark_gray>╠══════════════════════╣"));
        player.sendMessage(miniMessage.deserialize("<gray>⛏ Bloques rotos: <yellow>" + broken));
        player.sendMessage(miniMessage.deserialize("<gray>🧱 Bloques puestos: <yellow>" + placed));
        player.sendMessage(miniMessage.deserialize("<gray>☠ Muertes: <red>" + deaths));
        player.sendMessage(miniMessage.deserialize("<dark_gray>╚══════════════════════╝"));
    }

    private void sendTop(Player player, String[] args) {
        String type = args.length > 1 ? args[1].toLowerCase() : "broken";
        int limit = Math.min(parseInt(args.length > 2 ? args[2] : "10"), 20);

        List<String> lines = switch (type) {
            case "placed", "place", "p" -> manager.getTopBlocksPlaced(limit);
            case "deaths", "death", "d" -> manager.getTopDeaths(limit);
            default -> manager.getTopBlocksBroken(limit);
        };

        player.sendMessage(miniMessage.deserialize("<gold>🏆 Top " + limit + " - " + switch (type) {
            case "placed", "place", "p" -> "Bloques Puestos";
            case "deaths", "death", "d" -> "Muertes";
            default -> "Bloques Rotos";
        }));

        int rank = 1;
        for (String line : lines) {
            String color = switch (rank) {
                case 1 -> "<gold>";
                case 2 -> "<gray>";
                case 3 -> "<#cd7f32>";
                default -> "<white>";
            };
            player.sendMessage(miniMessage.deserialize(color + "#" + rank + " " + line));
            rank++;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("iraque.scoreboard.admin")) {
            sender.sendMessage(msg("no-permission", "<red>No tienes permiso."));
            return true;
        }

        manager.loadConfig();
        manager.updateAll();
        sender.sendMessage(msg("scoreboard.reloaded", "<green>Scoreboard recargado correctamente."));
        return true;
    }

    private void sendHelp(Player player, String label) {
        player.sendMessage(miniMessage.deserialize("<gold>╔══════════════════════════╗"));
        player.sendMessage(miniMessage.deserialize("<gold>     📋 Scoreboard Help"));
        player.sendMessage(miniMessage.deserialize("<gold>╠══════════════════════════╣"));
        player.sendMessage(miniMessage.deserialize("<yellow>/" + label + " <gray>- Ver estado"));
        player.sendMessage(miniMessage.deserialize("<yellow>/" + label + " on <gray>- Activar scoreboard"));
        player.sendMessage(miniMessage.deserialize("<yellow>/" + label + " off <gray>- Desactivar scoreboard"));
        player.sendMessage(miniMessage.deserialize("<yellow>/" + label + " toggle <gray>- Alternar scoreboard"));
        player.sendMessage(miniMessage.deserialize("<yellow>/" + label + " stats <gray>- Ver tus estadísticas"));
        player.sendMessage(miniMessage.deserialize("<yellow>/" + label + " top [tipo] [cantidad] <gray>- Top jugadores"));
        player.sendMessage(miniMessage.deserialize("<gray>   Tipos: broken, placed, deaths"));
        player.sendMessage(miniMessage.deserialize("<yellow>/" + label + " reload <gray>- Recargar config (admin)"));
        player.sendMessage(miniMessage.deserialize("<gold>╚══════════════════════════╝"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                        @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("on", "off", "toggle", "stats", "top", "help"));
            if (sender.hasPermission("iraque.scoreboard.admin")) {
                completions.add("reload");
            }
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("top")) {
            completions.addAll(List.of("broken", "placed", "deaths"));
            return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("top")) {
            for (int i = 5; i <= 20; i += 5) completions.add(String.valueOf(i));
            return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
        }

        return Collections.emptyList();
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 10;
        }
    }
}
