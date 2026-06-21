package gg.leo.IraqueCore.menu;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.playtime.PlaytimeManager;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import gg.leo.IraqueCore.utils.menu.type.BorderedPaginatedMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardMenu {

    private final IraqueCore plugin;

    public LeaderboardMenu(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void openMain(Player player) {
        new Menu(player) {
            {
                staticSize = 27;
                placeholder = true;
            }

            @Override
            public Map<Integer, Button> getButtons(Player p) {
                Map<Integer, Button> buttons = new HashMap<>();
                buttons.put(11, new Button() {
                    @Override public Material getMaterial(Player p) { return Material.DIAMOND_PICKAXE; }
                    @Override public List<String> getDescription(Player p) { return List.of("&7Click para ver el top de jugadores"); }
                    @Override public String getDisplayName(Player p) { return "&b&lBloques Rotos"; }
                    @Override public int getData(Player p) { return 0; }
                    @Override public void onClick(Player p, int slot, ClickType type) { openCategory(p, "blocks_broken", 0); }
                });
                buttons.put(13, new Button() {
                    @Override public Material getMaterial(Player p) { return Material.BRICK; }
                    @Override public List<String> getDescription(Player p) { return List.of("&7Click para ver el top de jugadores"); }
                    @Override public String getDisplayName(Player p) { return "&a&lBloques Puestos"; }
                    @Override public int getData(Player p) { return 0; }
                    @Override public void onClick(Player p, int slot, ClickType type) { openCategory(p, "blocks_placed", 0); }
                });
                buttons.put(15, new Button() {
                    @Override public Material getMaterial(Player p) { return Material.SKELETON_SKULL; }
                    @Override public List<String> getDescription(Player p) { return List.of("&7Click para ver el top de jugadores"); }
                    @Override public String getDisplayName(Player p) { return "&c&lMuertes"; }
                    @Override public int getData(Player p) { return 0; }
                    @Override public void onClick(Player p, int slot, ClickType type) { openCategory(p, "deaths", 0); }
                });
                buttons.put(22, new Button() {
                    @Override public Material getMaterial(Player p) { return Material.CLOCK; }
                    @Override public List<String> getDescription(Player p) { return List.of("&7Click para ver el top de jugadores"); }
                    @Override public String getDisplayName(Player p) { return "&e&lPlaytime"; }
                    @Override public int getData(Player p) { return 0; }
                    @Override public void onClick(Player p, int slot, ClickType type) { openCategory(p, "playtime", 0); }
                });
                return buttons;
            }

            @Override
            public String getTitle(Player p) {
                return "§8Leaderboards";
            }
        }.openMenu();
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

        String title = switch (category) {
            case "blocks_broken" -> "§8\u2692 Top Bloques Rotos";
            case "blocks_placed" -> "§8\uD83E\uDDF1 Top Bloques Puestos";
            case "deaths" -> "§8\u2620 Top Muertes";
            case "playtime" -> "§8\u23F1 Top Playtime";
            default -> "§8Leaderboard";
        };

        boolean fIsPlaytime = isPlaytime;

        new BorderedPaginatedMenu(player) {
            { currentPage = Math.max(1, Math.min(page + 1, Math.max(1, (int) Math.ceil((double) sorted.size() / getButtonsPerPage())))); }

            @Override
            public Map<Integer, Button> getPagesButtons(Player p) {
                Map<Integer, Button> buttons = new LinkedHashMap<>();
                int index = 0;
                for (Map.Entry<UUID, ? extends Number> entry : sorted) {
                    int position = index + 1;
                    String playerName = getPlayerName(entry.getKey());
                    String valueStr = fIsPlaytime
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
                    String displayName = prefix + trophy + "#" + position + " &f" + playerName;
                    String fValueStr = valueStr;
                    buttons.put(index, new Button() {
                        @Override public Material getMaterial(Player p) { return Material.PLAYER_HEAD; }
                        @Override public List<String> getDescription(Player p) { return List.of("&7Valor: &f" + fValueStr); }
                        @Override public String getDisplayName(Player p) { return displayName; }
                        @Override public int getData(Player p) { return 0; }
                        @Override public void onClick(Player p, int slot, ClickType type) {}
                        @Override
                        public ItemStack getButtonItem(Player p) {
                            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                            SkullMeta meta = (SkullMeta) head.getItemMeta();
                            meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', displayName));
                            OfflinePlayer off = Bukkit.getOfflinePlayer(entry.getKey());
                            meta.setOwningPlayer(off);
                            meta.setLore(List.of(org.bukkit.ChatColor.GRAY + "Valor: " + org.bukkit.ChatColor.WHITE + fValueStr));
                            head.setItemMeta(meta);
                            return head;
                        }
                    });
                    index++;
                }
                return buttons;
            }

            @Override
            public String getTitle(Player p) {
                return title;
            }

            @Override
            public Map<Integer, Button> getHeaderItems(Player p) {
                Map<Integer, Button> headers = super.getHeaderItems(p);
                headers.put(40, new Button() {
                    @Override public Material getMaterial(Player p) { return Material.BARRIER; }
                    @Override public List<String> getDescription(Player p) { return List.of("&7Volver al menú principal"); }
                    @Override public String getDisplayName(Player p) { return "&c\u2190 Volver al Menú"; }
                    @Override public int getData(Player p) { return 0; }
                    @Override public void onClick(Player p, int slot, ClickType type) {
                        openMain(p);
                    }
                });
                return headers;
            }
        }.updateMenu();
    }

    @SuppressWarnings("deprecation")
    private String getPlayerName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) return online.getName();
        OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
        return off.getName() != null ? off.getName() : "Unknown";
    }
}
