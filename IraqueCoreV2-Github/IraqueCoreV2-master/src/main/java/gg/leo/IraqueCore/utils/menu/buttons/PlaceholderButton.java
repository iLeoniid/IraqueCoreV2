package gg.leo.IraqueCore.utils.menu.buttons;

import gg.leo.IraqueCore.utils.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class PlaceholderButton extends Button {

    private final Material material;
    private final List<String> description;
    private final String name;
    private final int data;

    public PlaceholderButton(Material material, List<String> description, String name, int data) {
        this.material = material != null ? material : Material.GRAY_STAINED_GLASS_PANE;
        this.description = description != null ? description : List.of();
        this.name = name != null ? name : " ";
        this.data = data;
    }

    public static PlaceholderButton glass() {
        return new PlaceholderButton(Material.GRAY_STAINED_GLASS_PANE, List.of(), " ", 0);
    }

    public static PlaceholderButton glass(Material material) {
        return new PlaceholderButton(material, List.of(), " ", 0);
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
        return name;
    }

    @Override
    public int getData(Player player) {
        return data;
    }

    @Override
    public void onClick(Player player, int slot, ClickType type) {}
}
