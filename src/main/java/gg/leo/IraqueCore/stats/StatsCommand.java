package gg.leo.IraqueCore.stats;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.menu.StatsMenu;
import gg.leo.IraqueCore.utils.MenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class StatsCommand implements TabExecutor, Listener {

    private final IraqueCore plugin;
    private final StatsMenu menu;

    public StatsCommand(IraqueCore plugin) {
        this.plugin = plugin;
        this.menu = new StatsMenu(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("general.player-only", "&cOnly players can use this command."))));
            return true;
        }

        UUID targetId;
        String targetName;

        if (args.length == 0) {
            targetId = player.getUniqueId();
            targetName = player.getName();
        } else {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().translate(
                                plugin.getConfigManager().getMessage("general.player-not-found", "&cPlayer not found."))));
                return true;
            }
            if (!player.hasPermission("iraquecore.stats.other") && !target.equals(player)) {
                player.sendMessage(plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().translate(
                                plugin.getConfigManager().getMessage("general.no-permission", "&cYou don't have permission."))));
                return true;
            }
            targetId = target.getUniqueId();
            targetName = target.getName();
        }

        menu.open(player, targetId, targetName);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) return;
        if (!"stats".equals(holder.getType())) return;
        event.setCancelled(true);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("iraquecore.stats.other")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
