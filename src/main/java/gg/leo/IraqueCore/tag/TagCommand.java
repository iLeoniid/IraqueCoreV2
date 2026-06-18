package gg.leo.IraqueCore.tag;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TagCommand implements TabExecutor {

    private final IraqueCore plugin;

    public TagCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            plugin.getTagManager().openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!player.hasPermission("iraquecore.tags.admin")) {
                    player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                    return true;
                }
                plugin.getTagManager().reload();
                player.sendMessage(Component.text("Tags reloaded.", NamedTextColor.GREEN));
            }
            default -> plugin.getTagManager().openMainMenu(player);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("iraquecore.tags.admin")) {
            return List.of("reload");
        }
        return List.of();
    }
}
