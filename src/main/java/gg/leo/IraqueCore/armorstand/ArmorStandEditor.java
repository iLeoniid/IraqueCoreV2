package gg.leo.IraqueCore.armorstand;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.menu.ArmorStandMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmorStandEditor implements Listener {

    private final IraqueCore plugin;
    private final ArmorStandMenu menu;
    private final Map<UUID, ArmorStand> editing = new HashMap<>();
    private final Map<UUID, ArmorStandPose> copiedPose = new HashMap<>();

    public ArmorStandEditor(IraqueCore plugin) {
        this.plugin = plugin;
        this.menu = new ArmorStandMenu(plugin);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand stand)) return;
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        if (!player.hasPermission("iraquecore.armorstand")) return;

        event.setCancelled(true);
        editing.put(player.getUniqueId(), stand);
        menu.openEditor(player, stand);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ArmorStand stand = editing.get(player.getUniqueId());
        if (stand == null) return;
        if (!event.getView().getTitle().contains("Armor Stand")) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (event.getSlot()) {
            case ArmorStandMenu.SLOT_ARMS -> {
                stand.setArms(!stand.hasArms());
                refreshGUI(player, stand);
            }
            case ArmorStandMenu.SLOT_BASE_PLATE -> {
                stand.setBasePlate(!stand.hasBasePlate());
                refreshGUI(player, stand);
            }
            case ArmorStandMenu.SLOT_GRAVITY -> {
                stand.setGravity(!stand.hasGravity());
                refreshGUI(player, stand);
            }
            case ArmorStandMenu.SLOT_VISIBLE -> {
                stand.setVisible(!stand.isVisible());
                refreshGUI(player, stand);
            }
            case ArmorStandMenu.SLOT_SMALL -> {
                stand.setSmall(!stand.isSmall());
                refreshGUI(player, stand);
            }
            case ArmorStandMenu.SLOT_GLOW -> {
                stand.setGlowing(!stand.isGlowing());
                refreshGUI(player, stand);
            }
            case ArmorStandMenu.SLOT_MARKER -> {
                stand.setMarker(!stand.isMarker());
                refreshGUI(player, stand);
            }
            case ArmorStandMenu.SLOT_CAN_MOVE -> {
                stand.setCanMove(!stand.canMove());
                refreshGUI(player, stand);
            }
            case ArmorStandMenu.SLOT_RESET -> {
                stand.setHeadPose(EulerAngle.ZERO);
                stand.setBodyPose(EulerAngle.ZERO);
                stand.setLeftArmPose(new EulerAngle(Math.toRadians(345), 0, 0));
                stand.setRightArmPose(new EulerAngle(Math.toRadians(345), 0, 0));
                stand.setLeftLegPose(EulerAngle.ZERO);
                stand.setRightLegPose(EulerAngle.ZERO);
                refreshGUI(player, stand);
            }
            case ArmorStandMenu.SLOT_ROTATE_L -> {
                stand.setRotation(stand.getLocation().getYaw() - 45f, 0f);
                refreshGUI(player, stand);
            }
            case ArmorStandMenu.SLOT_ROTATE_R -> {
                stand.setRotation(stand.getLocation().getYaw() + 45f, 0f);
                refreshGUI(player, stand);
            }
            case ArmorStandMenu.SLOT_COPY -> {
                copiedPose.put(player.getUniqueId(), new ArmorStandPose(
                        stand.getHeadPose(), stand.getBodyPose(),
                        stand.getLeftArmPose(), stand.getRightArmPose(),
                        stand.getLeftLegPose(), stand.getRightLegPose()
                ));
                player.sendMessage(plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().getPrefixedMessage("armorstand.pose-copied")));
            }
            case ArmorStandMenu.SLOT_PASTE -> {
                ArmorStandPose pose = copiedPose.get(player.getUniqueId());
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
                refreshGUI(player, stand);
            }
            case ArmorStandMenu.SLOT_CLOSE -> {
                editing.remove(player.getUniqueId());
                player.closeInventory();
            }
        }
    }

    private void refreshGUI(Player player, ArmorStand stand) {
        Bukkit.getScheduler().runTask(plugin, () -> menu.openEditor(player, stand));
    }

    public record ArmorStandPose(
            EulerAngle head,
            EulerAngle body,
            EulerAngle leftArm,
            EulerAngle rightArm,
            EulerAngle leftLeg,
            EulerAngle rightLeg
    ) {}
}
