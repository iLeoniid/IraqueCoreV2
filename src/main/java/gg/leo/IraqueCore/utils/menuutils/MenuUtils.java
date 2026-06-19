package gg.leo.IraqueCore.utils.menuutils;

import gg.leo.IraqueCore.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class MenuUtils {

    private MenuUtils() {}

    public static void fillBackground(Inventory inv, Material material) {
        ItemStack bg = ItemBuilder.of(material).name(" ").build();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                inv.setItem(i, bg);
            }
        }
    }

    public static void fillBorders(Inventory inv, Material material) {
        ItemStack bg = ItemBuilder.of(material).name(" ").build();
        int rows = inv.getSize() / 9;
        for (int i = 0; i < inv.getSize(); i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == rows - 1 || col == 0 || col == 8) {
                if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                    inv.setItem(i, bg);
                }
            }
        }
    }

    public static ItemStack backButton() {
        return ItemBuilder.of(Material.BARRIER).name("&c\u2190 Back").build();
    }

    public static ItemStack closeButton() {
        return ItemBuilder.of(Material.EMERALD).name("&aClose").build();
    }

    public static ItemStack prevPageButton() {
        return ItemBuilder.of(Material.ARROW).name("&e\u2190 Previous Page").build();
    }

    public static ItemStack nextPageButton() {
        return ItemBuilder.of(Material.ARROW).name("&eNext Page \u2192").build();
    }
}
