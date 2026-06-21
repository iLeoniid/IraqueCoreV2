package gg.leo.IraqueCore.armorstand.menu;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.armorstand.ArmorStandEditor;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.util.EulerAngle;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ArmorStandPoseMenu extends Menu {

    private static final int STEP_DEGREES = 5;

    private final IraqueCore plugin;
    private final ArmorStand stand;
    private final ArmorStandEditor editor;
    private final List<PosePart> parts;

    public ArmorStandPoseMenu(IraqueCore plugin, Player player, ArmorStand stand, ArmorStandEditor editor) {
        super(player);
        this.plugin = plugin;
        this.stand = stand;
        this.editor = editor;
        this.staticSize = 54;
        this.placeholder = true;
        this.parts = List.of(
                new PosePart("&6Cabeza", Material.PLAYER_HEAD, stand::getHeadPose, stand::setHeadPose),
                new PosePart("&6Cuerpo", Material.LEATHER_CHESTPLATE, stand::getBodyPose, stand::setBodyPose),
                new PosePart("&6Brazo Izquierdo", Material.STICK, stand::getLeftArmPose, stand::setLeftArmPose),
                new PosePart("&6Brazo Derecho", Material.STICK, stand::getRightArmPose, stand::setRightArmPose),
                new PosePart("&6Pierna Izquierda", Material.LEATHER_LEGGINGS, stand::getLeftLegPose, stand::setLeftLegPose),
                new PosePart("&6Pierna Derecha", Material.LEATHER_LEGGINGS, stand::getRightLegPose, stand::setRightLegPose)
        );
    }

    private record PosePart(String displayName, Material icon,
                             Supplier<EulerAngle> getter, Consumer<EulerAngle> setter) {}

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (int row = 0; row < parts.size(); row++) {
            PosePart part = parts.get(row);
            int base = row * 9;

            buttons.put(base, backButton(part));
            buttons.put(base + 1, axisButton(part, "X", -1, "&c-X (" + STEP_DEGREES + "\u00b0)"));
            buttons.put(base + 2, axisButton(part, "X", 1, "&a+X (" + STEP_DEGREES + "\u00b0)"));
            buttons.put(base + 3, axisButton(part, "Y", -1, "&c-Y (" + STEP_DEGREES + "\u00b0)"));
            buttons.put(base + 4, axisButton(part, "Y", 1, "&a+Y (" + STEP_DEGREES + "\u00b0)"));
            buttons.put(base + 5, axisButton(part, "Z", -1, "&c-Z (" + STEP_DEGREES + "\u00b0)"));
            buttons.put(base + 6, axisButton(part, "Z", 1, "&a+Z (" + STEP_DEGREES + "\u00b0)"));
            buttons.put(base + 7, resetPartButton(part));
        }

        return buttons;
    }

    @Override
    public String getTitle(Player player) {
        return plugin.getConfigManager().toLegacy(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("armorstand.pose-editor-title", "&8Editor de Pose")));
    }

    private Button backButton(PosePart part) {
        EulerAngle angle = part.getter().get();
        List<String> lore = List.of(
                "&7X: &f" + degrees(angle.getX()) + "\u00b0",
                "&7Y: &f" + degrees(angle.getY()) + "\u00b0",
                "&7Z: &f" + degrees(angle.getZ()) + "\u00b0",
                "",
                "&eClick para volver al menu principal"
        );
        return new Button() {
            @Override public Material getMaterial(Player p) { return part.icon(); }
            @Override public List<String> getDescription(Player p) { return lore; }
            @Override public String getDisplayName(Player p) { return part.displayName(); }
            @Override public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                new ArmorStandMenu(plugin, p, stand, editor).openMenu();
            }
        };
    }

    private Button axisButton(PosePart part, String axis, int direction, String name) {
        return new Button() {
            @Override public Material getMaterial(Player p) { return Material.FEATHER; }
            @Override public List<String> getDescription(Player p) {
                return List.of("&7Mueve " + STEP_DEGREES + "\u00b0 en el eje " + axis);
            }
            @Override public String getDisplayName(Player p) { return name; }
            @Override public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                EulerAngle current = part.getter().get();
                double delta = Math.toRadians(STEP_DEGREES) * direction;
                EulerAngle updated = switch (axis) {
                    case "X" -> new EulerAngle(current.getX() + delta, current.getY(), current.getZ());
                    case "Y" -> new EulerAngle(current.getX(), current.getY() + delta, current.getZ());
                    default -> new EulerAngle(current.getX(), current.getY(), current.getZ() + delta);
                };
                part.setter().accept(updated);
                new ArmorStandPoseMenu(plugin, p, stand, editor).openMenu();
            }
        };
    }

    private Button resetPartButton(PosePart part) {
        return new Button() {
            @Override public Material getMaterial(Player p) { return Material.BARRIER; }
            @Override public List<String> getDescription(Player p) {
                return List.of("&7Restablece esta parte a 0\u00b0, 0\u00b0, 0\u00b0");
            }
            @Override public String getDisplayName(Player p) { return "&cReset"; }
            @Override public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                part.setter().accept(EulerAngle.ZERO);
                new ArmorStandPoseMenu(plugin, p, stand, editor).openMenu();
            }
        };
    }

    private static long degrees(double radians) {
        return Math.round(Math.toDegrees(radians));
    }
}