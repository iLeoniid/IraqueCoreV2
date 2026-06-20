package gg.leo.IraqueCore.menu;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ArmorStandMenu {

    private final IraqueCore plugin;

    public static final int SLOT_ARMS = 10;
    public static final int SLOT_BASE_PLATE = 11;
    public static final int SLOT_GRAVITY = 12;
    public static final int SLOT_VISIBLE = 13;
    public static final int SLOT_SMALL = 14;
    public static final int SLOT_GLOW = 15;
    public static final int SLOT_MARKER = 16;
    public static final int SLOT_CAN_MOVE = 17;
    public static final int SLOT_ROTATE_L = 20;
    public static final int SLOT_ROTATE_R = 21;
    public static final int SLOT_RESET = 22;
    public static final int SLOT_COPY = 23;
    public static final int SLOT_PASTE = 24;
    public static final int SLOT_CLOSE = 25;

    public ArmorStandMenu(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void openEditor(Player player, ArmorStand stand) {
        Inventory inv = Bukkit.createInventory(null, 27, plugin.getConfigManager().toLegacy(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("armorstand.edit-title", "&8Armor Stand Editor"))));

        ItemStack bg = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 27; i++) {
            if (isOptionSlot(i)) continue;
            inv.setItem(i, bg);
        }

        boolean arms = stand.hasArms();
        inv.setItem(SLOT_ARMS, ItemBuilder.of(Material.CHAINMAIL_CHESTPLATE)
                .name(plugin.getConfigManager().getMessage("armorstand.arms", "&6Arms"))
                .lore(status(arms)).build());

        boolean plate = stand.hasBasePlate();
        inv.setItem(SLOT_BASE_PLATE, ItemBuilder.of(Material.STONE_PRESSURE_PLATE)
                .name(plugin.getConfigManager().getMessage("armorstand.base-plate", "&6Base Plate"))
                .lore(status(plate)).build());

        boolean gravity = stand.hasGravity();
        inv.setItem(SLOT_GRAVITY, ItemBuilder.of(Material.ANVIL)
                .name(plugin.getConfigManager().getMessage("armorstand.gravity", "&6Gravity"))
                .lore(status(gravity)).build());

        boolean visible = stand.isVisible();
        inv.setItem(SLOT_VISIBLE, ItemBuilder.of(Material.GLASS_PANE)
                .name(plugin.getConfigManager().getMessage("armorstand.visible", "&6Visible"))
                .lore(status(visible)).build());

        boolean small = stand.isSmall();
        inv.setItem(SLOT_SMALL, ItemBuilder.of(Material.SLIME_BALL)
                .name(plugin.getConfigManager().getMessage("armorstand.small", "&6Small"))
                .lore(status(small)).build());

        boolean glow = stand.isGlowing();
        inv.setItem(SLOT_GLOW, ItemBuilder.of(Material.GLOWSTONE_DUST)
                .name(plugin.getConfigManager().getMessage("armorstand.glow", "&6Glow"))
                .lore(status(glow)).build());

        boolean marker = stand.isMarker();
        inv.setItem(SLOT_MARKER, ItemBuilder.of(Material.ENDER_PEARL)
                .name(plugin.getConfigManager().getMessage("armorstand.marker", "&6Marker"))
                .lore(status(marker)).build());

        boolean canMove = stand.canMove();
        inv.setItem(SLOT_CAN_MOVE, ItemBuilder.of(Material.LEATHER_BOOTS)
                .name(plugin.getConfigManager().getMessage("armorstand.can-move", "&6Can Move"))
                .lore(status(canMove)).build());

        inv.setItem(SLOT_RESET, ItemBuilder.of(Material.BARRIER)
                .name(plugin.getConfigManager().getMessage("armorstand.reset", "&cReset Pose")).build());

        inv.setItem(SLOT_ROTATE_L, ItemBuilder.of(Material.FEATHER)
                .name(plugin.getConfigManager().getMessage("armorstand.rotate-left", "&eRotate Left")).build());

        inv.setItem(SLOT_ROTATE_R, ItemBuilder.of(Material.FEATHER)
                .name(plugin.getConfigManager().getMessage("armorstand.rotate-right", "&eRotate Right")).build());

        inv.setItem(SLOT_COPY, ItemBuilder.of(Material.BOOK)
                .name(plugin.getConfigManager().getMessage("armorstand.copy-pose", "&aCopy Pose")).build());

        inv.setItem(SLOT_PASTE, ItemBuilder.of(Material.WRITABLE_BOOK)
                .name(plugin.getConfigManager().getMessage("armorstand.paste-pose", "&aPaste Pose")).build());

        inv.setItem(SLOT_CLOSE, ItemBuilder.of(Material.EMERALD)
                .name(plugin.getConfigManager().getMessage("armorstand.close", "&aClose")).build());

        player.openInventory(inv);
    }

    public static boolean isOptionSlot(int slot) {
        return slot == SLOT_ARMS || slot == SLOT_BASE_PLATE || slot == SLOT_GRAVITY
                || slot == SLOT_VISIBLE || slot == SLOT_SMALL || slot == SLOT_GLOW
                || slot == SLOT_MARKER || slot == SLOT_CAN_MOVE
                || slot == SLOT_RESET || slot == SLOT_ROTATE_L || slot == SLOT_ROTATE_R
                || slot == SLOT_COPY || slot == SLOT_PASTE || slot == SLOT_CLOSE;
    }

    public static String status(boolean enabled) {
        return enabled
                ? "&a\u2713 Enabled"
                : "&c\u2717 Disabled";
    }
}
