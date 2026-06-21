package gg.leo.IraqueCore.utils.menu.type;

import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.pagination.PaginatedMenu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BorderedPaginatedMenu extends PaginatedMenu {

    public BorderedPaginatedMenu(Player player) {
        super(36, player);
    }

    @Override
    public List<Integer> getButtonPositions() {
        return List.of(
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        );
    }

    @Override
    public Map<Integer, Button> getHeaderItems(Player player) {
        Map<Integer, Button> headers = new HashMap<>();
        Button placeholder = Button.placeholder();

        for (int slot : List.of(1, 2, 3, 4, 5, 6, 7, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44)) {
            headers.put(slot, placeholder);
        }
        return headers;
    }

    @Override
    public int getButtonsPerPage() {
        return 21;
    }
}
