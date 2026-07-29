package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RemoveArmorStandCommand implements TabExecutor {

    private final IraqueCore plugin;

    public RemoveArmorStandCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessageComponent("general.player-only"));
            return true;
        }
        if (!player.hasPermission("iraquecore.removearmorstand")) {
            player.sendMessage(plugin.getConfigManager().getMessageComponent("general.no-permission"));
            return true;
        }

        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                5.0,
                0.2,
                e -> e instanceof ArmorStand
        );

        if (result == null || !(result.getHitEntity() instanceof ArmorStand stand)) {
            player.sendMessage(plugin.getConfigManager().deserialize(
                    "<red>No hay un armor stand frente a ti.</red>"));
            return true;
        }

        stand.remove();
        player.sendMessage(plugin.getConfigManager().deserialize(
                "<green>Armor stand eliminado.</green>"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
