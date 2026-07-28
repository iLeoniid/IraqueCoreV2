package gg.leo.IraqueCore.punishment;

import gg.leo.IraqueCore.IraqueCore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PunishmentCommand implements TabExecutor {

    private final IraqueCore plugin;
    private final PunishmentManager manager;

    public PunishmentCommand(IraqueCore plugin, PunishmentManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    private Component msg(String path, String fallback) {
        String raw = plugin.getConfigManager().getMessage(path, fallback);
        return plugin.getConfigManager().deserialize(plugin.getConfigManager().translate(raw));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(msg("punishment." + label + ".usage",
                    "&cUsage: /" + label + " <player> [time] [reason...]"));
            return true;
        }

        return switch (label.toLowerCase()) {
            case "ban" -> handleBan(sender, args);
            case "unban" -> handleUnban(sender, args);
            case "mute" -> handleMute(sender, args);
            case "unmute" -> handleUnmute(sender, args);
            case "kick" -> handleKick(sender, args);
            default -> false;
        };
    }

    private boolean handleBan(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.ban")) {
            sender.sendMessage(msg("general.no-permission", "&cNo permission."));
            return true;
        }

        boolean silent = args[0].equalsIgnoreCase("-s");
        int offset = silent ? 1 : 0;

        if (args.length <= offset) {
            sender.sendMessage(msg("punishment.ban.usage",
                    "&cUsage: /ban [-s] <player> [time] [reason...]"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[offset]);
        if (target == null) {
            sender.sendMessage(msg("general.player-not-found", "&cPlayer not found."));
            return true;
        }

        String source = sender instanceof Player ? sender.getName() : "Console";
        long duration = -1;
        int reasonStart = offset + 1;

        if (args.length > offset + 1) {
            long parsed = PunishmentManager.parseDuration(args[offset + 1]);
            if (parsed != -1) {
                duration = parsed;
                reasonStart = offset + 2;
            }
        }

        String reason = "";
        if (args.length > reasonStart) {
            reason = String.join(" ", Arrays.copyOfRange(args, reasonStart, args.length));
        }
        if (reason.isEmpty()) {
            reason = plugin.getConfigManager().getMessage("punishment.default-reason",
                    "No reason specified");
        }

        manager.ban(target, source, reason, duration, silent);
        return true;
    }

    private boolean handleUnban(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.unban")) {
            sender.sendMessage(msg("general.no-permission", "&cNo permission."));
            return true;
        }

        String targetName = args[0];
        if (!manager.isBanned(targetName)) {
            sender.sendMessage(msg("punishment.unban.not-banned",
                    "&cThat player is not banned."));
            return true;
        }

        manager.unban(targetName);
        String msgStr = plugin.getConfigManager().getMessage("punishment.unban.success",
                "&a{player} &ahas been unbanned.");
        msgStr = msgStr.replace("{player}", targetName);
        sender.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(msgStr)));
        return true;
    }

    private boolean handleMute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.mute")) {
            sender.sendMessage(msg("general.no-permission", "&cNo permission."));
            return true;
        }

        boolean silent = args[0].equalsIgnoreCase("-s");
        int offset = silent ? 1 : 0;

        if (args.length <= offset) {
            sender.sendMessage(msg("punishment.mute.usage",
                    "&cUsage: /mute [-s] <player> [time] [reason...]"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[offset]);
        if (target == null) {
            sender.sendMessage(msg("general.player-not-found", "&cPlayer not found."));
            return true;
        }

        String source = sender instanceof Player ? sender.getName() : "Console";
        long duration = -1;
        int reasonStart = offset + 1;

        if (args.length > offset + 1) {
            long parsed = PunishmentManager.parseDuration(args[offset + 1]);
            if (parsed != -1) {
                duration = parsed;
                reasonStart = offset + 2;
            }
        }

        String reason = "";
        if (args.length > reasonStart) {
            reason = String.join(" ", Arrays.copyOfRange(args, reasonStart, args.length));
        }
        if (reason.isEmpty()) {
            reason = plugin.getConfigManager().getMessage("punishment.default-reason",
                    "No reason specified");
        }

        manager.mute(target, source, reason, duration);
        manager.broadcastMute(target, source, reason, duration, silent);
        return true;
    }

    private boolean handleUnmute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.unmute")) {
            sender.sendMessage(msg("general.no-permission", "&cNo permission."));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(msg("general.player-not-found", "&cPlayer not found."));
            return true;
        }

        if (!manager.isMuted(target.getUniqueId())) {
            sender.sendMessage(msg("punishment.unmute.not-muted",
                    "&cThat player is not muted."));
            return true;
        }

        manager.unmute(target.getUniqueId());
        String msgStr = plugin.getConfigManager().getMessage("punishment.unmute.success",
                "&a{player} &ahas been unmuted.");
        msgStr = msgStr.replace("{player}", target.getName());
        sender.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(msgStr)));
        return true;
    }

    private boolean handleKick(CommandSender sender, String[] args) {
        if (!sender.hasPermission("iraquecore.kick")) {
            sender.sendMessage(msg("general.no-permission", "&cNo permission."));
            return true;
        }

        boolean silent = args[0].equalsIgnoreCase("-s");
        int offset = silent ? 1 : 0;

        if (args.length <= offset) {
            sender.sendMessage(msg("punishment.kick.usage",
                    "&cUsage: /kick [-s] <player> [reason...]"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[offset]);
        if (target == null) {
            sender.sendMessage(msg("general.player-not-found", "&cPlayer not found."));
            return true;
        }

        String source = sender instanceof Player ? sender.getName() : "Console";
        String reason = "";
        if (args.length > offset + 1) {
            reason = String.join(" ", Arrays.copyOfRange(args, offset + 1, args.length));
        }
        if (reason.isEmpty()) {
            reason = plugin.getConfigManager().getMessage("punishment.default-reason",
                    "No reason specified");
        }

        manager.kick(target, source, reason);
        manager.broadcastKick(target, source, reason, silent);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("-s");
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).toList());
            return completions;
        }

        if (args.length == 2 && (label.equals("ban") || label.equals("mute"))) {
            return List.of("1h", "6h", "12h", "1d", "7d", "30d", "perm");
        }

        return List.of();
    }
}
