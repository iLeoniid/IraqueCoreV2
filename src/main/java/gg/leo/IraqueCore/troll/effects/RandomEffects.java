package gg.leo.IraqueCore.troll.effects;

import gg.leo.IraqueCore.troll.TrollEffect;
import gg.leo.IraqueCore.troll.TrollManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Random;

public final class RandomEffects {

    private static final Random RANDOM = new Random();

    private RandomEffects() {}

    public static void register(TrollManager manager) {
        manager.registerEffect(new AnnoyEffect());
        manager.registerEffect(new BurnEffect());
        manager.registerEffect(new StarveEffect());
        manager.registerEffect(new HideAllPlayersEffect());
        manager.registerEffect(new PoopEffect());
        manager.registerEffect(new PotatoEffect());
        manager.registerEffect(new RingOfFireEffect());
        manager.registerEffect(new SilverfishEffect());
        manager.registerEffect(new SlipperyHandsEffect());
        manager.registerEffect(new CaveSoundsEffect());
        manager.registerEffect(new GhastSoundsEffect());
        manager.registerEffect(new TimeFlashEffect());
        manager.registerEffect(new RandomTPEffect());
        manager.registerEffect(new RandomParticleEffect());
        manager.registerEffect(new LaunchEffect());
        manager.registerEffect(new FreeFallEffect());
        manager.registerEffect(new AllEntitiesDieEffect());
    }

    private static class AnnoyEffect extends TrollEffect {
        AnnoyEffect() {
            super("annoy", "&eMolestia", Material.VILLAGER_SPAWN_EGG,
                    Arrays.asList("&7Reproduce sonidos molestos", "&7de aldeano repetidamente.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.annoy", 14, 45);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            playAnnoy(target);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 8; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            playAnnoy(target);
        }

        private void playAnnoy(Player target) {
            Location loc = target.getLocation();
            target.playSound(loc, Sound.ENTITY_VILLAGER_AMBIENT, 2, 1);
            target.playSound(loc, Sound.ENTITY_VILLAGER_CELEBRATE, 2, 0.5f);
            target.playSound(loc, Sound.ENTITY_VILLAGER_NO, 2, 1.5f);
        }
    }

    private static class BurnEffect extends TrollEffect {
        BurnEffect() {
            super("burn", "&4Quemar", Material.FLINT_AND_STEEL,
                    Arrays.asList("&7Prende fuego al jugador", "&7constantemente.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.burn", 10, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.setFireTicks(1000000);
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.setFireTicks(0);
        }

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 40; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            if (target.getFireTicks() <= 0) {
                target.setFireTicks(1000000);
            }
        }
    }

    private static class StarveEffect extends TrollEffect {
        StarveEffect() {
            super("starve", "&6Hambre Extrema", Material.ROTTEN_FLESH,
                    Arrays.asList("&7Aplica hambre extrema", "&7al jugador.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.starve", 10, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 999999, 50, false, false));
            target.setFoodLevel(0);
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.removePotionEffect(PotionEffectType.HUNGER);
            target.setFoodLevel(20);
        }
    }

