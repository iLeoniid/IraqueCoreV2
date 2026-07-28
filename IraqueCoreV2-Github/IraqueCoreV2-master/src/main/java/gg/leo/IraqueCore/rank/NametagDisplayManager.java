package gg.leo.IraqueCore.rank;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NametagDisplayManager {

    private final IraqueCore plugin;
    // playerUUID -> displayEntityUUID
    private final Map<UUID, UUID> displays = new HashMap<>();

    // El pasajero de un jugador queda posicionado aprox. a la altura de la
    // cabeza; este offset lo sube un poco para que no se solape.
    private static final float Y_OFFSET = 0.35f;

    public NametagDisplayManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Crea o actualiza el nametag flotante de un jugador con un Component
     * completo (soporta hex). Se llama cada vez que cambia rango/tag/afk.
     */
    public void updateNametag(Player player, Component text) {
        TextDisplay display = getOrCreateDisplay(player);
        if (display != null) {
            display.text(text);
        }
    }

    private TextDisplay getOrCreateDisplay(Player player) {
        UUID existingId = displays.get(player.getUniqueId());
        if (existingId != null) {
            Entity entity = plugin.getServer().getEntity(existingId);
            if (entity instanceof TextDisplay td && !td.isDead()) {
                if (!player.getPassengers().contains(td)) {
                    player.addPassenger(td);
                }
                return td;
            }
            displays.remove(player.getUniqueId());
        }
        return spawnDisplay(player);
    }

    private TextDisplay spawnDisplay(Player player) {
        Location loc = player.getLocation();

        TextDisplay display = player.getWorld().spawn(loc, TextDisplay.class, td -> {
            td.setBillboard(Display.Billboard.CENTER);
            td.setShadowed(false);
            td.setSeeThrough(false);
            td.setDefaultBackground(false);
            td.setPersistent(false); // no se guarda en el mundo al reiniciar
            td.setTransformation(new Transformation(
                    new Vector3f(0f, Y_OFFSET, 0f),
                    new AxisAngle4f(0, 0, 0, 1),
                    new Vector3f(1f, 1f, 1f),
                    new AxisAngle4f(0, 0, 0, 1)
            ));
        });

        player.addPassenger(display);
        displays.put(player.getUniqueId(), display.getUniqueId());
        return display;
    }

    /** Fuerza a re-montar el display si se desmontó (teleport, respawn, etc.) */
    public void reattach(Player player) {
        UUID id = displays.get(player.getUniqueId());
        if (id == null) return;
        Entity entity = plugin.getServer().getEntity(id);
        if (entity instanceof TextDisplay td && !td.isDead()) {
            if (!player.getPassengers().contains(td)) {
                player.addPassenger(td);
            }
        }
    }

    /** Elimina el display (usar en quit) */
    public void remove(Player player) {
        UUID id = displays.remove(player.getUniqueId());
        if (id == null) return;
        Entity entity = plugin.getServer().getEntity(id);
        if (entity != null) entity.remove();
    }
}