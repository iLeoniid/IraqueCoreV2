package gg.leo.IraqueCore.home;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class HomeCommand implements TabExecutor {

    private final IraqueCore plugin;
    private final HomeManager homeManager;

    public HomeCommand(IraqueCore plugin, HomeManager homeManager) {
        this.plugin = plugin;
        this.homeManager = homeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("general.player-only", "&cOnly players can use this command."))));
            return true;
        }

        if (!player.hasPermission("iraquecore.home")) {
            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("general.no-permission", "&cYou don't have permission."))));
            return true;
        }

        switch (label.toLowerCase()) {
            case "sethome" -> handleSetHome(player);
            case "home", "home teleport" -> handleHome(player, args);
            case "delhome", "deletehome" -> handleDeleteHome(player);
        }

        return true;
    }

    private void handleSetHome(Player player) {
        homeManager.setHome(player);
        player.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("home.set",
                            "&aHome set successfully!"))));
    }

    private void handleHome(Player player, String[] args) {
        if (!homeManager.hasHome(player)) {
            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("home.not-set",
                                "&cYou don't have a home set. Use &e/sethome &cto set one."))));
            return;
        }

        Location home = homeManager.getHome(player);
        if (home == null) {
            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("home.invalid",
                                "&cYour home location is invalid. Use &e/sethome &cto set a new one."))));
            return;
        }

        player.teleport(home);
        player.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("home.teleport",
                            "&aTeleported to your home!"))));
    }

    private void handleDeleteHome(Player player) {
        if (!homeManager.hasHome(player)) {
            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("home.not-set",
                                "&cYou don't have a home set."))));
            return;
        }

        homeManager.deleteHome(player);
        player.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("home.deleted",
                            "&cHome deleted."))));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
