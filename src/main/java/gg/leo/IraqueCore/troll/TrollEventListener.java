package gg.leo.IraqueCore.troll;

import gg.leo.IraqueCore.utils.SchedulerUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class TrollEventListener implements Listener {

    private final TrollManager manager;
    private final Random random = new Random();

    public final Set<UUID> frozenPlayers = new HashSet<>();
    public final Set<UUID> breakPlayers = new HashSet<>();
    public final Set<UUID> deafenedPlayers = new HashSet<>();
    public final Set<UUID> explodeOnChatPlayers = new HashSet<>();
    public final Set<UUID> randomChatPlayers = new HashSet<>();
    public final Set<UUID> reverseChatPlayers = new HashSet<>();
    public final Set<UUID> bedExplodePlayers = new HashSet<>();
    public final Set<UUID> stopSleepPlayers = new HashSet<>();
    public final Set<UUID> tntPlacePlayers = new HashSet<>();
    public final Set<UUID> lightningPlayers = new HashSet<>();
    public final Set<UUID> forceJumpPlayers = new HashSet<>();
    public final Set<UUID> sneakDestroyPlayers = new HashSet<>();
    public final Set<UUID> poopPlayers = new HashSet<>();
    public final Set<UUID> instaToolBreakPlayers = new HashSet<>();
    public final Set<UUID> aquaphobiaPlayers = new HashSet<>();
    public final Set<UUID> inventoryStopPlayers = new HashSet<>();
    public final Set<UUID> randomInvPlayers = new HashSet<>();
    public final Set<UUID> entityMultiplyPlayers = new HashSet<>();

    public TrollEventListener(TrollManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        UUID id = p.getUniqueId();

        if (frozenPlayers.contains(id)) {
            event.setTo(event.getFrom());
            return;
        }

        if (lightningPlayers.contains(id)) {
            if (event.getFrom().distanceSquared(event.getTo()) > 0.01) {
                p.getWorld().strikeLightningEffect(p.getLocation());
            }
            return;
        }

        if (forceJumpPlayers.contains(id)) {
            if (event.getFrom().distanceSquared(event.getTo()) > 0.01) {
                p.setVelocity(p.getVelocity().setY(1.0));
            }
            return;
        }

        if (aquaphobiaPlayers.contains(id)) {
            Block block = p.getLocation().getBlock();
            if (block.getType() == Material.WATER || block.getType() == Material.LAVA) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 4, false, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1, false, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 2, false, false));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        UUID id = p.getUniqueId();
        if (breakPlayers.contains(id)) {
            event.setCancelled(true);
        }
        if (instaToolBreakPlayers.contains(id)) {
            event.getPlayer().getInventory().getItemInMainHand().setType(Material.AIR);
            p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1, 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        UUID id = p.getUniqueId();
        if (breakPlayers.contains(id)) {
            event.setCancelled(true);
            return;
        }
        if (tntPlacePlayers.contains(id)) {
            event.setCancelled(true);
            Location loc = event.getBlock().getLocation();
            TNTPrimed tnt = loc.getWorld().spawn(loc, TNTPrimed.class);
            tnt.setYield(0f);
            tnt.setIsIncendiary(false);
            tnt.setMetadata("troll_tnt", new FixedMetadataValue(manager.getPlugin(), true));
            loc.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player p = event.getPlayer();
        UUID id = p.getUniqueId();

        if (deafenedPlayers.contains(id)) {
            event.setCancelled(true);
            return;
        }

        if (explodeOnChatPlayers.contains(id)) {
            event.setCancelled(true);
            p.getWorld().createExplosion(p.getLocation(), 0f, false, false);
            p.getWorld().spawnParticle(Particle.EXPLOSION, p.getLocation(), 3, 0.5, 0.5, 0.5, 0);
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
            return;
        }

        if (randomChatPlayers.contains(id)) {
            String msg = manager.getPlugin().getConfig().getStringList("troll.troll-config.randomchat")
                    .stream().findAny().orElse("?");
            if (msg.isEmpty()) msg = "?";
            event.message(Component.text(msg));
            return;
        }

        if (reverseChatPlayers.contains(id)) {
            String plain = PlainTextComponentSerializer.plainText().serialize(event.message());
            String reversed = new StringBuilder(plain).reverse().toString();
            event.message(Component.text(reversed));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedEnter(PlayerBedEnterEvent event) {
        Player p = event.getPlayer();
        UUID id = p.getUniqueId();
        if (bedExplodePlayers.contains(id)) {
            Location loc = event.getBed().getLocation();
            p.getWorld().createExplosion(loc, 0f, false, false);
            p.getWorld().spawnParticle(Particle.EXPLOSION, loc, 3, 0.5, 0.5, 0.5, 0);
            p.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
            return;
        }
        if (stopSleepPlayers.contains(id)) {
            event.setCancelled(true);
            p.sendActionBar(Component.text("You may not rest now; there are monsters nearby"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSneak(PlayerToggleSneakEvent event) {
        Player p = event.getPlayer();
        UUID id = p.getUniqueId();
        if (sneakDestroyPlayers.contains(id)) {
            Block below = p.getLocation().subtract(0, 1, 0).getBlock();
            p.spawnParticle(Particle.BLOCK, below.getLocation().add(0.5, 0.5, 0.5), 12,
                    0.3, 0.3, 0.3, 0.1, below.getBlockData());
            p.getWorld().playSound(below.getLocation(), below.getBlockData().getSoundGroup().getBreakSound(), 1, 1);
        }
        if (poopPlayers.contains(id)) {
            p.getWorld().dropItemNaturally(p.getEyeLocation(), new org.bukkit.inventory.ItemStack(Material.COCOA_BEANS));
            p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_FOX_AGGRO, 1, 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (inventoryStopPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (randomInvPlayers.contains(event.getPlayer().getUniqueId())) {
            Player p = (Player) event.getPlayer();
            SchedulerUtil.runSync(manager.getPlugin(), () -> {
                if (p.isOnline() && randomInvPlayers.contains(p.getUniqueId())) {
                    p.closeInventory();
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        frozenPlayers.remove(id);
        breakPlayers.remove(id);
        deafenedPlayers.remove(id);
        explodeOnChatPlayers.remove(id);
        randomChatPlayers.remove(id);
        reverseChatPlayers.remove(id);
        bedExplodePlayers.remove(id);
        stopSleepPlayers.remove(id);
        tntPlacePlayers.remove(id);
        lightningPlayers.remove(id);
        forceJumpPlayers.remove(id);
        sneakDestroyPlayers.remove(id);
        poopPlayers.remove(id);
        instaToolBreakPlayers.remove(id);
        aquaphobiaPlayers.remove(id);
        inventoryStopPlayers.remove(id);
        randomInvPlayers.remove(id);
        entityMultiplyPlayers.remove(id);
        manager.endSession(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().hasMetadata("troll_fake")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK) {
            Location loc = event.getEntity().getLocation();
            for (Entity e : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                if (e instanceof org.bukkit.entity.FallingBlock && e.hasMetadata("troll_fake")) {
                    event.setCancelled(true);
                    e.remove();
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity().hasMetadata("troll_fake")) {
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (entity != null && entity.hasMetadata("troll_tnt")) {
            event.blockList().clear();
            event.setYield(0f);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().hasMetadata("troll_fake")) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            return;
        }
        if (event.getEntity().getKiller() != null) {
            UUID id = event.getEntity().getKiller().getUniqueId();
            if (entityMultiplyPlayers.contains(id)) {
                for (int i = 0; i < 2; i++) {
                    Location loc = event.getEntity().getLocation();
                    loc.getWorld().spawn(loc, event.getEntityType().getEntityClass());
                }
            }
        }
    }
}
