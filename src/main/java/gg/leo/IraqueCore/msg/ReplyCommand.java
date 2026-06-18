package gg.leo.IraqueCore.msg;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    private static final String FORMAT_TO = "&7[&aMe &7-> &a{target}&7] {message}";
    private static final String FORMAT_FROM = "&7[&a{sender} &7-> &aMe&7] {message}";

    public ReplyCommand(IraqueCore plugin, MsgManager msgManager) {
        this.plugin = plugin;
        this.msgManager = msgManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /r <message>", NamedTextColor.RED));
            return true;
        }

        Optional<UUID> lastOpt = msgManager.getLastMessager(player.getUniqueId());
        if (lastOpt.isEmpty()) {
            player.sendMessage(Component.text("Nobody has messaged you recently.", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayer(lastOpt.get());
        if (target == null || !target.isOnline()) {
            player.sendMessage(Component.text("That player is no longer online.", NamedTextColor.RED));
            msgManager.remove(player.getUniqueId());
            return true;
        }

        String message = String.join(" ", args);

        msgManager.setLastMessager(target.getUniqueId(), player.getUniqueId());

        String toMsg = FORMAT_TO
                .replace("{target}", target.getName())
                .replace("{message}", message);
        String fromMsg = FORMAT_FROM
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
        return LegacyComponentSerializer.legacySection().deserialize(
                text.replace("&", "\u00a7")
        );
    }
}
