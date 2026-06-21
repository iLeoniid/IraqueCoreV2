package gg.leo.IraqueCore.utils.menu;

import gg.leo.IraqueCore.utils.menu.pagination.PaginatedMenu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MenuController {

    public static final Map<UUID, Menu> menus = new HashMap<>();
    public static final Map<UUID, PaginatedMenu> paginatedMenus = new HashMap<>();

    private MenuController() {}
}
