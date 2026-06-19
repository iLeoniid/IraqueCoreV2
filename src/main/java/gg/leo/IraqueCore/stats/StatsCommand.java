package gg.leo.IraqueCore.stats;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.playtime.PlaytimeManager;
import gg.leo.IraqueCore.utils.ItemBuilder;
import gg.leo.IraqueCore.utils.MenuHolder;
import gg.leo.IraqueCore.utils.menuutils.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StatsCommand implements TabExecutor, Listener {

    private final IraqueCore plugin;

    public StatsCommand(IraqueCore plugin) {
        this.plugin = plugin;
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

        openStatsGUI(player, targetId, targetName);
        return true;
    }

    private void openStatsGUI(Player viewer, UUID targetId, String targetName) {
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
        return new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date(firstPlayed));
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
