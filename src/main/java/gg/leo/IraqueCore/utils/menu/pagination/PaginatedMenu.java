package gg.leo.IraqueCore.utils.menu.pagination;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.MenuController;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class PaginatedMenu {

    protected final int displaySize;
    protected final Player player;
    protected int currentPage = 1;
    protected int maxPages = 1;

    public PaginatedMenu(int displaySize, Player player) {
        this.displaySize = displaySize;
        this.player = player;
    }

    public abstract Map<Integer, Button> getPagesButtons(Player player);
    public abstract String getTitle(Player player);

    public int getButtonsPerPage() {
        return 18;
    }

    public List<Integer> getButtonPositions() {
        List<Integer> positions = new ArrayList<>();
        for (int i = 9; i < displaySize + 9; i++) {
            positions.add(i);
        }
        return positions;
    }

    public Map<Integer, Button> getHeaderItems(Player player) {
        return new HashMap<>();
    }

    public Map<Integer, Button> getButtonsInRange(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        Map<Integer, Button> paginatedButtons = getPagesButtons(player);
        int buttonAmount = paginatedButtons.size();
        maxPages = buttonAmount == 0 ? 1 : (int) Math.ceil((double) buttonAmount / getButtonsPerPage());

        Map<Integer, Button> header = getHeaderItems(player);
        for (Map.Entry<Integer, Button> entry : header.entrySet()) {
            buttons.put(entry.getKey(), entry.getValue());
        }

        buttons.put(getPageButtonPositions().get(0), getPreviousPageButton());
        buttons.put(getPageButtonPositions().get(1), getNextPageButton());

        int minIndex = (currentPage - 1) * getButtonsPerPage();
        int maxIndex = currentPage * getButtonsPerPage();

        List<Integer> positions = getButtonPositions();
        int posIndex = 0;
        int i = 0;

        for (Map.Entry<Integer, Button> entry : paginatedButtons.entrySet()) {
            if (i < minIndex || i >= maxIndex) {
                i++;
                continue;
            }
            if (posIndex >= positions.size()) break;

            buttons.put(positions.get(posIndex), entry.getValue());
            posIndex++;
            i++;
        }

        return buttons;
    }

    public List<Integer> getPageButtonPositions() {
        return List.of(0, 8);
    }

    public Button getPreviousPageButton() {
        return new Button() {
            @Override
            public Material getMaterial(Player player) {
                return Material.PAPER;
            }

            @Override
            public List<String> getDescription(Player player) {
                return List.of(ChatColor.translateAlternateColorCodes('&', "&eNavigate to previous page"));
            }

            @Override
            public String getDisplayName(Player player) {
                return ChatColor.translateAlternateColorCodes('&',
                        "&cPrevious Page &7(&e" + currentPage + "&7/&e" + maxPages + "&7)");
            }

            @Override
            public int getData(Player player) {
                return 0;
            }

            @Override
            public void onClick(Player player, int slot, ClickType type) {
                if (currentPage == 1) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou are already on the first page!"));
                    return;
                }
                currentPage--;
                updateMenu();
            }
        };
    }

    public Button getNextPageButton() {
        return new Button() {
            @Override
            public Material getMaterial(Player player) {
                return Material.PAPER;
            }

            @Override
            public List<String> getDescription(Player player) {
                return List.of(ChatColor.translateAlternateColorCodes('&', "&eNavigate to next page"));
            }

            @Override
            public String getDisplayName(Player player) {
                return ChatColor.translateAlternateColorCodes('&',
                        "&aNext Page &7(&e" + currentPage + "&7/&e" + maxPages + "&7)");
            }

            @Override
            public int getData(Player player) {
                return 0;
            }

            @Override
            public void onClick(Player player, int slot, ClickType type) {
                if (currentPage >= maxPages) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have already reached the last page!"));
                    return;
                }
                currentPage++;
                updateMenu();
            }
        };
    }

    public void updateMenu() {
        Map<Integer, Button> buttons = getButtonsInRange(player);

        int size = displaySize + 9;
        String title = "(" + currentPage + "/" + (maxPages == 0 ? 1 : maxPages) + ") " + getTitle(player);

        Inventory inv = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', title));

        MenuController.menus.remove(player.getUniqueId());
        MenuController.paginatedMenus.put(player.getUniqueId(), this);

        for (Map.Entry<Integer, Button> entry : buttons.entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue().constructItemStack(player));
        }

        player.openInventory(inv);
        player.updateInventory();
    }
}
