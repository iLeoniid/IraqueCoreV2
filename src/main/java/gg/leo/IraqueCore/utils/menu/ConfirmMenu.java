package gg.leo.IraqueCore.utils.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfirmMenu {

    public static void open(Player player, String title, List<String> lore, Runnable onConfirm) {
        new Menu(player) {
            {
                staticSize = 27;
                placeholder = true;
            }

            @Override
            public Map<Integer, Button> getButtons(Player p) {
                Map<Integer, Button> buttons = new HashMap<>();

                buttons.put(11, new Button() {
                    @Override
                    public Material getMaterial(Player p) { return Material.EMERALD_BLOCK; }
                    @Override
                    public List<String> getDescription(Player p) {
                        List<String> description = new ArrayList<>(lore);
                        description.add("");
                        description.add("&a\u25B8 Clique para confirmar");
                        return description;
                    }
                    @Override
                    public String getDisplayName(Player p) { return "&a&lConfirmar"; }
                    @Override
                    public int getData(Player p) { return 0; }
                    @Override
                    public void onClick(Player p, int slot, ClickType type) {
                        p.closeInventory();
                        if (onConfirm != null) onConfirm.run();
                    }
                });

                buttons.put(15, new Button() {
                    @Override
                    public Material getMaterial(Player p) { return Material.BARRIER; }
                    @Override
                    public List<String> getDescription(Player p) { return List.of("&7Clique para cancelar"); }
                    @Override
                    public String getDisplayName(Player p) { return "&c&lCancelar"; }
                    @Override
                    public int getData(Player p) { return 0; }
                    @Override
                    public void onClick(Player p, int slot, ClickType type) { p.closeInventory(); }
                });

                return buttons;
            }

            @Override
            public String getTitle(Player p) { return title; }
        }.openMenu();
    }
}
