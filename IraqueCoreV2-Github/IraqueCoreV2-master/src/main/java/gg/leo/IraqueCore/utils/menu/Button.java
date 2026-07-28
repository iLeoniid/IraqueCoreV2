package gg.leo.IraqueCore.utils.menu;

import gg.leo.IraqueCore.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class Button {

    public static Button placeholder() {
        return placeholder(Material.GRAY_STAINED_GLASS_PANE);
    }

    public static Button placeholder(Material material) {
        return of(material, " ", Collections.emptyList(), (p, s) -> {});
    }

    public static Button of(Material material, String name, List<String> lore, Consumer<Player> click) {
        return of(material, name, lore, (player, slot) -> click.accept(player));
    }

    public static Button of(Material material, String name, List<String> lore, BiConsumer<Player, Integer> click) {
        return new Button() {
            @Override
            public Material getMaterial(Player player) {
                return material;
            }

            @Override
            public List<String> getDescription(Player player) {
                return lore;
            }

            @Override
            public String getDisplayName(Player player) {
                return name;
            }

            @Override
            public void onClick(Player player, int slot, ClickType type) {
                if (click != null) click.accept(player, slot);
            }
        };
    }

    public abstract Material getMaterial(Player player);

    public abstract String getDisplayName(Player player);

    public abstract void onClick(Player player, int slot, ClickType type);

    public List<String> getDescription(Player player) {
        return Collections.emptyList();
    }

    /** Legacy durability / dye data. Unused on modern versions; override if needed. */
    public int getData(Player player) {
        return 0;
    }

    public int getAmount(Player player) {
        return 1;
    }

    /** Override to supply a fully custom ItemStack (skulls, enchanted items, etc.). */
    public ItemStack getButtonItem(Player player) {
        return null;
    }

    public ItemStack constructItemStack(Player player) {
        ItemStack custom = getButtonItem(player);
        if (custom != null) return custom;

        Material material = getMaterial(player);
        if (material == null || material.isAir()) {
            material = Material.BARRIER;
        }

        ItemBuilder builder = ItemBuilder.of(material, Math.max(1, getAmount(player)));
        String display = getDisplayName(player);
        if (display != null) builder.name(display);

        List<String> lore = getDescription(player);
        if (lore != null && !lore.isEmpty()) builder.lore(lore);

        return builder.build();
    }
}
