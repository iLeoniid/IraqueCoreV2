package gg.leo.IraqueCore.home;

import gg.leo.IraqueCore.IraqueCore;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HomeCommand
implements TabExecutor {
    private final IraqueCore plugin;
    private final HomeManager homeManager;

    public HomeCommand(IraqueCore plugin, HomeManager homeManager) {
        this.plugin = plugin;
        this.homeManager = homeManager;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("general.player-only", "&cOnly players can use this command."))));
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("iraquecore.home")) {
            player.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("general.no-permission", "&cYou don't have permission."))));
            return true;
        }
        switch (label.toLowerCase()) {
            case "sethome": {
                this.handleSetHome(player);
                break;
            }
            case "home": 
            case "home teleport": {
                this.handleHome(player, args);
                break;
            }
            case "delhome": 
            case "deletehome": {
                this.handleDeleteHome(player);
            }
        }
        return true;
    }

    private void handleSetHome(Player player) {
        this.homeManager.setHome(player);
        player.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("home.set", "&aHome set successfully!"))));
    }

    private void handleHome(Player player, String[] args) {
        new HomeMenu(this.plugin, this.homeManager).open(player);
    }

    private void handleDeleteHome(Player player) {
        if (!this.homeManager.hasHome(player)) {
            player.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("home.not-set", "&cYou don't have a home set."))));
            return;
        }
        this.homeManager.deleteHome(player);
        player.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("home.deleted", "&cHome deleted."))));
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
