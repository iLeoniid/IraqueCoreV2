package gg.leo.IraqueCore.leaderboard;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.playtime.PlaytimeManager;
import gg.leo.IraqueCore.utils.ItemBuilder;
import gg.leo.IraqueCore.utils.MenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager implements Listener {

    private final IraqueCore plugin;
    private static final int ITEMS_PER_PAGE = 45;

    public LeaderboardManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        MenuHolder holder = new MenuHolder("leaderboard_main");
        Inventory inv = Bukkit.createInventory(holder, 27, "§8Leaderboards");
        holder.setInventory(inv);

        ItemStack bg = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 27; i++) inv.setItem(i, bg);

        inv.setItem(11, ItemBuilder.of(Material.DIAMOND_PICKAXE)
                .name("&bBlocks Broken")
                .lore("&7Click to view top players").build());
        inv.setItem(13, ItemBuilder.of(Material.BRICK)
                .name("&aBlocks Placed")
                .lore("&7Click to view top players").build());
        inv.setItem(15, ItemBuilder.of(Material.SKELETON_SKULL)
                .name("&cDeaths")
                .lore("&7Click to view top players").build());
        inv.setItem(22, ItemBuilder.of(Material.CLOCK)
                .name("&ePlaytime")
                .lore("&7Click to view top players").build());

        player.openInventory(inv);
    }

    public void openCategory(Player player, String category, int page) {
        boolean isPlaytime = category.equals("playtime");

        List<Map.Entry<UUID, ? extends Number>> sorted;
        if (isPlaytime) {
            var ptm = plugin.getPlaytimeManager();
            sorted = (ptm != null ? ptm.getPlaytimeMap() : Collections.<UUID, Long>emptyMap())
                    .entrySet().stream()
                    .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                    .collect(Collectors.toList());
        } else {
            Map<UUID, Integer> stats = switch (category) {
                case "blocks_broken" -> plugin.getScoreboardManager().getBlocksBroken();
                case "blocks_placed" -> plugin.getScoreboardManager().getBlocksPlaced();
                case "deaths" -> plugin.getScoreboardManager().getDeaths();
                default -> Collections.emptyMap();
            };
            sorted = stats.entrySet().stream()
                    .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                    .collect(Collectors.toList());
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) sorted.size() / ITEMS_PER_PAGE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        String title = switch (category) {
            case "blocks_broken" -> "§8Blocks Broken";
            case "blocks_placed" -> "§8Blocks Placed";
            case "deaths" -> "§8Deaths";
            case "playtime" -> "§8Playtime";
            default -> "§8Leaderboard";
        };

        MenuHolder holder = new MenuHolder("leaderboard_category", category, page);
        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inv);

        int start = page * ITEMS_PER_PAGE;
        int slot = 0;
        for (int i = start; i < sorted.size() && slot < ITEMS_PER_PAGE; i++, slot++) {
            Map.Entry<UUID, ? extends Number> entry = sorted.get(i);
            String playerName = getPlayerName(entry.getKey());
            int position = i + 1;

            String valueStr = isPlaytime
                    ? PlaytimeManager.formatTime(entry.getValue().longValue())
                    : String.valueOf(entry.getValue().intValue());

            ItemStack head = ItemBuilder.of(Material.PLAYER_HEAD, 1)
                    .skullOwner(playerName)
                    .name("&e#" + position + " &6" + playerName)
                    .lore("&7Value: &f" + valueStr)
                    .build();
            inv.setItem(slot, head);
        }

        if (page > 0)
            inv.setItem(48, ItemBuilder.of(Material.ARROW).name("&e← Previous Page").build());
        inv.setItem(49, ItemBuilder.of(Material.BARRIER).name("&c← Back to Menu").build());
        if (page < totalPages - 1)
            inv.setItem(50, ItemBuilder.of(Material.ARROW).name("&eNext Page →").build());

        player.openInventory(inv);
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
                    case 11 -> openCategory(player, "blocks_broken", 0);
                    case 13 -> openCategory(player, "blocks_placed", 0);
                    case 15 -> openCategory(player, "deaths", 0);
                    case 22 -> openCategory(player, "playtime", 0);
                }
            }
            case "leaderboard_category" -> {
                switch (event.getSlot()) {
                    case 48 -> openCategory(player, holder.getCategory(), holder.getPage() - 1);
                    case 49 -> openMainMenu(player);
                    case 50 -> openCategory(player, holder.getCategory(), holder.getPage() + 1);
                }
            }
        }
    }

    private String getPlayerName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) return online.getName();
        org.bukkit.OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
        return off.getName() != null ? off.getName() : "Unknown";
    }
}
