package gg.leo.IraqueCore.grave;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GraveListener implements Listener {

    private final IraqueCore plugin;
    private final Map<Location, GraveData> graves = new ConcurrentHashMap<>();
    private final NamespacedKey graveKey;
    private final NamespacedKey graveOwnerKey;

    // Config cache
    private final boolean allowOthersOpen;
    private final boolean dropOnExpire;
    private final boolean protectExplosions;
    private final boolean hologramEnabled;
    private final long expireTicks;
    private final String hologramFormat;
    private final String broadcastFormat;
    private final Material graveMaterial;

    public GraveListener(IraqueCore plugin) {
        this.plugin = plugin;
        this.graveKey = new NamespacedKey(plugin, "grave");
        this.graveOwnerKey = new NamespacedKey(plugin, "grave_owner");

        var cfg = plugin.getConfig();
        this.allowOthersOpen = cfg.getBoolean("grave.allow-others-open", false);
        this.dropOnExpire = cfg.getBoolean("grave.drop-on-expire", true);
        this.protectExplosions = cfg.getBoolean("grave.protect-explosions", true);
        this.hologramEnabled = cfg.getBoolean("grave.hologram.enabled", true);
        this.expireTicks = cfg.getLong("grave.expire-time", 600) * 20L;
        this.hologramFormat = cfg.getString("grave.hologram.format", "&6⚰ Cadaver de &e{player} &7(&f{time}&7)");
        this.broadcastFormat = cfg.getString("grave.broadcast", "&6{player} &emurió. Items en tumba: &6{x}, {y}, {z} &7- &f{world}");
        String mat = cfg.getString("grave.material", "CHEST").toUpperCase();
        this.graveMaterial = Material.getMaterial(mat) != null ? Material.getMaterial(mat) : Material.CHEST;

        startExpirationTask();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity(); // 1.26.2: getEntity() en vez de getPlayer()

        if (Boolean.TRUE.equals(player.getWorld().getGameRuleValue(org.bukkit.GameRule.KEEP_INVENTORY))) {
            return;
        }

        List<ItemStack> drops = new ArrayList<>(event.getDrops());
        if (drops.isEmpty()) return;

        event.getDrops().clear();
        event.setKeepInventory(false);

        Location loc = findSafeLocation(player.getLocation());
        if (loc == null) {
            drops.forEach(i -> { if (i != null) player.getWorld().dropItemNaturally(player.getLocation(), i); });
            return;
        }

        createGrave(player, loc, drops);
    }

    private void createGrave(Player player, Location loc, List<ItemStack> items) {
        Block block = loc.getBlock();
        block.setType(graveMaterial, false);

        // Orientar cofre hacia el jugador
        if (block.getBlockData() instanceof Directional dir) {
            dir.setFacing(getFacing(player.getLocation().getYaw()));
            block.setBlockData(dir, false);
        }

        // Doble cofre si >27 items
        boolean isDouble = items.size() > 27;
        Location secondLoc = null;

        if (isDouble && block.getBlockData() instanceof Directional dir) {
            secondLoc = loc.clone().add(getRight(dir.getFacing()));
            if (secondLoc.getBlock().getType().isAir()) {
                secondLoc.getBlock().setType(graveMaterial, false);
                org.bukkit.block.data.type.Chest c1 = (org.bukkit.block.data.type.Chest) block.getBlockData();
                org.bukkit.block.data.type.Chest c2 = (org.bukkit.block.data.type.Chest) secondLoc.getBlock().getBlockData();
                c1.setType(Type.LEFT);
                c2.setType(Type.RIGHT);
                block.setBlockData(c1, false);
                secondLoc.getBlock().setBlockData(c2, false);
            } else {
                isDouble = false;
            }
        }

        // Guardar items
        Inventory inv;
        if (!(block.getState() instanceof Chest chest)) {
            items.forEach(i -> block.getWorld().dropItemNaturally(block.getLocation(), i));
            return;
        }

        inv = isDouble && secondLoc != null
            ? ((DoubleChest) chest.getInventory().getHolder()).getInventory()
            : chest.getInventory();

        int slot = 0;
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR && slot < inv.getSize()) {
                inv.setItem(slot++, item);
            }
        }

        // Marcar con PDC
        markGrave(chest, player.getUniqueId());
        if (isDouble && secondLoc != null && secondLoc.getBlock().getState() instanceof Chest c2) {
            markGrave(c2, player.getUniqueId());
        }

        // Holograma
        ArmorStand holo = hologramEnabled
            ? createHologram(loc.clone().add(0.5, 1.3, 0.5), player.getName())
            : null;

        // Guardar datos
        GraveData data = new GraveData(player.getUniqueId(), player.getName(), loc, secondLoc, System.currentTimeMillis(), holo);
        graves.put(loc, data);
        if (secondLoc != null) graves.put(secondLoc, data);

        // Broadcast
        if (!broadcastFormat.isEmpty()) {
            Bukkit.broadcastMessage(ItemBuilder.color(broadcastFormat
                .replace("{player}", player.getName())
                .replace("{x}", String.valueOf(loc.getBlockX()))
                .replace("{y}", String.valueOf(loc.getBlockY()))
                .replace("{z}", String.valueOf(loc.getBlockZ()))
                .replace("{world}", loc.getWorld().getName())));
        }
    }

    private void markGrave(Chest chest, UUID owner) {
        PersistentDataContainer pdc = chest.getPersistentDataContainer();
        pdc.set(graveKey, PersistentDataType.BYTE, (byte) 1);
        pdc.set(graveOwnerKey, PersistentDataType.STRING, owner.toString());
        chest.update();
    }

    private ArmorStand createHologram(Location loc, String name) {
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setCustomNameVisible(true);
        stand.setInvulnerable(true);
        stand.setPersistent(false);
        stand.setSmall(true);
        stand.setArms(false);
        stand.setBasePlate(false);
        updateHologram(stand, name, expireTicks);
        return stand;
    }

    private void updateHologram(ArmorStand stand, String name, long ticks) {
        long sec = ticks / 20;
        stand.setCustomName(ItemBuilder.color(hologramFormat
            .replace("{player}", name)
            .replace("{time}", String.format("%02d:%02d", sec / 60, sec % 60))));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Block block = event.getClickedBlock();
        if (!isGrave(block)) return;

        Player player = event.getPlayer();
        GraveData data = graves.get(block.getLocation());
        if (data == null) return;

        if (!allowOthersOpen && !data.owner.equals(player.getUniqueId()) && !player.hasPermission("iraque.grave.admin")) {
            event.setCancelled(true);
            player.sendMessage(ItemBuilder.color(plugin.getConfigManager().getMessage("grave.no-permission", "&cNo puedes abrir la tumba de otro jugador.")));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        checkEmpty(event.getInventory());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        checkEmpty(event.getInventory());
    }

    private void checkEmpty(Inventory inv) {
        if (inv.getLocation() == null) return;
        Location loc = inv.getLocation().getBlock().getLocation();
        if (!graves.containsKey(loc)) return;
        if (isEmpty(inv)) removeGrave(loc, true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!isGrave(block)) return;

        Player player = event.getPlayer();
        GraveData data = graves.get(block.getLocation());
        if (data == null) { cleanBlock(block); return; }

        if (!data.owner.equals(player.getUniqueId()) && !player.hasPermission("iraque.grave.admin")) {
            event.setCancelled(true);
            player.sendMessage(ItemBuilder.color("&cNo puedes romper la tumba de otro jugador."));
            return;
        }

        // Dropear SOLO los items restantes, NO el cofre
        if (block.getState() instanceof Chest chest) {
            Inventory inv = data.secondLoc != null
                ? ((DoubleChest) chest.getInventory().getHolder()).getInventory()
                : chest.getInventory();
            for (ItemStack item : inv.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
            }
        }

        event.setDropItems(false);
        event.setExpToDrop(0);

        removeGrave(block.getLocation(), true);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (protectExplosions) event.blockList().removeIf(this::isGrave);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (protectExplosions) event.blockList().removeIf(this::isGrave);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity e : event.getChunk().getEntities()) {
            if (e instanceof ArmorStand stand && stand.isMarker() && !stand.hasGravity() && !stand.isVisible()) {
                boolean orphan = graves.values().stream().noneMatch(d -> d.hologram != null && d.hologram.equals(stand));
                if (orphan && stand.getCustomName() != null && stand.getCustomName().contains("⚰")) {
                    stand.remove();
                }
            }
        }
    }

    private boolean isGrave(Block block) {
        return block.getState() instanceof Chest chest
            && chest.getPersistentDataContainer().has(graveKey, PersistentDataType.BYTE);
    }

    private boolean isEmpty(Inventory inv) {
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() != Material.AIR) return false;
        }
        return true;
    }

    private void removeGrave(Location loc, boolean removeBlocks) {
        GraveData data = graves.remove(loc);
        if (data == null) return;

        if (data.secondLoc != null) {
            graves.remove(data.secondLoc);
            if (removeBlocks) cleanBlock(data.secondLoc.getBlock());
        }
        if (removeBlocks) cleanBlock(loc.getBlock());

        if (data.hologram != null && !data.hologram.isDead()) data.hologram.remove();
    }

    private void cleanBlock(Block block) {
        if (block.getState() instanceof Chest chest) {
            PersistentDataContainer pdc = chest.getPersistentDataContainer();
            pdc.remove(graveKey);
            pdc.remove(graveOwnerKey);
            chest.update();
        }
        block.setType(Material.AIR, true);
    }

    private Location findSafeLocation(Location origin) {
        for (int dy = 0; dy < 10; dy++) {
            Location check = origin.clone().add(0, dy, 0);
            if (check.getBlock().getType().isAir() && check.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                return check;
            }
        }
        for (int dy = -1; dy > -10; dy--) {
            Location check = origin.clone().add(0, dy, 0);
            if (check.getBlock().getType().isAir() && check.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                return check;
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Location check = origin.clone().add(dx, 0, dz);
                if (check.getBlock().getType().isAir() && check.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                    return check;
                }
            }
        }
        return null;
    }

    private org.bukkit.block.BlockFace getFacing(float yaw) {
        yaw = (yaw % 360 + 360) % 360;
        if (yaw < 45 || yaw >= 315) return org.bukkit.block.BlockFace.SOUTH;
        if (yaw < 135) return org.bukkit.block.BlockFace.WEST;
        if (yaw < 225) return org.bukkit.block.BlockFace.NORTH;
        return org.bukkit.block.BlockFace.EAST;
    }

    private Vector getRight(org.bukkit.block.BlockFace face) {
        return switch (face) {
            case NORTH -> new Vector(1, 0, 0);
            case SOUTH -> new Vector(-1, 0, 0);
            case EAST -> new Vector(0, 0, 1);
            case WEST -> new Vector(0, 0, -1);
            default -> new Vector(1, 0, 0);
        };
    }

    private void startExpirationTask() {
        if (expireTicks <= 0) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator<Map.Entry<Location, GraveData>> it = graves.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Location, GraveData> entry = it.next();
                    GraveData data = entry.getValue();
                    long elapsed = (now - data.creationTime) / 1000 * 20;

                    if (elapsed >= expireTicks) {
                        if (dropOnExpire) dropItems(data);
                        removeGrave(entry.getKey(), true);
                        it.remove();

                        Player owner = Bukkit.getPlayer(data.owner);
                        if (owner != null && owner.isOnline()) {
                            owner.sendMessage(ItemBuilder.color("&cTu tumba ha expirado."));
                        }
                    } else if (data.hologram != null && !data.hologram.isDead()) {
                        updateHologram(data.hologram, data.ownerName, expireTicks - elapsed);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void dropItems(GraveData data) {
        Block block = data.mainLoc.getBlock();
        if (!(block.getState() instanceof Chest chest)) return;
        Inventory inv = data.secondLoc != null
            ? ((DoubleChest) chest.getInventory().getHolder()).getInventory()
            : chest.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                block.getWorld().dropItemNaturally(block.getLocation(), item);
            }
        }
    }

    public void shutdown() {
        graves.values().forEach(d -> { if (d.hologram != null && !d.hologram.isDead()) d.hologram.remove(); });
        graves.clear();
    }

    private record GraveData(UUID owner, String ownerName, Location mainLoc, Location secondLoc,
                             long creationTime, ArmorStand hologram) {}
}