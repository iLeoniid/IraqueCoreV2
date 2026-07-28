package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class IraqueCoreCommand implements TabExecutor {

    private final IraqueCore plugin;

    public IraqueCoreCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sendInfo(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "settings" -> handleSettings(sender);
            default -> sendInfo(sender);
        }
        return true;
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  \u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557")
                .color(NamedTextColor.DARK_GRAY));
        sender.sendMessage(Component.text("  \u2551   ")
                .color(NamedTextColor.DARK_GRAY)
                .append(Component.text("IraqueCore")
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" v" + plugin.getPluginMeta().getVersion())
                        .color(NamedTextColor.GRAY))
                .append(Component.text("        \u2551")
                        .color(NamedTextColor.DARK_GRAY)));
        sender.sendMessage(Component.text("  \u2560\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2563")
                .color(NamedTextColor.DARK_GRAY));
        sender.sendMessage(Component.text("  \u2551   ")
                .color(NamedTextColor.DARK_GRAY)
                .append(Component.text("/iraque reload")
                        .color(NamedTextColor.GREEN))
                .append(Component.text("  - Reload configs")
                        .color(NamedTextColor.GRAY))
                .append(Component.text("      \u2551")
                        .color(NamedTextColor.DARK_GRAY)));
        sender.sendMessage(Component.text("  \u2551   ")
                .color(NamedTextColor.DARK_GRAY)
                .append(Component.text("/iraque settings")
                        .color(NamedTextColor.GREEN))
                .append(Component.text(" - Open settings GUI")
                        .color(NamedTextColor.GRAY))
                .append(Component.text(" \u2551")
                        .color(NamedTextColor.DARK_GRAY)));
        sender.sendMessage(Component.text("  \u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D")
                .color(NamedTextColor.DARK_GRAY));
        sender.sendMessage(Component.empty());
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("iraquecore.reload")) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("general.no-permission", "&cYou don't have permission."))));
            return;
        }

        try {
            String reloaded = plugin.reload();
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().getPrefixedMessage("reload.success")));
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            "&7Recargado: &f" + reloaded)));
        } catch (Exception e) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().getPrefixedMessage("reload.failed")));
            plugin.getPluginLogger().error("Error reloading plugin", e);
        }
    }

    private void handleSettings(CommandSender sender) {
        if (!(sender instanceof org.bukkit.entity.Player player)) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("general.player-only", "&cOnly players can use this command."))));
            return;
        }
        if (!sender.hasPermission("iraquecore.admin")) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("general.no-permission", "&cYou don't have permission."))));
            return;
        }
        new gg.leo.IraqueCore.settings.SettingsMenu(plugin, player).openMenu();
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "settings").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
