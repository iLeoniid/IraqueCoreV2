package gg.leo.IraqueCore.leaderboard;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.menu.LeaderboardMenu;
import gg.leo.IraqueCore.utils.MenuHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class LeaderboardManager implements Listener {

    private final LeaderboardMenu menu;

    public LeaderboardManager(IraqueCore plugin) {
        this.menu = new LeaderboardMenu(plugin);
    }

    public void openMainMenu(Player player) {
        menu.openMain(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        switch (holder.getType()) {
            case "leaderboard_main" -> {
                switch (event.getSlot()) {
                    case 11 -> menu.openCategory(player, "blocks_broken", 0);
                    case 13 -> menu.openCategory(player, "blocks_placed", 0);
                    case 15 -> menu.openCategory(player, "deaths", 0);
                    case 22 -> menu.openCategory(player, "playtime", 0);
                }
            }
            case "leaderboard_category" -> {
                switch (event.getSlot()) {
                    case 48 -> menu.openCategory(player, holder.getCategory(), holder.getPage() - 1);
                    case 49 -> menu.openMain(player);
                    case 50 -> menu.openCategory(player, holder.getCategory(), holder.getPage() + 1);
                }
            }
        }
    }
}
