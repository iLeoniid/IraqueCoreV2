package gg.leo.IraqueCore.armorstand;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.armorstand.menu.ArmorStandMenu;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmorStandEditor implements Listener {

    private final IraqueCore plugin;
    private final Map<UUID, ArmorStand> editing = new HashMap<>();
    public final Map<UUID, ArmorStandPose> copiedPose = new HashMap<>();

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
        editing.put(player.getUniqueId(), stand);
        new ArmorStandMenu(plugin, player, stand, this).openMenu();
    }

    public void cleanup(Player player) {
        editing.remove(player.getUniqueId());
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
