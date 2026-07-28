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
import java.util.Optional;
import java.util.UUID;

public class ReplyCommand implements TabExecutor {

    private final IraqueCore plugin;
    private final MsgManager msgManager;

    public ReplyCommand(IraqueCore plugin, MsgManager msgManager) {
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
            sender.sendMessage(plugin.getConfigManager().deserialize(msg("reply.player-only")));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().deserialize(msg("reply.usage")));
            return true;
        }

        Optional<UUID> lastOpt = msgManager.getLastMessager(player.getUniqueId());
        if (lastOpt.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().deserialize(msg("reply.nobody")));
            return true;
        }

        Player target = Bukkit.getPlayer(lastOpt.get());
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.getConfigManager().deserialize(msg("reply.not-online")));
            msgManager.remove(player.getUniqueId());
            return true;
        }

        String message = String.join(" ", args);

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
        return List.of();
    }

    private Component legacy(String text) {
        return plugin.getConfigManager().deserialize(text);
    }
}
