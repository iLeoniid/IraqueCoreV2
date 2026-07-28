package gg.leo.IraqueCore.tag;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
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

    private Component txt(String path) {
        return plugin.getConfigManager().getMessageComponent(path);
    }

    private Component txt(String path, String placeholder, String value) {
        return plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translateAndReplace(
                        plugin.getConfigManager().getMessage(path, "&c" + path),
                        placeholder, value));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(txt("tag.player-only"));
                return true;
            }
            plugin.getTagManager().openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("iraquecore.tags.admin")) {
                    sender.sendMessage(txt("tag.no-permission"));
                    return true;
                }
                plugin.getTagManager().reload();
                sender.sendMessage(txt("tag.reloaded"));
            }
            case "set" -> handleSet(sender, args);
            default -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(txt("tag.player-only"));
                    return true;
                }
                plugin.getTagManager().openMainMenu(player);
            }
        }
        return true;
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.tags.admin")) {
            sender.sendMessage(txt("tag.no-permission"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(txt("tag.set.usage"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(txt("tag.set.player-not-found"));
            return;
        }

        String tagId = args[2].toLowerCase();
        if (plugin.getTagManager().getTag(tagId) == null) {
            sender.sendMessage(txt("tag.set.tag-not-found", "{ids}",
                    String.join(", ", plugin.getTagManager().getTags().keySet())));
            return;
        }

        plugin.getTagManager().setPlayerTag(target, tagId);
        sender.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("tag.set.success")
                                .replace("{player}", target.getName())
                                .replace("{tag}", tagId))));
        target.sendMessage(txt("tag.set.notify", "{tag}", tagId));
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
