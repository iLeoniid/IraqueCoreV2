package gg.leo.IraqueCore.utils.menu.buttons;

import gg.leo.IraqueCore.utils.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.function.BiConsumer;

public class SimpleActionButton extends Button {

    private final Material material;
    private final List<String> description;
    private final String name;
    private final int data;
    private BiConsumer<Player, Integer> onClick;

    public SimpleActionButton(Material material, List<String> description, String name, int data) {
        this.material = material;
        this.description = description;
        this.name = name;
        this.data = data;
    }

    public SimpleActionButton onClick(BiConsumer<Player, Integer> handler) {
        this.onClick = handler;
        return this;
    }

    @Override
    public Material getMaterial(Player player) {
        return material;
    }

    @Override
    public List<String> getDescription(Player player) {
        return description;
    }

    @Override
    public String getDisplayName(Player player) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', name);
    }

    @Override
    public int getData(Player player) {
        return data;
    }

    @Override
    public void onClick(Player player, int slot, ClickType type) {
        if (onClick != null) {
            onClick.accept(player, slot);
        }
    }
}
