package gg.leo.IraqueCore.grant;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.rank.Rank;
import gg.leo.IraqueCore.utils.MenuHolder;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class GrantListener implements Listener {

    private final IraqueCore plugin;
    private final Map<UUID, GrantSession> sessions = new HashMap<>();

    private static final int ITEMS_PER_PAGE = 21;
    private static final List<Integer> SLOT_POSITIONS = List.of(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    );

    public GrantListener(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void startGrant(Player granter, Player target) {
        sessions.put(granter.getUniqueId(), new GrantSession(target.getUniqueId(), target.getName()));
        openRankSelect(granter);
    }

    public void openRankSelect(Player granter) {
        GrantSession session = sessions.get(granter.getUniqueId());
        if (session == null) return;

        List<Rank> ranks = plugin.getRankManager().getRanks().values().stream()
                .sorted(Comparator.comparingInt(Rank::weight).reversed())
                .collect(Collectors.toList());

        int totalPages = Math.max(1, (int) Math.ceil((double) ranks.size() / ITEMS_PER_PAGE));
        session.page = Math.min(session.page, totalPages - 1);

        int size = 36;
        Inventory inv = Bukkit.createInventory(
                new MenuHolder("grant_rank", null, session.page), size,
                "Select Rank - " + session.targetName);

        int start = session.page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, ranks.size());

        for (int i = start; i < end; i++) {
            Rank rank = ranks.get(i);
            int slot = SLOT_POSITIONS.get(i - start);
            ItemStack item = new ItemStack(Material.WHITE_WOOL);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', rank.color() + rank.name()));
            List<String> lore = new ArrayList<>();
            lore.add(org.bukkit.ChatColor.GRAY + "Prefix: " + org.bukkit.ChatColor.translateAlternateColorCodes('&', rank.prefix()));
            lore.add(org.bukkit.ChatColor.GRAY + "Weight: " + org.bukkit.ChatColor.WHITE + rank.weight());
            lore.add("");
            lore.add(org.bukkit.ChatColor.GREEN + "\u25B8 Click to grant");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);
        }

        addNavButtons(inv, granter, session, totalPages);

        granter.openInventory(inv);
    }

    public void openDurationSelect(Player granter) {
        GrantSession session = sessions.get(granter.getUniqueId());
        if (session == null) return;

        int size = 27;
        Inventory inv = Bukkit.createInventory(
                new MenuHolder("grant_duration", session.selectedRank, 0), size,
                "Select Duration - " + session.targetName);

        inv.setItem(4, createBackItem());

        // Duration presets
        inv.setItem(10, createDurationItem(Material.LIME_WOOL, 5, "&a1 Hour", "1h"));
        inv.setItem(11, createDurationItem(Material.LIME_WOOL, 5, "&a1 Day", "1d"));
        inv.setItem(12, createDurationItem(Material.YELLOW_WOOL, 4, "&e1 Week", "1w"));
        inv.setItem(13, createDurationItem(Material.ORANGE_WOOL, 1, "&61 Month", "30d"));
        inv.setItem(14, createDurationItem(Material.RED_WOOL, 14, "&c1 Year", "1y"));
        inv.setItem(15, createDurationItem(Material.PURPLE_WOOL, 10, "&dPermanent", "permanent"));
        inv.setItem(16, createDurationItem(Material.WHITE_WOOL, 8, "&7Custom", "custom"));

        granter.openInventory(inv);
    }

    private ItemStack createDurationItem(Material mat, int data, String display, String duration) {
        ItemStack item = new ItemStack(mat, 1, (short) data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', display));
        meta.setLore(List.of(
                org.bukkit.ChatColor.GRAY + "Duration: " + org.bukkit.ChatColor.WHITE + duration,
                "",
                org.bukkit.ChatColor.GREEN + "\u25B8 Click to select"
        ));
        item.setItemMeta(meta);
        return item;
    }

    public void openReasonSelect(Player granter) {
        GrantSession session = sessions.get(granter.getUniqueId());
        if (session == null) return;

        int size = 27;
        Inventory inv = Bukkit.createInventory(
                new MenuHolder("grant_reason", null, 0), size,
                "Select Reason - " + session.targetName);

        inv.setItem(4, createBackItem());

        // Reason presets
        inv.setItem(10, createReasonItem(Material.PAPER, "Promotion", "&5Promotion"));
        inv.setItem(11, createReasonItem(Material.DIAMOND, "Won Event", "&dWon Event"));
        inv.setItem(12, createReasonItem(Material.EMERALD, "Purchased", "&9Purchased"));
        inv.setItem(13, createReasonItem(Material.NETHER_STAR, "Staff Grant", "&3Staff Grant"));
        inv.setItem(14, createReasonItem(Material.BOOK, "Application", "&bApplication"));
        inv.setItem(16, createReasonItem(Material.COMMAND_BLOCK, "Custom", "&7Custom"));

        granter.openInventory(inv);
    }

    private ItemStack createReasonItem(Material mat, String reason, String display) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', display));
        meta.setLore(List.of(
                org.bukkit.ChatColor.GRAY + "Reason: " + org.bukkit.ChatColor.WHITE + reason,
                "",
                org.bukkit.ChatColor.GREEN + "\u25B8 Click to select"
        ));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        event.setCancelled(true);

        String type = holder.getType();
        switch (type) {
            case "grant_rank" -> handleRankClick(player, event);
            case "grant_duration" -> handleDurationClick(player, event);
            case "grant_reason" -> handleReasonClick(player, event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) return;
    }

    private void handleRankClick(Player player, InventoryClickEvent event) {
        GrantSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        String displayName = item.getItemMeta().getDisplayName();
        String stripped = org.bukkit.ChatColor.stripColor(displayName);

        // Navigation buttons
        if (item.getType() == Material.ARROW) {
            if (displayName.contains("Previous")) {
                session.page = Math.max(0, session.page - 1);
                openRankSelect(player);
            } else if (displayName.contains("Next")) {
                List<Rank> ranks = new ArrayList<>(plugin.getRankManager().getRanks().values());
                int totalPages = Math.max(1, (int) Math.ceil((double) ranks.size() / ITEMS_PER_PAGE));
                session.page = Math.min(session.page + 1, totalPages - 1);
                openRankSelect(player);
            }
            return;
        }
        if (item.getType() == Material.BARRIER) {
            sessions.remove(player.getUniqueId());
            player.closeInventory();
            return;
        }

        // Rank selection
        for (Rank rank : plugin.getRankManager().getRanks().values()) {
            if (rank.name().equalsIgnoreCase(stripped)) {
                session.selectedRank = rank.name();
                openDurationSelect(player);
                return;
            }
        }
    }

    private void handleDurationClick(Player player, InventoryClickEvent event) {
        GrantSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        String stripped = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (item.getType() == Material.BARRIER) {
            openRankSelect(player);
            return;
        }

        // Each duration item has a lore line with the duration value
        List<String> lore = item.getItemMeta().getLore();
        if (lore != null && lore.size() >= 1) {
            String durLine = org.bukkit.ChatColor.stripColor(lore.get(0));
            String durValue = durLine.replace("Duration: ", "").trim();

            if (durValue.equalsIgnoreCase("custom")) {
                player.sendMessage(plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().translate("&eUse &c/grant " + session.targetName + " " + session.selectedRank + " <duration> <reason> &efor custom values.")));
                sessions.remove(player.getUniqueId());
                player.closeInventory();
                return;
            }

            long duration;
            if (durValue.equalsIgnoreCase("permanent")) {
                duration = Long.MAX_VALUE;
            } else {
                duration = parseDuration(durValue);
            }

            session.duration = duration;
            openReasonSelect(player);
        }
    }

    private void handleReasonClick(Player player, InventoryClickEvent event) {
        GrantSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        String stripped = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (item.getType() == Material.BARRIER) {
            openDurationSelect(player);
            return;
        }

        List<String> lore = item.getItemMeta().getLore();
        String reason = "No reason specified";
        if (lore != null && lore.size() >= 1) {
            String reasonLine = org.bukkit.ChatColor.stripColor(lore.get(0));
            reason = reasonLine.replace("Reason: ", "").trim();
        }

        if (reason.equalsIgnoreCase("Custom")) {
            player.closeInventory();
            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate("&eType your reason in chat, or type &ccancel &eto cancel.")));
            session.waitingForReason = true;
            return;
        }

        completeGrant(player, reason);
    }

    private void completeGrant(Player granter, String reason) {
        GrantSession session = sessions.get(granter.getUniqueId());
        if (session == null) return;

        Player target = Bukkit.getPlayer(session.targetId);
        if (target == null) {
            granter.sendMessage(plugin.getConfigManager().getMessageComponent("permission.player-not-found"));
            sessions.remove(granter.getUniqueId());
            return;
        }

        plugin.getGrantManager().grant(session.targetId, granter.getUniqueId(),
                session.selectedRank, reason, session.duration);

        String msg = plugin.getConfigManager().translate(
                plugin.getConfigManager().getMessage("grant.success", "&aGranted {rank} to {player} for {reason}")
                        .replace("{player}", target.getName())
                        .replace("{rank}", session.selectedRank)
                        .replace("{reason}", reason));

        granter.sendMessage(plugin.getConfigManager().deserialize(msg));

        String targetMsg = plugin.getConfigManager().translate(
                plugin.getConfigManager().getMessage("grant.received", "&aYou have been granted the rank {rank}")
                        .replace("{rank}", session.selectedRank));

        target.sendMessage(plugin.getConfigManager().deserialize(targetMsg));

        sessions.remove(granter.getUniqueId());
        granter.closeInventory();
    }

    public boolean handleChatInput(Player player, String input) {
        GrantSession session = sessions.get(player.getUniqueId());
        if (session == null || !session.waitingForReason) return false;

        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate("&cGrant cancelled.")));
            sessions.remove(player.getUniqueId());
            return true;
        }

        completeGrant(player, input);
        return true;
    }

    private long parseDuration(String input) {
        if (input.equalsIgnoreCase("permanent")) return Long.MAX_VALUE;
        try {
            char unit = input.charAt(input.length() - 1);
            long amount = Long.parseLong(input.substring(0, input.length() - 1));
            return switch (Character.toLowerCase(unit)) {
                case 's' -> amount * 1000L;
                case 'm' -> amount * 60000L;
                case 'h' -> amount * 3600000L;
                case 'd' -> amount * 86400000L;
                case 'w' -> amount * 604800000L;
                case 'y' -> amount * 31536000000L;
                default -> 0;
            };
        } catch (Exception e) {
            return 0;
        }
    }

    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.RED + "\u2190 Back");
        meta.setLore(List.of(org.bukkit.ChatColor.GRAY + "Go back to previous menu"));
        item.setItemMeta(meta);
        return item;
    }

    private void addNavButtons(Inventory inv, Player player, GrantSession session, int totalPages) {
        // Border
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, border);
        }

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(org.bukkit.ChatColor.RED + "\u2716 Cancel");
        close.setItemMeta(closeMeta);
        inv.setItem(31, close);

        if (session.page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.setDisplayName(org.bukkit.ChatColor.YELLOW + "\u2190 Previous Page");
            prevMeta.setLore(List.of(org.bukkit.ChatColor.GRAY + "Page " + session.page));
            prev.setItemMeta(prevMeta);
            inv.setItem(30, prev);
        }

        if (session.page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName(org.bukkit.ChatColor.YELLOW + "Next Page \u2192");
            nextMeta.setLore(List.of(org.bukkit.ChatColor.GRAY + "Page " + (session.page + 2)));
            next.setItemMeta(nextMeta);
            inv.setItem(32, next);
        }
    }

    public void cleanupPlayer(Player player) {
        sessions.remove(player.getUniqueId());
    }

    private static class GrantSession {
        final UUID targetId;
        final String targetName;
        String selectedRank;
        long duration;
        int page;
        boolean waitingForReason;

        GrantSession(UUID targetId, String targetName) {
            this.targetId = targetId;
            this.targetName = targetName;
        }
    }
}
