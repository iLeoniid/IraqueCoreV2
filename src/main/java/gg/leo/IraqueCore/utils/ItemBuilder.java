package gg.leo.IraqueCore.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    private ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material, 1);
    }

    public static ItemBuilder of(Material material, int amount) {
        return new ItemBuilder(material, amount);
    }

    public static String color(String text) {
        if (text == null) return null;
        return ChatColor.translateAlternateColorCodes('&',
                text.replaceAll("&#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])",
                        "§x§$1§$2§$3§$4§$5§$6"));
    }

    public ItemBuilder name(String name) {
        if (name == null) return this;
        meta.setDisplayName(color(name));
        return this;
    }

    public ItemBuilder lore(String... lines) {
        meta.setLore(Arrays.stream(lines)
                .map(ItemBuilder::color)
                .collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        if (lines == null) return this;
        meta.setLore(lines.stream()
                .map(ItemBuilder::color)
                .collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder skullOwner(String playerName) {
        if (playerName == null || !(meta instanceof SkullMeta skullMeta)) return this;
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
