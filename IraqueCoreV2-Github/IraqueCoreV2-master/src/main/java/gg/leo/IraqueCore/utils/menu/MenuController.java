package gg.leo.IraqueCore.utils.menu;

import gg.leo.IraqueCore.utils.menu.pagination.PaginatedMenu;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MenuController {

    private static final Map<UUID, Menu> MENUS = new ConcurrentHashMap<>();
    private static final Map<UUID, PaginatedMenu> PAGINATED = new ConcurrentHashMap<>();
    private static final Set<UUID> UPDATING = ConcurrentHashMap.newKeySet();

    /** @deprecated Use {@link #getMenu(UUID)} — kept for compatibility */
    @Deprecated
    public static final Map<UUID, Menu> menus = MENUS;

    /** @deprecated Use {@link #getPaginated(UUID)} — kept for compatibility */
    @Deprecated
    public static final Map<UUID, PaginatedMenu> paginatedMenus = PAGINATED;

    private MenuController() {}

    public static void register(Menu menu) {
        UUID uuid = menu.getPlayer().getUniqueId();
        PAGINATED.remove(uuid);
        MENUS.put(uuid, menu);
    }

    public static void register(PaginatedMenu menu) {
        UUID uuid = menu.getPlayer().getUniqueId();
        MENUS.remove(uuid);
        PAGINATED.put(uuid, menu);
    }

    public static void unregister(UUID uuid) {
        MENUS.remove(uuid);
        PAGINATED.remove(uuid);
    }

    public static Menu getMenu(UUID uuid) {
        return MENUS.get(uuid);
    }

    public static PaginatedMenu getPaginated(UUID uuid) {
        return PAGINATED.get(uuid);
    }

    public static boolean hasOpenMenu(UUID uuid) {
        return MENUS.containsKey(uuid) || PAGINATED.containsKey(uuid);
    }

    public static void beginUpdate(UUID uuid) {
        UPDATING.add(uuid);
    }

    public static void endUpdate(UUID uuid) {
        UPDATING.remove(uuid);
    }

    public static boolean isUpdating(UUID uuid) {
        return UPDATING.contains(uuid);
    }

    public static void clear(UUID uuid) {
        MENUS.remove(uuid);
        PAGINATED.remove(uuid);
        UPDATING.remove(uuid);
    }
}