    private static class HideAllPlayersEffect extends TrollEffect {
        HideAllPlayersEffect() {
            super("hide-players", "&7Ocultar Jugadores", Material.INK_SAC,
                    Arrays.asList("&7Oculta todos los jugadores", "&7de la vista de la victima.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.hide-players", 15, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            org.bukkit.Bukkit.getOnlinePlayers().forEach(p -> {
                if (!p.equals(target)) {
                    target.hidePlayer(gg.leo.IraqueCore.IraqueCore.getInstance(), p);
                }
            });
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            org.bukkit.Bukkit.getOnlinePlayers().forEach(p -> {
                if (!p.equals(target)) {
                    target.showPlayer(gg.leo.IraqueCore.IraqueCore.getInstance(), p);
                }
            });
        }
    }

    private static class PoopEffect extends TrollEffect {
        PoopEffect() {
            super("poop", "&6CACA", Material.COCOA_BEANS,
                    Arrays.asList("&7Caca de cocoa beans al", "&7agacharse.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.poop", 15, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            manager.getPlugin().getTrollEventListener().poopPlayers.add(target.getUniqueId());
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            manager.getPlugin().getTrollEventListener().poopPlayers.remove(target.getUniqueId());
        }
    }

    private static class PotatoEffect extends TrollEffect {
        PotatoEffect() {
            super("potato", "&ePapa Invisible", Material.POTATO,
                    Arrays.asList("&7Vuelve invisible al jugador", "&7y dropea papas.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.potato", 18, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.setInvisible(true);
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.setInvisible(false);
            target.getWorld().getEntitiesByClass(org.bukkit.entity.Item.class).forEach(item -> {
                if (item.getItemStack().getType() == Material.POTATO) item.remove();
            });
        }

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 8; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            Location loc = target.getLocation();
            target.getWorld().dropItemNaturally(loc, new org.bukkit.inventory.ItemStack(Material.POTATO, 2));
        }
    }

    private static class RingOfFireEffect extends TrollEffect {
        private int tick = 0;

        RingOfFireEffect() {
            super("ring-of-fire", "&4Anillo de Fuego", Material.BLAZE_POWDER,
                    Arrays.asList("&7Rodea al jugador con un", "&7anillo de fuego y particulas.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.ring-of-fire", 12, 90);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            tick = 0;
            target.setFireTicks(1000000);
            target.getWorld().strikeLightning(target.getLocation());
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.setFireTicks(0);
        }

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 2; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            tick++;
            Location loc = target.getLocation().add(0, 1, 0);
            target.setFireTicks(1000);
            double radius = 2.5;
            for (int i = 0; i < 8; i++) {
                double angle = (2 * Math.PI * i / 8) + (tick * 0.2);
                double x = loc.getX() + Math.cos(angle) * radius;
                double z = loc.getZ() + Math.sin(angle) * radius;
                Location pLoc = new Location(loc.getWorld(), x, loc.getY(), z);
                loc.getWorld().spawnParticle(Particle.FLAME, pLoc, 1, 0, 0, 0, 0);
            }
        }
    }

    private static class SilverfishEffect extends TrollEffect {
        SilverfishEffect() {
            super("silverfish", "&8Plaga de Silverfish", Material.INFESTED_STONE,
                    Arrays.asList("&7Invocas 50 silverfish", "&7que atacan al jugador.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.silverfish", 12, 120);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            Location loc = target.getLocation();
            for (int i = 0; i < 50; i++) {
                Location spawnLoc = loc.clone().add(
                        RANDOM.nextDouble() * 10 - 5,
                        0,
                        RANDOM.nextDouble() * 10 - 5);
                org.bukkit.entity.Silverfish fish = (org.bukkit.entity.Silverfish)
                        spawnLoc.getWorld().spawn(spawnLoc, org.bukkit.entity.Silverfish.class);
                fish.setTarget(target);
                fish.setInvulnerable(true);
            }
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.getWorld().getEntitiesByClass(org.bukkit.entity.Silverfish.class).forEach(e -> {
                if (e.getTarget() == target) e.remove();
            });
        }
    }

    private static class SlipperyHandsEffect extends TrollEffect {
        SlipperyHandsEffect() {
            super("slippery-hands", "&bManos Resbaladizas", Material.SLIME_BALL,
                    Arrays.asList("&7Suelta el item en la mano", "&7cada 2 segundos.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.slippery-hands", 14, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            dropItem(target);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 40; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            dropItem(target);
        }

        private void dropItem(Player target) {
            org.bukkit.inventory.ItemStack held = target.getInventory().getItemInMainHand();
            if (held.getType() != Material.AIR) {
                target.getWorld().dropItemNaturally(target.getLocation(), held);
                target.getInventory().setItemInMainHand(null);
            }
        }
    }

    private static class CaveSoundsEffect extends TrollEffect {
        CaveSoundsEffect() {
            super("cave-sounds", "&8Sonidos de Cueva", Material.SCULK_SHRIEKER,
                    Arrays.asList("&7Reproduce sonidos de cueva", "&7espeluznantes.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.cave-sounds", 12, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.playSound(target.getLocation(), Sound.AMBIENT_CAVE, 2, 1);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 10; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            target.playSound(target.getLocation(), Sound.AMBIENT_CAVE, 2, 1 + RANDOM.nextFloat());
        }
    }

    private static class GhastSoundsEffect extends TrollEffect {
        GhastSoundsEffect() {
            super("ghast-sounds", "&fSonidos de Ghast", Material.GHAST_SPAWN_EGG,
                    Arrays.asList("&7Reproduce sonidos de ghast", "&7aletratorios.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.ghast-sounds", 12, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.playSound(target.getLocation(), Sound.ENTITY_GHAST_AMBIENT, 2, 1);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 40; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            target.playSound(target.getLocation(), Sound.ENTITY_GHAST_AMBIENT, 2, 0.5f + RANDOM.nextFloat());
        }
    }

    private static class TimeFlashEffect extends TrollEffect {
        TimeFlashEffect() {
            super("time-flash", "&bDestello Temporal", Material.CLOCK,
                    Arrays.asList("&7Cambia el tiempo y aplica", "&7ceguera pulsante.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.time-flash", 12, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.setPlayerTime(0, false);
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 100, false, false));
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.removePotionEffect(PotionEffectType.BLINDNESS);
            target.resetPlayerTime();
        }

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 30; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 35, 100, false, false));
        }
    }

    private static class RandomTPEffect extends TrollEffect {
        RandomTPEffect() {
            super("random-tp-world", "&3TP Aleatorio", Material.ENDER_PEARL,
                    Arrays.asList("&7Teletransporta al jugador", "&7a una ubicacion aleatoria.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.random-tp-world", 0, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            Location loc = target.getLocation();
            double x = RANDOM.nextDouble() * 2000 - 1000;
            double z = RANDOM.nextDouble() * 2000 - 1000;
            Location targetLoc = new Location(loc.getWorld(), x, 255, z);
            targetLoc = targetLoc.getWorld().getHighestBlockAt(targetLoc).getLocation().add(0, 1, 0);
            target.teleport(targetLoc);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }

    private static class RandomParticleEffect extends TrollEffect {
        private static final Particle[] PARTICLES = {
                Particle.CLOUD, Particle.LAVA, Particle.SMOKE, Particle.EXPLOSION,
                Particle.PORTAL, Particle.BUBBLE_POP, Particle.CRIT, Particle.HEART,
                Particle.FLAME, Particle.END_ROD, Particle.WITCH
        };

        RandomParticleEffect() {
            super("random-particle", "&dParticulas Aleatorias", Material.FIREWORK_ROCKET,
                    Arrays.asList("&7Particulas aleatorias alrededor", "&7del jugador.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.random-particle", 14, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {}

        @Override
        public void revert(Player target, TrollManager manager) {}

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 15; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            Location loc = target.getLocation().add(0, 1, 0);
            Particle particle = PARTICLES[RANDOM.nextInt(PARTICLES.length)];
            loc.getWorld().spawnParticle(particle, loc, 10, 1, 1, 1, 0.05);
        }
    }

    private static class LaunchEffect extends TrollEffect {
        LaunchEffect() {
            super("launch", "&aLanzar al Cielo", Material.FIREWORK_ROCKET,
                    Arrays.asList("&7Lanza al jugador hacia", "&7arriba con gran velocidad.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.launch", 0, 30);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.setVelocity(new org.bukkit.util.Vector(0, 5, 0));
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_UP, 2, 1);
            target.getWorld().spawnParticle(Particle.CLOUD, target.getLocation(), 30, 1, 1, 1, 0.1);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }

    private static class FreeFallEffect extends TrollEffect {
        FreeFallEffect() {
            super("free-fall", "&bCaida Libre", Material.FEATHER,
                    Arrays.asList("&7Teletransporta al jugador", "&7muy alto y cae en caida libre.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.free-fall", 0, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            Location loc = target.getLocation();
            loc.setY(loc.getY() + 1000);
            target.teleport(loc);
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 2, 1);
            target.getWorld().spawnParticle(Particle.BUBBLE_POP, target.getLocation(), 50, 2, 2, 2, 0.1);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }

    private static class AllEntitiesDieEffect extends TrollEffect {
        AllEntitiesDieEffect() {
            super("all-entities-die", "&cMuerte de Entidades", Material.BONE,
                    Arrays.asList("&7Mata todas las entidades", "&7en un radio de 10 bloques.", "", "&e\u25B8 Click para aplicar"),
                    "random", "troll.effect.all-entities-die", 0, 30);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.getWorld().getNearbyEntities(target.getLocation(), 10, 10, 10).forEach(e -> {
                if (e instanceof org.bukkit.entity.Mob) {
                    e.remove();
                }
            });
            target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation(), 20, 3, 3, 3, 0.1);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_DONKEY_DEATH, 2, 1);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }
}
