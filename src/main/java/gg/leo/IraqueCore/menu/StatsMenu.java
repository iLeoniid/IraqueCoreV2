package gg.leo.IraqueCore.menu;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.playtime.PlaytimeManager;
import gg.leo.IraqueCore.utils.ItemBuilder;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class StatsMenu {

    private final IraqueCore plugin;

    public StatsMenu(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer, UUID targetId, String targetName) {
        int blocksBroken = plugin.getScoreboardManager().getBlocksBroken().getOrDefault(targetId, 0);
        int blocksPlaced = plugin.getScoreboardManager().getBlocksPlaced().getOrDefault(targetId, 0);
        int deaths = plugin.getScoreboardManager().getDeaths().getOrDefault(targetId, 0);
        long playtime = plugin.getPlaytimeManager() != null ? plugin.getPlaytimeManager().getPlaytime(targetId) : 0;
        String firstJoined = getFirstJoined(targetId);

        String finalRankPrefix = plugin.getRankManager().getPlayerRank(targetId)
                .map(rank -> org.bukkit.ChatColor.translateAlternateColorCodes('&', rank.prefix() + rank.name()))
                .orElse("");

        long fPlaytime = playtime;
        int fBlocksBroken = blocksBroken;
        int fBlocksPlaced = blocksPlaced;
        int fDeaths = deaths;
        String fFirstJoined = firstJoined;

        new Menu(viewer) {
            {
                staticSize = 27;
                placeholder = true;
            }

            @Override
            public Map<Integer, Button> getButtons(Player player) {
                Map<Integer, Button> buttons = new HashMap<>();

                buttons.put(4, new Button() {
                    @Override
                    public Material getMaterial(Player player) { return Material.PLAYER_HEAD; }
                    @Override
                    public List<String> getDescription(Player player) {
                        List<String> lore = new ArrayList<>();
                        if (!finalRankPrefix.isEmpty()) {
                            lore.add("&7Rank: " + finalRankPrefix);
                        }
                        lore.add("&7Playtime: &e" + PlaytimeManager.formatTime(fPlaytime));
                        lore.add("&7First joined: &e" + fFirstJoined);
                        return lore;
                    }
                    @Override
                    public String getDisplayName(Player player) { return "&6" + targetName; }
                    @Override
                    public int getData(Player player) { return 0; }
                    @Override
                    public void onClick(Player player, int slot, ClickType type) {}
                    @Override
                    public ItemStack getButtonItem(Player player) {
                        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta meta = (SkullMeta) head.getItemMeta();
                        meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', "&6" + targetName));
                        OfflinePlayer off = Bukkit.getOfflinePlayer(targetId);
                        meta.setOwningPlayer(off);
                        List<String> lore = new ArrayList<>();
                        if (!finalRankPrefix.isEmpty()) {
                            lore.add(org.bukkit.ChatColor.GRAY + "Rank: " + finalRankPrefix);
                        }
                        lore.add(org.bukkit.ChatColor.GRAY + "Playtime: " + org.bukkit.ChatColor.YELLOW + PlaytimeManager.formatTime(fPlaytime));
                        lore.add(org.bukkit.ChatColor.GRAY + "First joined: " + org.bukkit.ChatColor.YELLOW + fFirstJoined);
                        meta.setLore(lore);
                        head.setItemMeta(meta);
                        return head;
                    }
                });

                buttons.put(10, new Button() {
                    @Override
                    public Material getMaterial(Player player) { return Material.DIAMOND_PICKAXE; }
                    @Override
                    public List<String> getDescription(Player player) { return List.of("&7" + fBlocksBroken); }
                    @Override
                    public String getDisplayName(Player player) { return "&bBlocks Broken"; }
                    @Override
                    public int getData(Player player) { return 0; }
                    @Override
                    public void onClick(Player player, int slot, ClickType type) {}
                });
                buttons.put(12, new Button() {
                    @Override
                    public Material getMaterial(Player player) { return Material.BRICK; }
                    @Override
                    public List<String> getDescription(Player player) { return List.of("&7" + fBlocksPlaced); }
                    @Override
                    public String getDisplayName(Player player) { return "&aBlocks Placed"; }
                    @Override
                    public int getData(Player player) { return 0; }
                    @Override
                    public void onClick(Player player, int slot, ClickType type) {}
                });
                buttons.put(14, new Button() {
                    @Override
                    public Material getMaterial(Player player) { return Material.SKELETON_SKULL; }
                    @Override
                    public List<String> getDescription(Player player) { return List.of("&7" + fDeaths); }
                    @Override
                    public String getDisplayName(Player player) { return "&cDeaths"; }
                    @Override
                    public int getData(Player player) { return 0; }
                    @Override
                    public void onClick(Player player, int slot, ClickType type) {}
                });
                buttons.put(16, new Button() {
                    @Override
                    public Material getMaterial(Player player) { return Material.CLOCK; }
                    @Override
                    public List<String> getDescription(Player player) { return List.of("&7" + PlaytimeManager.formatTime(fPlaytime)); }
                    @Override
                    public String getDisplayName(Player player) { return "&ePlaytime"; }
                    @Override
                    public int getData(Player player) { return 0; }
                    @Override
                    public void onClick(Player player, int slot, ClickType type) {}
                });

                return buttons;
            }

            @Override
            public String getTitle(Player player) {
                return "\u00a78Stats \u00a77- \u00a7f" + targetName;
            }
        }.openMenu();
    }

    private String getFirstJoined(UUID uuid) {
        OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
        long firstPlayed = off.getFirstPlayed();
        if (firstPlayed == 0) return "Unknown";
        return new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date(firstPlayed));
    }
}
