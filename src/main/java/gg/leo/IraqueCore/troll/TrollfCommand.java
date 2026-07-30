package gg.leo.IraqueCore.troll;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrollfCommand implements TabExecutor {

    private final TrollManager manager;

    public TrollfCommand(TrollManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /trollf <player> <troll>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        String effectId = args[1].toLowerCase();
        TrollEffect effect = manager.getEffect(effectId);
        if (effect == null) {
            sender.sendMessage("§cTroll effect '" + effectId + "' not found.");
            return true;
        }

        if (sender instanceof Player player) {
            if (!player.hasPermission("troll.use") && !player.hasPermission(effect.getPermission())) {
                player.sendMessage("§cYou don't have permission.");
                return true;
            }
            manager.applyEffect(target, effectId, player);
        } else {
            manager.applyEffect(target, effectId, null);
            sender.sendMessage("§aApplied " + effect.getName() + " on " + target.getName());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                       @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return manager.getEffects().keySet().stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
