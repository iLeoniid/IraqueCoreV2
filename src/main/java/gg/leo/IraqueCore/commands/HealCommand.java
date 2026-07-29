package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class HealCommand implements TabExecutor {

    private final IraqueCore plugin;

    public HealCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("iraquecore.heal")) {
            sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.no-permission"));
            return true;
        }

        Player target;
        if (args.length > 0) {
            if (!sender.hasPermission("iraquecore.heal.other")) {
                sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.no-permission"));
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.player-not-found"));
                return true;
            }
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.player-only"));
                return true;
            }
            target = player;
        }

        target.setHealth(target.getAttribute(Attribute.MAX_HEALTH).getDefaultValue());
        target.setFoodLevel(20);
        target.setSaturation(10f);
        target.getActivePotionEffects().forEach(e -> target.removePotionEffect(e.getType()));
        target.setFireTicks(0);

        target.sendMessage(plugin.getConfigManager().getMessageComponent("heal.target"));
        if (!target.equals(sender)) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("heal.other", "&aHealed {player}.")
                                    .replace("{player}", target.getName()))));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("iraquecore.heal.other")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
