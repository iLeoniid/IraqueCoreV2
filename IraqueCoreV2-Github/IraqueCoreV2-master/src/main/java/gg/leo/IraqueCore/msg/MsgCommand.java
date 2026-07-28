package gg.leo.IraqueCore.msg;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class MsgCommand implements TabExecutor {

    private final IraqueCore plugin;
    private final MsgManager msgManager;

    public MsgCommand(IraqueCore plugin, MsgManager msgManager) {
        this.plugin = plugin;
        this.msgManager = msgManager;
    }

    private String msg(String path) {
        return plugin.getConfigManager().translate(
                plugin.getConfigManager().getMessage(path, "&c" + path));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().deserialize(msg("msg.player-only")));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().deserialize(msg("msg.usage")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getConfigManager().deserialize(msg("msg.player-not-found")));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(plugin.getConfigManager().deserialize(msg("msg.cannot-self")));
            return true;
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        msgManager.setLastMessager(target.getUniqueId(), player.getUniqueId());

        String toMsg = msg("msg.format-to")
                .replace("{target}", target.getName())
                .replace("{message}", message);
        String fromMsg = msg("msg.format-from")
                .replace("{sender}", player.getName())
                .replace("{message}", message);

        player.sendMessage(legacy(toMsg));
        target.sendMessage(legacy(fromMsg));

        plugin.getPluginLogger().info("[MSG] {} -> {}: {}", player.getName(), target.getName(), message);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private Component legacy(String text) {
        return plugin.getConfigManager().deserialize(text);
    }
}
