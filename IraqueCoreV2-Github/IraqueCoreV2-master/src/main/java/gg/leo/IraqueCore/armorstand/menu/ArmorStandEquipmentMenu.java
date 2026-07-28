package gg.leo.IraqueCore.armorstand.menu;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.armorstand.ArmorStandEditor;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ArmorStandEquipmentMenu extends Menu {

    private final IraqueCore plugin;
    private final ArmorStand stand;
    private final ArmorStandEditor editor;

    public ArmorStandEquipmentMenu(IraqueCore plugin, Player player, ArmorStand stand, ArmorStandEditor editor) {
        super(player);
        this.plugin = plugin;
        this.stand = stand;
        this.editor = editor;
        this.staticSize = 27;
        this.placeholder = true;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        EntityEquipment eq = stand.getEquipment();

        buttons.put(10, equipmentButton("armorstand.helmet", "&6Casco", eq::getHelmet, eq::setHelmet));
        buttons.put(11, equipmentButton("armorstand.chestplate", "&6Pechera", eq::getChestplate, eq::setChestplate));
        buttons.put(12, equipmentButton("armorstand.leggings", "&6Pantalones", eq::getLeggings, eq::setLeggings));
        buttons.put(13, equipmentButton("armorstand.boots", "&6Botas", eq::getBoots, eq::setBoots));
        buttons.put(15, equipmentButton("armorstand.main-hand", "&6Mano Principal", eq::getItemInMainHand, eq::setItemInMainHand));
        buttons.put(16, equipmentButton("armorstand.off-hand", "&6Mano Secundaria", eq::getItemInOffHand, eq::setItemInOffHand));

        buttons.put(20, resetEquipmentButton(eq));
        buttons.put(22, backButton());
        buttons.put(25, closeButton());

        return buttons;
    }

    @Override
    public String getTitle(Player player) {
        return plugin.getConfigManager().toLegacy(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("armorstand.equipment-title", "&8Editor de Equipo")));
    }

    private Button equipmentButton(String path, String def, Supplier<ItemStack> getter, Consumer<ItemStack> setter) {
        return new Button() {
            @Override
            public Material getMaterial(Player p) {
                ItemStack current = getter.get();
                return (current != null && current.getType() != Material.AIR)
                        ? current.getType() : Material.LIGHT_GRAY_STAINED_GLASS_PANE;
            }
            @Override
            public List<String> getDescription(Player p) {
                ItemStack current = getter.get();
                boolean empty = current == null || current.getType() == Material.AIR;
                List<String> lore = new ArrayList<>();
                lore.add(empty ? "&7Vacio" : "&7Equipado");
                lore.add("");
                lore.add("&aClick &7con un objeto en mano para equiparlo");
                lore.add("&cClick derecho &7para quitarlo");
                return lore;
            }
            @Override
            public String getDisplayName(Player p) {
                return plugin.getConfigManager().getMessage(path, def);
            }
            @Override public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                if (type == ClickType.RIGHT) {
                    setter.accept(null);
                } else if (type == ClickType.LEFT) {
                    ItemStack hand = p.getInventory().getItemInMainHand();
                    if (hand.getType() == Material.AIR) {
                        p.sendMessage(plugin.getConfigManager().deserialize(
                                plugin.getConfigManager().getPrefixedMessage("armorstand.no-item-in-hand")));
                        return;
                    }
                    setter.accept(hand.clone());
                }
                new ArmorStandEquipmentMenu(plugin, p, stand, editor).openMenu();
            }
        };
    }

    private Button resetEquipmentButton(EntityEquipment eq) {
        return new Button() {
            @Override public Material getMaterial(Player p) { return Material.BARRIER; }
            @Override public List<String> getDescription(Player p) {
                return List.of("&7Quita toda la armadura y los objetos en mano");
            }
            @Override public String getDisplayName(Player p) { return "&cQuitar Todo"; }
            @Override public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                eq.setHelmet(null);
                eq.setChestplate(null);
                eq.setLeggings(null);
                eq.setBoots(null);
                eq.setItemInMainHand(null);
                eq.setItemInOffHand(null);
                new ArmorStandEquipmentMenu(plugin, p, stand, editor).openMenu();
            }
        };
    }

    private Button backButton() {
        return new Button() {
            @Override public Material getMaterial(Player p) { return Material.ARROW; }
            @Override public List<String> getDescription(Player p) { return List.of(); }
            @Override public String getDisplayName(Player p) { return "&eVolver"; }
            @Override public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                new ArmorStandMenu(plugin, p, stand, editor).openMenu();
            }
        };
    }

    private Button closeButton() {
        return new Button() {
            @Override public Material getMaterial(Player p) { return Material.EMERALD; }
            @Override public List<String> getDescription(Player p) { return List.of(); }
            @Override public String getDisplayName(Player p) { return "&aClose"; }
            @Override public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                editor.cleanup(p);
                Bukkit.getScheduler().runTask(plugin, (Runnable) p::closeInventory);
            }
        };
    }
}