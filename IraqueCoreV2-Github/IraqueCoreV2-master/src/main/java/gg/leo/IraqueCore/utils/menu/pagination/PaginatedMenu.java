package gg.leo.IraqueCore.utils.menu.pagination;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.ItemBuilder;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.MenuController;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PaginatedMenu {

    protected final int displaySize;
    protected final Player player;
    public int currentPage = 1;
    protected int maxPages = 1;

    private Map<Integer, Button> cachedButtons = Map.of();
    private Inventory openInventory;

    public PaginatedMenu(int displaySize, Player player) {
        this.displaySize = displaySize;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
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

        if (currentPage > maxPages) currentPage = maxPages;
        if (currentPage < 1) currentPage = 1;

        buttons.putAll(getHeaderItems(player));
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

        cachedButtons = Map.copyOf(buttons);
        return buttons;
    }

    public Button getButton(int slot) {
        return cachedButtons.get(slot);
    }

    public List<Integer> getPageButtonPositions() {
        return List.of(0, 8);
    }

    public Button getPreviousPageButton() {
        boolean canGo = currentPage > 1;
        return new Button() {
            @Override
            public Material getMaterial(Player player) {
                return canGo ? Material.ARROW : Material.GRAY_DYE;
            }

            @Override
            public List<String> getDescription(Player player) {
                return List.of(canGo
                        ? "&7Ir a la página anterior"
                        : "&cYa estás en la primera página");
            }

            @Override
            public String getDisplayName(Player player) {
                return "&c« Anterior &7(&e" + currentPage + "&7/&e" + maxPages + "&7)";
            }

            @Override
            public void onClick(Player player, int slot, ClickType type) {
                if (currentPage <= 1) return;
                currentPage--;
                updateMenu();
            }
        };
    }

    public Button getNextPageButton() {
        boolean canGo = currentPage < maxPages;
        return new Button() {
            @Override
            public Material getMaterial(Player player) {
                return canGo ? Material.ARROW : Material.GRAY_DYE;
            }

            @Override
            public List<String> getDescription(Player player) {
                return List.of(canGo
                        ? "&7Ir a la página siguiente"
                        : "&cYa estás en la última página");
            }

            @Override
            public String getDisplayName(Player player) {
                return "&aSiguiente » &7(&e" + currentPage + "&7/&e" + maxPages + "&7)";
            }

            @Override
            public void onClick(Player player, int slot, ClickType type) {
                if (currentPage >= maxPages) return;
                currentPage++;
                updateMenu();
            }
        };
    }

    public void openMenu() {
        updateMenu();
    }

    public void updateMenu() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(IraqueCore.getInstance(), this::updateMenu);
            return;
        }
        if (!player.isOnline()) return;

        Map<Integer, Button> buttons = getButtonsInRange(player);
        int size = displaySize + 9;
        String title = ItemBuilder.color("(" + currentPage + "/" + maxPages + ") " + getTitle(player));

        Inventory current = player.getOpenInventory().getTopInventory();
        if (openInventory != null && current == openInventory && current.getSize() == size) {
            render(current, buttons, size);
            MenuController.register(this);
            return;
        }

        var uuid = player.getUniqueId();
        MenuController.beginUpdate(uuid);
        try {
            Inventory inv = Bukkit.createInventory(null, size, title != null ? title : "Menu");
            render(inv, buttons, size);
            openInventory = inv;
            MenuController.register(this);
            player.openInventory(inv);
        } finally {
            MenuController.endUpdate(uuid);
        }
    }

    private void render(Inventory inv, Map<Integer, Button> buttons, int size) {
        inv.clear();
        for (Map.Entry<Integer, Button> entry : buttons.entrySet()) {
            int slot = entry.getKey();
            if (slot < 0 || slot >= size) continue;
            inv.setItem(slot, entry.getValue().constructItemStack(player));
        }
    }
}
