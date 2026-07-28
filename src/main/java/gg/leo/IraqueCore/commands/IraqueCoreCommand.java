package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IraqueCoreCommand implements TabExecutor {

    private final IraqueCore plugin;

    public IraqueCoreCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }

        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  ╔═══════════════════════════════╗")
                .color(NamedTextColor.DARK_GRAY));
        sender.sendMessage(Component.text("  ║   ")
                .color(NamedTextColor.DARK_GRAY)
                .append(Component.text("IraqueCore")
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" v" + plugin.getPluginMeta().getVersion())
                        .color(NamedTextColor.GRAY))
                .append(Component.text("        ║")
                        .color(NamedTextColor.DARK_GRAY)));
        sender.sendMessage(Component.text("  ╠═══════════════════════════════╣")
                .color(NamedTextColor.DARK_GRAY));
        sender.sendMessage(Component.text("  ║   ")
                .color(NamedTextColor.DARK_GRAY)
                .append(Component.text("Hecho con mucho amor")
                        .color(NamedTextColor.AQUA))
                .append(Component.text("            ║")
                        .color(NamedTextColor.DARK_GRAY)));
        sender.sendMessage(Component.text("  ║   ")
                .color(NamedTextColor.DARK_GRAY)
                .append(Component.text("por ")
                        .color(NamedTextColor.GRAY))
                .append(Component.text("Proctocol")
                        .color(NamedTextColor.LIGHT_PURPLE)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text("          ║")
                        .color(NamedTextColor.DARK_GRAY)));
        sender.sendMessage(Component.text("  ╚═══════════════════════════════╝")
                .color(NamedTextColor.DARK_GRAY));
        sender.sendMessage(Component.empty());
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("iraquecore.reload")) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("general.no-permission", "&cYou don't have permission."))));
            return true;
        }

        try {
            plugin.reload();
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().getPrefixedMessage("reload.success")));
        } catch (Exception e) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().getPrefixedMessage("reload.failed")));
            plugin.getPluginLogger().error("Error reloading plugin", e);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("iraquecore.reload")) {
            return List.of("reload");
        }
        return List.of();
    }
}
