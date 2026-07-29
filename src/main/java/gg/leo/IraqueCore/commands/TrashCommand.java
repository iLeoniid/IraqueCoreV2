package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TrashCommand implements TabExecutor, Listener {

    private final IraqueCore plugin;
    private final Set<UUID> trashViewers = new HashSet<>();

    public TrashCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("iraquecore.trash")) {
            sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.no-permission"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.player-only"));
            return true;
        }

        Inventory inv = Bukkit.createInventory(player, 54,
                plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().translate(
                                plugin.getConfigManager().getMessage("trash.title", "&cTrash"))));
        trashViewers.add(player.getUniqueId());
        player.openInventory(inv);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        return List.of();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player
                && trashViewers.remove(player.getUniqueId())) {
            event.getInventory().clear();
        }
    }
}
