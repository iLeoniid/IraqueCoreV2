package gg.leo.IraqueCore.grant;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.rank.Rank;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import gg.leo.IraqueCore.utils.menu.type.BorderedPaginatedMenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class GrantListener {

    private final IraqueCore plugin;
    private final Map<UUID, GrantSession> sessions = new HashMap<>();

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

        new BorderedPaginatedMenu(granter) {
            @Override
            public Map<Integer, Button> getPagesButtons(Player p) {
                Map<Integer, Button> buttons = new LinkedHashMap<>();
                int index = 0;
                for (Rank rank : ranks) {
                    String fRankName = rank.name();
                    buttons.put(index, rankButton(rank, () -> {
                        session.selectedRank = fRankName;
                        openDurationSelect(granter);
                    }));
                    index++;
                }
                return buttons;
            }

            @Override
            public String getTitle(Player p) {
                return "Select Rank - " + session.targetName;
            }

            @Override
            public Map<Integer, Button> getHeaderItems(Player p) {
                Map<Integer, Button> headers = super.getHeaderItems(p);
                headers.put(40, new Button() {
                    @Override public Material getMaterial(Player p) { return Material.BARRIER; }
                    @Override public List<String> getDescription(Player p) { return List.of("&7Cancelar el grant"); }
                    @Override public String getDisplayName(Player p) { return "&c\u2716 Cancel"; }
                    @Override public int getData(Player p) { return 0; }
                    @Override public void onClick(Player p, int slot, ClickType type) {
                        sessions.remove(granter.getUniqueId());
                        p.closeInventory();
                    }
                });
                return headers;
            }
        }.updateMenu();
    }

    public void openDurationSelect(Player granter) {
        GrantSession session = sessions.get(granter.getUniqueId());
        if (session == null) return;

        new Menu(granter) {
            {
                staticSize = 27;
                placeholder = true;
            }

            @Override
            public Map<Integer, Button> getButtons(Player p) {
                Map<Integer, Button> buttons = new HashMap<>();

                buttons.put(4, backButton(() -> openRankSelect(granter)));

                buttons.put(10, durationButton(Material.LIME_WOOL, "&a1 Hour", "1h", session, () -> openReasonSelect(granter)));
                buttons.put(11, durationButton(Material.LIME_WOOL, "&a1 Day", "1d", session, () -> openReasonSelect(granter)));
                buttons.put(12, durationButton(Material.YELLOW_WOOL, "&e1 Week", "1w", session, () -> openReasonSelect(granter)));
                buttons.put(13, durationButton(Material.ORANGE_WOOL, "&61 Month", "30d", session, () -> openReasonSelect(granter)));
                buttons.put(14, durationButton(Material.RED_WOOL, "&c1 Year", "1y", session, () -> openReasonSelect(granter)));
                buttons.put(15, durationButton(Material.PURPLE_WOOL, "&dPermanent", "permanent", session, () -> openReasonSelect(granter)));
                buttons.put(16, new Button() {
                    @Override public Material getMaterial(Player p) { return Material.WHITE_WOOL; }
                    @Override public List<String> getDescription(Player p) {
                        return List.of("&7Duration: &fcustom", "", "&e\u25B8 Click to select");
                    }
                    @Override public String getDisplayName(Player p) { return "&7Custom"; }
                    @Override public int getData(Player p) { return 8; }
                    @Override public void onClick(Player p, int slot, ClickType type) {
                        p.sendMessage(plugin.getConfigManager().deserialize(
                                plugin.getConfigManager().translate("&eUse &c/grant " + session.targetName + " " + session.selectedRank + " <duration> <reason> &efor custom values.")));
                        sessions.remove(granter.getUniqueId());
                        p.closeInventory();
                    }
                });

                return buttons;
            }

            @Override
            public String getTitle(Player p) {
                return "Select Duration - " + session.targetName;
            }
        }.openMenu();
    }

    public void openReasonSelect(Player granter) {
        GrantSession session = sessions.get(granter.getUniqueId());
        if (session == null) return;

        new Menu(granter) {
            {
                staticSize = 27;
                placeholder = true;
            }

            @Override
            public Map<Integer, Button> getButtons(Player p) {
                Map<Integer, Button> buttons = new HashMap<>();

                buttons.put(4, backButton(() -> openDurationSelect(granter)));

                buttons.put(10, reasonButton(Material.PAPER, "Promotion", () -> completeGrant(granter, "Promotion")));
                buttons.put(11, reasonButton(Material.DIAMOND, "Won Event", () -> completeGrant(granter, "Won Event")));
                buttons.put(12, reasonButton(Material.EMERALD, "Purchased", () -> completeGrant(granter, "Purchased")));
                buttons.put(13, reasonButton(Material.NETHER_STAR, "Staff Grant", () -> completeGrant(granter, "Staff Grant")));
                buttons.put(14, reasonButton(Material.BOOK, "Application", () -> completeGrant(granter, "Application")));
                buttons.put(16, new Button() {
                    @Override public Material getMaterial(Player p) { return Material.COMMAND_BLOCK; }
                    @Override public List<String> getDescription(Player p) {
                        return List.of("&7Reason: &fCustom", "", "&e\u25B8 Click to select");
                    }
                    @Override public String getDisplayName(Player p) { return "&7Custom"; }
                    @Override public int getData(Player p) { return 0; }
                    @Override public void onClick(Player p, int slot, ClickType type) {
                        p.closeInventory();
                        p.sendMessage(plugin.getConfigManager().deserialize(
                                plugin.getConfigManager().translate("&eType your reason in chat, or type &ccancel &eto cancel.")));
                        session.waitingForReason = true;
                    }
                });

                return buttons;
            }

            @Override
            public String getTitle(Player p) {
                return "Select Reason - " + session.targetName;
            }
        }.openMenu();
    }

    private Button rankButton(Rank rank, Runnable onSelect) {
        return new Button() {
            @Override public Material getMaterial(Player p) { return Material.WHITE_WOOL; }
            @Override public List<String> getDescription(Player p) {
                return List.of(
                        "&7Prefix: " + org.bukkit.ChatColor.translateAlternateColorCodes('&', rank.prefix()),
                        "&7Weight: &f" + rank.weight(),
                        "",
                        "&e\u25B8 Click to grant"
                );
            }
            @Override public String getDisplayName(Player p) { return rank.color() + rank.name(); }
            @Override public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                onSelect.run();
            }
            @Override
            public ItemStack getButtonItem(Player p) {
                ItemStack item = new ItemStack(Material.WHITE_WOOL, 1);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', rank.color() + rank.name()));
                List<String> lore = new ArrayList<>();
                lore.add(org.bukkit.ChatColor.GRAY + "Prefix: " + org.bukkit.ChatColor.translateAlternateColorCodes('&', rank.prefix()));
                lore.add(org.bukkit.ChatColor.GRAY + "Weight: " + org.bukkit.ChatColor.WHITE + rank.weight());
                lore.add("");
                lore.add(org.bukkit.ChatColor.GREEN + "\u25B8 Click to grant");
                meta.setLore(lore);
                item.setItemMeta(meta);
                return item;
            }
        };
    }

    private Button durationButton(Material mat, String display, String duration, GrantSession session, Runnable onSelect) {
        return new Button() {
            @Override public Material getMaterial(Player p) { return mat; }
            @Override public List<String> getDescription(Player p) {
                return List.of("&7Duration: &f" + duration, "", "&e\u25B8 Click to select");
            }
            @Override public String getDisplayName(Player p) { return display; }
            @Override public int getData(Player p) {
                return switch (duration) {
                    case "1h" -> 5; case "1d" -> 5; case "1w" -> 4;
                    case "30d" -> 1; case "1y" -> 14; case "permanent" -> 10;
                    default -> 0;
                };
            }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                long dur;
                if (duration.equalsIgnoreCase("permanent")) {
                    dur = Long.MAX_VALUE;
                } else {
                    dur = parseDuration(duration);
                }
                session.duration = dur;
                onSelect.run();
            }
        };
    }

    private Button reasonButton(Material mat, String reason, Runnable onSelect) {
        return new Button() {
            @Override public Material getMaterial(Player p) { return mat; }
            @Override public List<String> getDescription(Player p) {
                return List.of("&7Reason: &f" + reason, "", "&e\u25B8 Click to select");
            }
            @Override public String getDisplayName(Player p) {
                return switch (reason) {
                    case "Promotion" -> "&5Promotion";
                    case "Won Event" -> "&dWon Event";
                    case "Purchased" -> "&9Purchased";
                    case "Staff Grant" -> "&3Staff Grant";
                    case "Application" -> "&bApplication";
                    default -> reason;
                };
            }
            @Override public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                onSelect.run();
            }
        };
    }

    private Button backButton(Runnable onBack) {
        return new Button() {
            @Override public Material getMaterial(Player p) { return Material.BARRIER; }
            @Override public List<String> getDescription(Player p) { return List.of("&7Go back to previous menu"); }
            @Override public String getDisplayName(Player p) { return "&c\u2190 Back"; }
            @Override public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                onBack.run();
            }
        };
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

    public void cleanupPlayer(Player player) {
        sessions.remove(player.getUniqueId());
    }

    private static class GrantSession {
        final UUID targetId;
        final String targetName;
        String selectedRank;
        long duration;
        boolean waitingForReason;

        GrantSession(UUID targetId, String targetName) {
            this.targetId = targetId;
            this.targetName = targetName;
        }
    }
}
