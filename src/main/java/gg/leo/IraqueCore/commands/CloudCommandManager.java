package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.alerts.Alert;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;

public final class CloudCommandManager {

    private final IraqueCore plugin;
    private final TrashCommand trashCommand;
    private final LegacyPaperCommandManager<CommandSender> manager;

    public CloudCommandManager(IraqueCore plugin, TrashCommand trashCommand) {
        this.plugin = plugin;
        this.trashCommand = trashCommand;
        this.manager = LegacyPaperCommandManager.createNative(
                plugin,
                ExecutionCoordinator.simpleCoordinator()
        );
        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }
    }

    public void registerCommands() {
        registerHeal();
        registerFeed();
        registerAlert();
        registerTrash();
    }

    private void registerHeal() {
        manager.command(
                manager.commandBuilder("heal", "curar")
                        .optional("target", PlayerParser.playerParser())
                        .handler(ctx -> {
                            CommandSender sender = ctx.sender();
                            if (!sender.hasPermission("iraquecore.heal")) {
                                sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.no-permission"));
                                return;
                            }

                            Player target = ctx.getOrDefault("target", null);
                            if (target == null) {
                                if (!(sender instanceof Player player)) {
                                    sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.player-only"));
                                    return;
                                }
                                target = player;
                            } else if (!sender.hasPermission("iraquecore.heal.other")) {
                                sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.no-permission"));
                                return;
                            }

                            heal(sender, target);
                        })
        );
    }

    private void heal(CommandSender sender, Player target) {
        target.setHealth(target.getAttribute(Attribute.MAX_HEALTH).getDefaultValue());
        target.setFoodLevel(20);
        target.setSaturation(10f);
        target.getActivePotionEffects().forEach(e -> target.removePotionEffect(e.getType()));
        target.setFireTicks(0);

        target.sendMessage(plugin.getConfigManager().getMessageComponent("heal.target"));
        if (!target.equals(sender)) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("heal.other", "&aHealed {player}.")
                                    .replace("{player}", target.getName()))));
        }
    }

    private void registerFeed() {
        manager.command(
                manager.commandBuilder("feed", "alimentar")
                        .optional("target", PlayerParser.playerParser())
                        .handler(ctx -> {
                            CommandSender sender = ctx.sender();
                            if (!sender.hasPermission("iraquecore.feed")) {
                                sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.no-permission"));
                                return;
                            }

                            Player target = ctx.getOrDefault("target", null);
                            if (target == null) {
                                if (!(sender instanceof Player player)) {
                                    sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.player-only"));
                                    return;
                                }
                                target = player;
                            } else if (!sender.hasPermission("iraquecore.feed.other")) {
                                sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.no-permission"));
                                return;
                            }

                            feed(sender, target);
                        })
        );
    }

    private void feed(CommandSender sender, Player target) {
        target.setFoodLevel(20);
        target.setSaturation(10f);

        target.sendMessage(plugin.getConfigManager().getMessageComponent("feed.target"));
        if (!target.equals(sender)) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("feed.other", "&aFed {player}.")
                                    .replace("{player}", target.getName()))));
        }
    }

    private void registerAlert() {
        manager.command(
                manager.commandBuilder("alert", "alerta")
                        .literal("list")
                        .handler(ctx -> handleAlertList(ctx.sender()))
        );
        manager.command(
                manager.commandBuilder("alert", "alerta")
                        .literal("reload")
                        .handler(ctx -> handleAlertReload(ctx.sender()))
        );
        manager.command(
                manager.commandBuilder("alert", "alerta")
                        .literal("test")
                        .required("name", StringParser.stringParser())
                        .handler(ctx -> handleAlertTest(ctx.sender(), ctx.get("name")))
        );
        manager.command(
                manager.commandBuilder("alert", "alerta")
                        .required("name", StringParser.stringParser())
                        .handler(ctx -> handleAlertSend(ctx.sender(), ctx.get("name")))
        );
    }

    private void registerTrash() {
        manager.command(
                manager.commandBuilder("trash", "lixeira", "dispose")
                        .handler(ctx -> {
                            CommandSender sender = ctx.sender();
                            if (!sender.hasPermission("iraquecore.trash")) {
                                sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.no-permission"));
                                return;
                            }
                            if (!(sender instanceof Player player)) {
                                sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.player-only"));
                                return;
                            }
                            trashCommand.openTrash(player);
                        })
        );
    }

    private void handleAlertSend(CommandSender sender, String alertId) {
        if (!sender.hasPermission("alert.use")) {
            msg(sender, "&cYou don't have permission to send alerts.");
            return;
        }

        Alert alert = plugin.getAlertManager().getAlert(alertId);
        if (alert == null) {
            msg(sender, "&cAlert '&e" + alertId + "&c' not found. Use &e/alert list");
            return;
        }

        Player playerSource = sender instanceof Player ? (Player) sender : null;
        plugin.getAlertManager().sendToAll(playerSource, alert);
        msg(sender, "&aAlert '&e" + alertId + "&a' sent to all players.");
    }

    private void handleAlertList(CommandSender sender) {
        if (!sender.hasPermission("alert.list")) {
            msg(sender, "&cYou don't have permission to list alerts.");
            return;
        }

        if (plugin.getAlertManager().getAlerts().isEmpty()) {
            msg(sender, "&eNo alerts configured.");
            return;
        }

        msg(sender, "&6─── Available Alerts (" + plugin.getAlertManager().getAlerts().size() + ") ───");
        for (Alert alert : plugin.getAlertManager().getAlerts().values()) {
            String sound = alert.sound() != null && alert.sound().enabled() ? alert.sound().id() : "none";
            msg(sender, "&7  &e" + alert.id() + " &8- &7sound: " + sound);
        }
    }

    private void handleAlertReload(CommandSender sender) {
        if (!sender.hasPermission("alert.reload")) {
            msg(sender, "&cYou don't have permission to reload alerts.");
            return;
        }

        plugin.getAlertManager().reload();
        msg(sender, "&aAlerts reloaded successfully.");
    }

    private void handleAlertTest(CommandSender sender, String name) {
        if (!sender.hasPermission("alert.test")) {
            msg(sender, "&cYou don't have permission to test alerts.");
            return;
        }

        if (!(sender instanceof Player player)) {
            msg(sender, "&cOnly players can test alerts.");
            return;
        }

        Alert alert = plugin.getAlertManager().getAlert(name);
        if (alert == null) {
            msg(sender, "&cAlert '&e" + name + "&c' not found.");
            return;
        }

        plugin.getAlertManager().sendToPlayer(player, alert);
        msg(sender, "&aAlert '&e" + name + "&a' sent to you.");
    }

    private void msg(CommandSender sender, String text) {
        sender.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(text)));
    }
}
