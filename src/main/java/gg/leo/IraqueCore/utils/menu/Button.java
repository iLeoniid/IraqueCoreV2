package gg.leo.IraqueCore.utils.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Collections;

public abstract class Button {

    public static Button placeholder() {
        return new Button() {
            @Override
            public Material getMaterial(Player player) { return Material.GRAY_STAINED_GLASS_PANE; }
            @Override
            public List<String> getDescription(Player player) { return Collections.emptyList(); }
            @Override
            public String getDisplayName(Player player) { return " "; }
            @Override
            public int getData(Player player) { return 0; }
            @Override
            public void onClick(Player player, int slot, ClickType type) {}
        };
    }

    public abstract Material getMaterial(Player player);
    public abstract List<String> getDescription(Player player);
    public abstract String getDisplayName(Player player);
    public abstract int getData(Player player);
    public abstract void onClick(Player player, int slot, ClickType type);

    public int getAmount(Player player) {
        return 1;
    }

    public ItemStack getButtonItem(Player player) {
        return null;
    }

    public ItemStack constructItemStack(Player player) {
        ItemStack custom = getButtonItem(player);
        if (custom != null) return custom;

        Material material = getMaterial(player);
        int data = getData(player);

        if (data != 0) {
            String name = material.name();
            if (name.equals("LEGACY_WOOL") || name.equals("WOOL")
                    || name.equals("STAINED_GLASS_PANE") || name.equals("LEGACY_STAINED_GLASS_PANE")) {
                try {
                    org.bukkit.DyeColor dye = org.bukkit.DyeColor.getByWoolData((byte) data);
                    if (dye != null) {
                        String suffix = switch (name) {
                            case "STAINED_GLASS_PANE", "LEGACY_STAINED_GLASS_PANE" -> "_STAINED_GLASS_PANE";
                            default -> "_WOOL";
                        };
                        String modernName = dye.name() + suffix;
                        Material modern = Material.getMaterial(modernName);
                        if (modern != null) {
                            material = modern;
                            data = 0;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        ItemStack item = new ItemStack(material, getAmount(player));
        if (data != 0) {
            try {
                item.setDurability((short) data);
            } catch (Exception ignored) {}
        }

        ItemMeta meta = item.getItemMeta();
        String display = getDisplayName(player);
        if (display != null) meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', display));
        List<String> lore = getDescription(player);
        if (lore != null) {
            meta.setLore(lore.stream()
                    .map(l -> org.bukkit.ChatColor.translateAlternateColorCodes('&', l))
                    .toList());
        }
        item.setItemMeta(meta);
        return item;
    }
}
