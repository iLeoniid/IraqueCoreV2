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

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
