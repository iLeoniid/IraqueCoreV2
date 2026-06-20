package gg.leo.IraqueCore.menu;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.playtime.PlaytimeManager;
import gg.leo.IraqueCore.utils.ItemBuilder;
import gg.leo.IraqueCore.utils.MenuHolder;
import gg.leo.IraqueCore.utils.menuutils.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StatsMenu {

    private final IraqueCore plugin;

    public StatsMenu(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer, UUID targetId, String targetName) {
        MenuHolder holder = new MenuHolder("stats");
        Inventory inv = Bukkit.createInventory(holder, 27, "\u00a78Stats \u00a77- \u00a7f" + targetName);
        holder.setInventory(inv);

        int blocksBroken = plugin.getScoreboardManager().getBlocksBroken().getOrDefault(targetId, 0);
        int blocksPlaced = plugin.getScoreboardManager().getBlocksPlaced().getOrDefault(targetId, 0);
        int deaths = plugin.getScoreboardManager().getDeaths().getOrDefault(targetId, 0);
        long playtime = plugin.getPlaytimeManager() != null ? plugin.getPlaytimeManager().getPlaytime(targetId) : 0;

        ItemStack head = ItemBuilder.of(Material.PLAYER_HEAD).skullOwner(targetName).name("&6" + targetName).build();
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            plugin.getRankManager().getPlayerRank(targetId).ifPresent(rank -> {
                lore.add("&7Rank: " + rank.prefix() + rank.name());
            });
            lore.add("&7Playtime: &e" + PlaytimeManager.formatTime(playtime));
            lore.add("&7First joined: &e" + getFirstJoined(targetId));
            meta.setLore(lore.stream().map(l -> l.replace("&", "\u00a7")).toList());
            head.setItemMeta(meta);
        }
        inv.setItem(4, head);

        inv.setItem(10, ItemBuilder.of(Material.DIAMOND_PICKAXE)
                .name("&bBlocks Broken")
                .lore("&7" + blocksBroken).build());
        inv.setItem(12, ItemBuilder.of(Material.BRICK)
                .name("&aBlocks Placed")
                .lore("&7" + blocksPlaced).build());
        inv.setItem(14, ItemBuilder.of(Material.SKELETON_SKULL)
                .name("&cDeaths")
                .lore("&7" + deaths).build());
        inv.setItem(16, ItemBuilder.of(Material.CLOCK)
                .name("&ePlaytime")
                .lore("&7" + PlaytimeManager.formatTime(playtime)).build());

        MenuUtils.fillBorders(inv, Material.GRAY_STAINED_GLASS_PANE);
        viewer.openInventory(inv);
    }

    private String getFirstJoined(UUID uuid) {
        OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
        long firstPlayed = off.getFirstPlayed();
        if (firstPlayed == 0) return "Unknown";
        return new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date(firstPlayed));
    }
}
