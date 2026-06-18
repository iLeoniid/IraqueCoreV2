package gg.leo.IraqueCore.tag;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TagCommand implements TabExecutor {

    private final IraqueCore plugin;

    public TagCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
                return true;
            }
            plugin.getTagManager().openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("iraquecore.tags.admin")) {
                    sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                    return true;
                }
                plugin.getTagManager().reload();
                sender.sendMessage(Component.text("Tags reloaded.", NamedTextColor.GREEN));
            }
            case "set" -> handleSet(sender, args);
            default -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
                    return true;
                }
                plugin.getTagManager().openMainMenu(player);
            }
        }
        return true;
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.tags.admin")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /tag set <player> <tagId>", NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
            return;
        }

        String tagId = args[2].toLowerCase();
        if (plugin.getTagManager().getTag(tagId) == null) {
            sender.sendMessage(Component.text("Tag not found. Available IDs: "
                    + String.join(", ", plugin.getTagManager().getTags().keySet()), NamedTextColor.RED));
            return;
        }

        plugin.getTagManager().setPlayerTag(target, tagId);
        sender.sendMessage(Component.text("Set " + target.getName() + "'s tag to " + tagId, NamedTextColor.GREEN));
        target.sendMessage(Component.text("Your tag has been set to " + tagId + " by an admin.", NamedTextColor.GREEN));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> cmds = new ArrayList<>();
        if (sender.hasPermission("iraquecore.tags.admin")) {
            cmds.add("reload");
            cmds.add("set");
        }

        if (args.length == 1) {
            return cmds.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("set") && sender.hasPermission("iraquecore.tags.admin")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set") && sender.hasPermission("iraquecore.tags.admin")) {
            return plugin.getTagManager().getTags().keySet().stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
