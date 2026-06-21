package gg.leo.IraqueCore.menu;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.armorstand.ArmorStandEditor;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;

import java.util.*;

public class ArmorStandMenu extends Menu {

    private final IraqueCore plugin;
    private final ArmorStand stand;
    private final ArmorStandEditor editor;

    public ArmorStandMenu(IraqueCore plugin, Player player, ArmorStand stand, ArmorStandEditor editor) {
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

        buttons.put(10, toggleButton(Material.CHAINMAIL_CHESTPLATE, "armorstand.arms", "&6Arms",
                stand.hasArms(), () -> stand.setArms(!stand.hasArms())));
        buttons.put(11, toggleButton(Material.STONE_PRESSURE_PLATE, "armorstand.base-plate", "&6Base Plate",
                stand.hasBasePlate(), () -> stand.setBasePlate(!stand.hasBasePlate())));
        buttons.put(12, toggleButton(Material.ANVIL, "armorstand.gravity", "&6Gravity",
                stand.hasGravity(), () -> stand.setGravity(!stand.hasGravity())));
        buttons.put(13, toggleButton(Material.GLASS_PANE, "armorstand.visible", "&6Visible",
                stand.isVisible(), () -> stand.setVisible(!stand.isVisible())));
        buttons.put(14, toggleButton(Material.SLIME_BALL, "armorstand.small", "&6Small",
                stand.isSmall(), () -> stand.setSmall(!stand.isSmall())));
        buttons.put(15, toggleButton(Material.GLOWSTONE_DUST, "armorstand.glow", "&6Glow",
                stand.isGlowing(), () -> stand.setGlowing(!stand.isGlowing())));
        buttons.put(16, toggleButton(Material.ENDER_PEARL, "armorstand.marker", "&6Marker",
                stand.isMarker(), () -> stand.setMarker(!stand.isMarker())));
        buttons.put(17, toggleButton(Material.LEATHER_BOOTS, "armorstand.can-move", "&6Can Move",
                stand.canMove(), () -> stand.setCanMove(!stand.canMove())));

        buttons.put(20, actionButton(Material.FEATHER, "armorstand.rotate-left", "&eRotate Left",
                () -> stand.setRotation(stand.getLocation().getYaw() - 45f, 0f)));
        buttons.put(21, actionButton(Material.FEATHER, "armorstand.rotate-right", "&eRotate Right",
                () -> stand.setRotation(stand.getLocation().getYaw() + 45f, 0f)));
        buttons.put(22, actionButton(Material.BARRIER, "armorstand.reset", "&cReset Pose", () -> {
            stand.setHeadPose(EulerAngle.ZERO);
            stand.setBodyPose(EulerAngle.ZERO);
            stand.setLeftArmPose(new EulerAngle(Math.toRadians(345), 0, 0));
            stand.setRightArmPose(new EulerAngle(Math.toRadians(345), 0, 0));
            stand.setLeftLegPose(EulerAngle.ZERO);
            stand.setRightLegPose(EulerAngle.ZERO);
        }));
        buttons.put(23, actionButton(Material.BOOK, "armorstand.copy-pose", "&aCopy Pose", () -> {
            editor.copiedPose.put(player.getUniqueId(), new ArmorStandEditor.ArmorStandPose(
                    stand.getHeadPose(), stand.getBodyPose(),
                    stand.getLeftArmPose(), stand.getRightArmPose(),
                    stand.getLeftLegPose(), stand.getRightLegPose()
            ));
            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().getPrefixedMessage("armorstand.pose-copied")));
        }));
        buttons.put(24, actionButton(Material.WRITABLE_BOOK, "armorstand.paste-pose", "&aPaste Pose", () -> {
            ArmorStandEditor.ArmorStandPose pose = editor.copiedPose.get(player.getUniqueId());
            if (pose == null) {
                player.sendMessage(plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().getPrefixedMessage("armorstand.no-pose")));
                return;
            }
            stand.setHeadPose(pose.head());
            stand.setBodyPose(pose.body());
            stand.setLeftArmPose(pose.leftArm());
            stand.setRightArmPose(pose.rightArm());
            stand.setLeftLegPose(pose.leftLeg());
            stand.setRightLegPose(pose.rightLeg());
        }));
        buttons.put(25, actionButton(Material.EMERALD, "armorstand.close", "&aClose", () -> {
            editor.cleanup(player);
            Bukkit.getScheduler().runTask(plugin, player::closeInventory);
        }));

        return buttons;
    }

    @Override
    public String getTitle(Player player) {
        return plugin.getConfigManager().toLegacy(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("armorstand.edit-title", "&8Armor Stand Editor")));
    }

    private Button toggleButton(Material material, String path, String def, boolean enabled, Runnable action) {
        return new Button() {
            @Override public Material getMaterial(Player p) { return material; }
            @Override public List<String> getDescription(Player p) {
                return List.of(status(enabled));
            }
            @Override public String getDisplayName(Player p) {
                return plugin.getConfigManager().getMessage(path, def);
            }
            @Override public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                action.run();
                new ArmorStandMenu(plugin, p, stand, editor).openMenu();
            }
        };
    }

    private Button actionButton(Material material, String path, String def, Runnable action) {
        return new Button() {
            @Override public Material getMaterial(Player p) { return material; }
            @Override public List<String> getDescription(Player p) { return List.of(); }
            @Override public String getDisplayName(Player p) {
                return plugin.getConfigManager().getMessage(path, def);
            }
            @Override public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                action.run();
                // Only refresh if not the close button
                if (material != Material.EMERALD) {
                    new ArmorStandMenu(plugin, p, stand, editor).openMenu();
                }
            }
        };
    }

    private static String status(boolean enabled) {
        return enabled ? "&a\u2713 Enabled" : "&c\u2717 Disabled";
    }
}
