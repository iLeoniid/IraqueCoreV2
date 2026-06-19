package gg.leo.IraqueCore.armorstand;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmorStandEditor implements Listener {

    private final IraqueCore plugin;
    private final Map<UUID, ArmorStand> editing = new HashMap<>();
    private final Map<UUID, ArmorStandPose> copiedPose = new HashMap<>();

    private static final int SLOT_ARMS = 10;
    private static final int SLOT_BASE_PLATE = 11;
    private static final int SLOT_GRAVITY = 12;
    private static final int SLOT_VISIBLE = 13;
    private static final int SLOT_SMALL = 14;
    private static final int SLOT_GLOW = 15;
    private static final int SLOT_RESET = 16;
    private static final int SLOT_ROTATE_L = 20;
    private static final int SLOT_ROTATE_R = 21;
    private static final int SLOT_COPY = 22;
    private static final int SLOT_PASTE = 23;
    private static final int SLOT_CLOSE = 24;

    public ArmorStandEditor(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand stand)) return;
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        if (!player.hasPermission("iraquecore.armorstand")) return;

        event.setCancelled(true);
        openEditor(player, stand);
    }

    private void openEditor(Player player, ArmorStand stand) {
        editing.put(player.getUniqueId(), stand);

        Inventory inv = Bukkit.createInventory(null, 27, plugin.getConfigManager().toLegacy(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("armorstand.edit-title", "&8Armor Stand Editor"))));

        ItemStack bg = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 27; i++) {
            if (isOptionSlot(i)) continue;
            inv.setItem(i, bg);
        }

        boolean arms = stand.hasArms();
        inv.setItem(SLOT_ARMS, ItemBuilder.of(Material.CHAINMAIL_CHESTPLATE)
                .name(plugin.getConfigManager().getMessage("armorstand.arms", "&6Arms"))
                .lore(status(arms)).build());

        boolean plate = stand.hasBasePlate();
        inv.setItem(SLOT_BASE_PLATE, ItemBuilder.of(Material.STONE_PRESSURE_PLATE)
                .name(plugin.getConfigManager().getMessage("armorstand.base-plate", "&6Base Plate"))
                .lore(status(plate)).build());

        boolean gravity = stand.hasGravity();
        inv.setItem(SLOT_GRAVITY, ItemBuilder.of(Material.ANVIL)
                .name(plugin.getConfigManager().getMessage("armorstand.gravity", "&6Gravity"))
                .lore(status(gravity)).build());

        boolean visible = stand.isVisible();
        inv.setItem(SLOT_VISIBLE, ItemBuilder.of(Material.GLASS_PANE)
                .name(plugin.getConfigManager().getMessage("armorstand.visible", "&6Visible"))
                .lore(status(visible)).build());

        boolean small = stand.isSmall();
        inv.setItem(SLOT_SMALL, ItemBuilder.of(Material.SLIME_BALL)
                .name(plugin.getConfigManager().getMessage("armorstand.small", "&6Small"))
                .lore(status(small)).build());

        boolean glow = stand.isGlowing();
        inv.setItem(SLOT_GLOW, ItemBuilder.of(Material.GLOWSTONE_DUST)
                .name(plugin.getConfigManager().getMessage("armorstand.glow", "&6Glow"))
                .lore(status(glow)).build());

        inv.setItem(SLOT_RESET, ItemBuilder.of(Material.BARRIER)
                .name(plugin.getConfigManager().getMessage("armorstand.reset", "&cReset Pose")).build());

        inv.setItem(SLOT_ROTATE_L, ItemBuilder.of(Material.FEATHER)
                .name(plugin.getConfigManager().getMessage("armorstand.rotate-left", "&eRotate Left")).build());

        inv.setItem(SLOT_ROTATE_R, ItemBuilder.of(Material.FEATHER)
                .name(plugin.getConfigManager().getMessage("armorstand.rotate-right", "&eRotate Right")).build());

        inv.setItem(SLOT_COPY, ItemBuilder.of(Material.BOOK)
                .name(plugin.getConfigManager().getMessage("armorstand.copy-pose", "&aCopy Pose")).build());

        inv.setItem(SLOT_PASTE, ItemBuilder.of(Material.WRITABLE_BOOK)
                .name(plugin.getConfigManager().getMessage("armorstand.paste-pose", "&aPaste Pose")).build());

        inv.setItem(SLOT_CLOSE, ItemBuilder.of(Material.EMERALD)
                .name(plugin.getConfigManager().getMessage("armorstand.close", "&aClose")).build());

        player.openInventory(inv);
    }

    private boolean isOptionSlot(int slot) {
        return slot == SLOT_ARMS || slot == SLOT_BASE_PLATE || slot == SLOT_GRAVITY
                || slot == SLOT_VISIBLE || slot == SLOT_SMALL || slot == SLOT_GLOW
                || slot == SLOT_RESET || slot == SLOT_ROTATE_L || slot == SLOT_ROTATE_R
                || slot == SLOT_COPY || slot == SLOT_PASTE || slot == SLOT_CLOSE;
    }

    private String status(boolean enabled) {
        return enabled
                ? "&a\u2713 Enabled"
                : "&c\u2717 Disabled";
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
            case SLOT_ARMS -> {
                stand.setArms(!stand.hasArms());
                refreshGUI(player, stand);
            }
            case SLOT_BASE_PLATE -> {
                stand.setBasePlate(!stand.hasBasePlate());
                refreshGUI(player, stand);
            }
            case SLOT_GRAVITY -> {
                stand.setGravity(!stand.hasGravity());
                refreshGUI(player, stand);
            }
            case SLOT_VISIBLE -> {
                stand.setVisible(!stand.isVisible());
                refreshGUI(player, stand);
            }
            case SLOT_SMALL -> {
                stand.setSmall(!stand.isSmall());
                refreshGUI(player, stand);
            }
            case SLOT_GLOW -> {
                stand.setGlowing(!stand.isGlowing());
                refreshGUI(player, stand);
            }
            case SLOT_RESET -> {
                stand.setHeadPose(EulerAngle.ZERO);
                stand.setBodyPose(EulerAngle.ZERO);
                stand.setLeftArmPose(new EulerAngle(Math.toRadians(345), 0, 0));
                stand.setRightArmPose(new EulerAngle(Math.toRadians(345), 0, 0));
                stand.setLeftLegPose(EulerAngle.ZERO);
                stand.setRightLegPose(EulerAngle.ZERO);
                refreshGUI(player, stand);
            }
            case SLOT_ROTATE_L -> {
                stand.setRotation(stand.getLocation().getYaw() - 45f, 0f);
                refreshGUI(player, stand);
            }
            case SLOT_ROTATE_R -> {
                stand.setRotation(stand.getLocation().getYaw() + 45f, 0f);
                refreshGUI(player, stand);
            }
            case SLOT_COPY -> {
                copiedPose.put(player.getUniqueId(), new ArmorStandPose(
                        stand.getHeadPose(), stand.getBodyPose(),
                        stand.getLeftArmPose(), stand.getRightArmPose(),
                        stand.getLeftLegPose(), stand.getRightLegPose()
                ));
                player.sendMessage(plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().getPrefixedMessage("armorstand.pose-copied")));
            }
            case SLOT_PASTE -> {
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
            case SLOT_CLOSE -> {
                editing.remove(player.getUniqueId());
                player.closeInventory();
            }
        }
    }

    private void refreshGUI(Player player, ArmorStand stand) {
        Bukkit.getScheduler().runTask(plugin, () -> openEditor(player, stand));
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
