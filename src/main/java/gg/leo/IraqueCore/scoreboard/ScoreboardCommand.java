package gg.leo.IraqueCore.scoreboard;

import gg.leo.IraqueCore.IraqueCore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class ScoreboardCommand
implements TabExecutor {
    private final ScoreboardManager manager;
    private final IraqueCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ScoreboardCommand(ScoreboardManager manager, IraqueCore plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    private Component msg(String path, String fallback) {
        String raw = this.plugin.getConfigManager().getMessage(path, fallback);
        return this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(raw));
    }

    private Component msg(String path) {
        return this.msg(path, "<red>Mensaje no encontrado: " + path);
    }

    private String msgString(String path, String fallback) {
        return this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage(path, fallback));
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            return this.handleReload(sender);
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.msg("scoreboard.player-only", "<red>Solo jugadores pueden usar este comando."));
            return true;
        }
        Player player = (Player)sender;
        if (args.length == 0) {
            this.sendStatus(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "on": 
            case "enable": {
                this.toggleScoreboard(player, true);
                break;
            }
            case "off": 
            case "disable": {
                this.toggleScoreboard(player, false);
                break;
            }
            case "toggle": {
                this.toggleScoreboard(player, !this.manager.isPlayerEnabled(player));
                break;
            }
            case "stats": {
                this.sendStats(player);
                break;
            }
            case "top": {
                this.sendTop(player, args);
                break;
            }
            case "help": 
            case "?": {
                this.sendHelp(player, label);
                break;
            }
            default: {
                player.sendMessage(this.msg("scoreboard.unknown", "<red>Subcomando desconocido. Usa /{label} help"));
            }
        }
        return true;
    }

    private void toggleScoreboard(Player player, boolean enabled) {
        this.manager.setPlayerEnabled(player, enabled);
        player.sendMessage(enabled ? this.msg("scoreboard.enabled", "<green>Scoreboard activado.") : this.msg("scoreboard.disabled", "<red>Scoreboard desactivado."));
    }

    private void sendStatus(Player player) {
        boolean enabled = this.manager.isPlayerEnabled(player);
        Component status = enabled ? this.miniMessage.deserialize("<green>ACTIVADO") : this.miniMessage.deserialize("<red>DESACTIVADO");
        player.sendMessage(this.msg("scoreboard.status", "<gray>Estado: {status}").replaceText(builder -> builder.matchLiteral("{status}").replacement((ComponentLike)status)));
        player.sendMessage(this.msg("scoreboard.usage", "<gray>Uso: /sb <on|off|toggle|stats|top|help>"));
    }

    private void sendStats(Player player) {
        int broken = this.manager.getBlocksBroken().getOrDefault(player.getUniqueId(), 0);
        int placed = this.manager.getBlocksPlaced().getOrDefault(player.getUniqueId(), 0);
        int deaths = this.manager.getDeaths().getOrDefault(player.getUniqueId(), 0);
        player.sendMessage(this.miniMessage.deserialize("<dark_gray>\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557"));
        player.sendMessage(this.miniMessage.deserialize("<gold>     \ud83d\udcca Tus Estad\u00edsticas"));
        player.sendMessage(this.miniMessage.deserialize("<dark_gray>\u2560\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2563"));
        player.sendMessage(this.miniMessage.deserialize("<gray>\u26cf Bloques rotos: <yellow>" + broken));
        player.sendMessage(this.miniMessage.deserialize("<gray>\ud83e\uddf1 Bloques puestos: <yellow>" + placed));
        player.sendMessage(this.miniMessage.deserialize("<gray>\u2620 Muertes: <red>" + deaths));
        player.sendMessage(this.miniMessage.deserialize("<dark_gray>\u255a\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255d"));
    }

    private void sendTop(Player player, String[] args) {
        String type = args.length > 1 ? args[1].toLowerCase() : "broken";
        int limit = Math.min(this.parseInt(args.length > 2 ? args[2] : "10"), 20);
        List<String> lines = switch (type) {
            case "placed", "place", "p" -> this.manager.getTopBlocksPlaced(limit);
            case "deaths", "death", "d" -> this.manager.getTopDeaths(limit);
            default -> this.manager.getTopBlocksBroken(limit);
        };
        player.sendMessage(this.miniMessage.deserialize("<gold>\ud83c\udfc6 Top " + limit + " - " + (switch (type) {
            case "placed", "place", "p" -> "Bloques Puestos";
            case "deaths", "death", "d" -> "Muertes";
            default -> "Bloques Rotos";
        })));
        int rank = 1;
        for (String line : lines) {
            String color = switch (rank) {
                case 1 -> "<gold>";
                case 2 -> "<gray>";
                case 3 -> "<#cd7f32>";
                default -> "<white>";
            };
            player.sendMessage(this.miniMessage.deserialize(color + "#" + rank + " " + line));
            ++rank;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("iraque.scoreboard.admin")) {
            sender.sendMessage(this.msg("no-permission", "<red>No tienes permiso."));
            return true;
        }
        this.manager.loadConfig();
        this.manager.updateAll();
        sender.sendMessage(this.msg("scoreboard.reloaded", "<green>Scoreboard recargado correctamente."));
        return true;
    }

    private void sendHelp(Player player, String label) {
        player.sendMessage(this.miniMessage.deserialize("<gold>\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557"));
        player.sendMessage(this.miniMessage.deserialize("<gold>     \ud83d\udccb Scoreboard Help"));
        player.sendMessage(this.miniMessage.deserialize("<gold>\u2560\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2563"));
        player.sendMessage(this.miniMessage.deserialize("<yellow>/" + label + " <gray>- Ver estado"));
        player.sendMessage(this.miniMessage.deserialize("<yellow>/" + label + " on <gray>- Activar scoreboard"));
        player.sendMessage(this.miniMessage.deserialize("<yellow>/" + label + " off <gray>- Desactivar scoreboard"));
        player.sendMessage(this.miniMessage.deserialize("<yellow>/" + label + " toggle <gray>- Alternar scoreboard"));
        player.sendMessage(this.miniMessage.deserialize("<yellow>/" + label + " stats <gray>- Ver tus estad\u00edsticas"));
        player.sendMessage(this.miniMessage.deserialize("<yellow>/" + label + " top [tipo] [cantidad] <gray>- Top jugadores"));
        player.sendMessage(this.miniMessage.deserialize("<gray>   Tipos: broken, placed, deaths"));
        player.sendMessage(this.miniMessage.deserialize("<yellow>/" + label + " reload <gray>- Recargar config (admin)"));
        player.sendMessage(this.miniMessage.deserialize("<gold>\u255a\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255d"));
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ArrayList<String> completions = new ArrayList<>();
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
            for (int i = 5; i <= 20; i += 5) {
                completions.add(String.valueOf(i));
            }
            return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
        }
        return Collections.emptyList();
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            return 10;
        }
    }
}
