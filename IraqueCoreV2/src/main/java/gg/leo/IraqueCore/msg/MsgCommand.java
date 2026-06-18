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
import java.util.stream.Collectors;

public class MsgCommand implements TabExecutor {

    private final IraqueCore plugin;
    private final MsgManager msgManager;

    private static final String FORMAT_TO = "&7[&aMe &7-> &a{target}&7] {message}";
    private static final String FORMAT_FROM = "&7[&a{sender} &7-> &aMe&7] {message}";

    public MsgCommand(IraqueCore plugin, MsgManager msgManager) {
        this.plugin = plugin;
        this.msgManager = msgManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /msg <player> <message>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(Component.text("You cannot message yourself.", NamedTextColor.RED));
            return true;
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

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
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private Component legacy(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(
                text.replace("&", "\u00a7")
        );
    }
}
