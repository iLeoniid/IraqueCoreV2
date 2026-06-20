package gg.leo.IraqueCore.menu;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.playtime.PlaytimeManager;
import gg.leo.IraqueCore.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardMenu {

    private static final int ITEMS_PER_PAGE = 45;

    private final IraqueCore plugin;

    public LeaderboardMenu(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void openMain(Player player) {
        BaseMenu menu = new BaseMenu(player, 27, "§8Leaderboards", "leaderboard_main") {
            @Override
            public void build() {
                ItemStack bg = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
                for (int i = 0; i < 27; i++) setItem(i, bg);

                setItem(11, ItemBuilder.of(Material.DIAMOND_PICKAXE)
                        .name("&b&lBloques Rotos")
                        .lore("&7Click para ver el top de jugadores").build());
                setItem(13, ItemBuilder.of(Material.BRICK)
                        .name("&a&lBloques Puestos")
                        .lore("&7Click para ver el top de jugadores").build());
                setItem(15, ItemBuilder.of(Material.SKELETON_SKULL)
                        .name("&c&lMuertes")
                        .lore("&7Click para ver el top de jugadores").build());
                setItem(22, ItemBuilder.of(Material.CLOCK)
                        .name("&e&lPlaytime")
                        .lore("&7Click para ver el top de jugadores").build());
            }

            @Override
            public void handleClick(int slot) {
                switch (slot) {
                    case 11 -> openCategory(player, "blocks_broken", 0);
                    case 13 -> openCategory(player, "blocks_placed", 0);
                    case 15 -> openCategory(player, "deaths", 0);
                    case 22 -> openCategory(player, "playtime", 0);
                }
            }
        };
        menu.open();
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
        int currentPage = Math.max(0, Math.min(page, totalPages - 1));

        String title = switch (category) {
            case "blocks_broken" -> "§8\u2692 Top Bloques Rotos";
            case "blocks_placed" -> "§8\uD83E\uDDF1 Top Bloques Puestos";
            case "deaths" -> "§8\u2620 Top Muertes";
            case "playtime" -> "§8\u23F1 Top Playtime";
            default -> "§8Leaderboard";
        };

        BaseMenu menu = new BaseMenu(player, 54, title, "leaderboard_category", category, currentPage) {
            @Override
            public void build() {
                int start = currentPage * ITEMS_PER_PAGE;
                int slot = 0;
                for (int i = start; i < sorted.size() && slot < ITEMS_PER_PAGE; i++, slot++) {
                    Map.Entry<UUID, ? extends Number> entry = sorted.get(i);
                    String playerName = getPlayerName(entry.getKey());
                    int position = i + 1;

                    String valueStr = isPlaytime
                            ? PlaytimeManager.formatTime(entry.getValue().longValue())
                            : String.valueOf(entry.getValue().intValue());

                    String prefix = switch (position) {
                        case 1 -> "&6";
                        case 2 -> "&7";
                        case 3 -> "&6";
                        default -> "&e";
                    };
                    String trophy = switch (position) {
                        case 1 -> "\uD83E\uDD47 ";
                        case 2 -> "\uD83E\uDD48 ";
                        case 3 -> "\uD83E\uDD49 ";
                        default -> "";
                    };

                    ItemStack head = ItemBuilder.of(Material.PLAYER_HEAD, 1)
                            .skullOwner(playerName)
                            .name(prefix + trophy + "#" + position + " &f" + playerName)
                            .lore("&7Valor: &f" + valueStr)
                            .build();
                    setItem(slot, head);
                }

                fillBorders();

                if (currentPage > 0)
                    setItem(48, ItemBuilder.of(Material.ARROW).name("&e\u2190 Página Anterior").build());
                setItem(49, ItemBuilder.of(Material.BARRIER).name("&c\u2190 Volver al Menú").build());
                if (currentPage < totalPages - 1)
                    setItem(50, ItemBuilder.of(Material.ARROW).name("&eSiguiente Página \u2192").build());
            }

            @Override
            public void handleClick(int slot) {
                switch (slot) {
                    case 48 -> openCategory(player, category, currentPage - 1);
                    case 49 -> openMain(player);
                    case 50 -> openCategory(player, category, currentPage + 1);
                }
            }
        };
        menu.open();
    }

    @SuppressWarnings("deprecation")
    private String getPlayerName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) return online.getName();
        org.bukkit.OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
        return off.getName() != null ? off.getName() : "Unknown";
    }
}
