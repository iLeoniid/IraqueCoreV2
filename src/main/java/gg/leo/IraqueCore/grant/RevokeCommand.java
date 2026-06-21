package gg.leo.IraqueCore.grant;

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

public class RevokeCommand implements TabExecutor {

    private final IraqueCore plugin;

    public RevokeCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    private String msg(String path) {
        return plugin.getConfigManager().translate(
                plugin.getConfigManager().getMessage(path, "&c" + path));
    }

    private Component txt(String path) {
        return plugin.getConfigManager().getMessageComponent(path);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("iraquecore.grant.revoke")) {
            sender.sendMessage(txt("grant.no-permission-revoke"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("grant.usage-revoke")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(txt("general.player-not-found"));
            return true;
        }

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) reasonBuilder.append(" ");
            reasonBuilder.append(args[i]);
        }
        String reason = reasonBuilder.toString();

        UUID senderUUID = sender instanceof Player player ? player.getUniqueId() : UUID.fromString("00000000-0000-0000-0000-000000000001");

        boolean success = plugin.getGrantManager().revoke(target.getUniqueId(), senderUUID, reason);

        if (success) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("grant.revoked")
                            .replace("{player}", target.getName())
                            .replace("{reason}", reason)));

            target.sendMessage(plugin.getConfigManager().deserialize(
                    msg("grant.revoked-target")
                            .replace("{reason}", reason)));
        } else {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    msg("grant.no-active-grants")
                            .replace("{player}", target.getName())));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("iraquecore.grant.revoke")) return List.of();
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
