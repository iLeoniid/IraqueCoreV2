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
            super("annoy", "&eIncomodo", Material.VILLAGER_SPAWN_EGG,
                    Arrays.asList("&7Reproduz sons irritantes", "&7de aldeao repetidamente.", "", "&e\u25B8 Clique para aplicar"),
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
            super("burn", "&4Queimar", Material.FLINT_AND_STEEL,
                    Arrays.asList("&7Poes fogo no jogador", "&7constantemente.", "", "&e\u25B8 Clique para aplicar"),
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
            super("starve", "&6Fome Extrema", Material.ROTTEN_FLESH,
                    Arrays.asList("&7Aplica fome extrema", "&7ao jogador.", "", "&e\u25B8 Clique para aplicar"),
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
            super("hide-players", "&7Ocultar Jogadores", Material.INK_SAC,
                    Arrays.asList("&7Oculta todos os jogadores", "&7da vista da vitima.", "", "&e\u25B8 Clique para aplicar"),
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
                    Arrays.asList("&7Solta cocoa beans ao", "&7se agachar.", "", "&e\u25B8 Clique para aplicar"),
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
            super("potato", "&eBatata Invisivel", Material.POTATO,
                    Arrays.asList("&7Torna o jogador invisivel", "&7e solta batatas.", "", "&e\u25B8 Clique para aplicar"),
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
            super("ring-of-fire", "&4Anel de Fogo", Material.BLAZE_POWDER,
                    Arrays.asList("&7Cerca o jogador com um", "&7anel de fogo e particulas.", "", "&e\u25B8 Clique para aplicar"),
                    "random", "troll.effect.ring-of-fire", 12, 90);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            tick = 0;
            target.setFireTicks(1000000);
            target.getWorld().strikeLightningEffect(target.getLocation());
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
            super("silverfish", "&8Praga de Silverfish", Material.INFESTED_STONE,
                    Arrays.asList("&7Invoca 50 silverfish", "&7que atacam o jogador.", "", "&e\u25B8 Clique para aplicar"),
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
                fish.setMetadata("troll_fake",
                        new org.bukkit.metadata.FixedMetadataValue(
                                gg.leo.IraqueCore.IraqueCore.getInstance(), true));
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
            super("slippery-hands", "&bMaos Escorregadias", Material.SLIME_BALL,
                    Arrays.asList("&7Solta o item na mao", "&7a cada 2 segundos.", "", "&e\u25B8 Clique para aplicar"),
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
            super("cave-sounds", "&8Sons de Caverna", Material.SCULK_SHRIEKER,
                    Arrays.asList("&7Reproduz sons de caverna", "&7assustadores.", "", "&e\u25B8 Clique para aplicar"),
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
            super("ghast-sounds", "&fSons de Ghast", Material.GHAST_SPAWN_EGG,
                    Arrays.asList("&7Reproduz sons de ghast", "&7aleatorios.", "", "&e\u25B8 Clique para aplicar"),
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
            super("time-flash", "&bLampejo Temporal", Material.CLOCK,
                    Arrays.asList("&7Altera o tempo e aplica", "&7cegueira pulsante.", "", "&e\u25B8 Clique para aplicar"),
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
                    Arrays.asList("&7Teletransporta o jogador", "&7para uma localizacao aleatoria.", "", "&e\u25B8 Clique para aplicar"),
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
                    Arrays.asList("&7Particulas aleatorias ao redor", "&7do jogador.", "", "&e\u25B8 Clique para aplicar"),
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
            super("launch", "&aLancar ao Ceu", Material.FIREWORK_ROCKET,
                    Arrays.asList("&7Lanca o jogador para", "&7cima com grande velocidade.", "", "&e\u25B8 Clique para aplicar"),
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
            super("free-fall", "&bQueda Livre", Material.FEATHER,
                    Arrays.asList("&7Teletransporta o jogador", "&7bem alto e cai em queda livre.", "", "&e\u25B8 Clique para aplicar"),
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
            super("all-entities-die", "&cMorte de Entidades", Material.BONE,
                    Arrays.asList("&7Mata todas as entidades", "&7em um raio de 10 blocos.", "", "&e\u25B8 Clique para aplicar"),
                    "random", "troll.effect.all-entities-die", 0, 30);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            Location loc = target.getLocation();
            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 20, 3, 3, 3, 0.1);
            loc.getWorld().spawnParticle(Particle.ASH, loc, 50, 3, 3, 3, 0.1);
            loc.getWorld().playSound(loc, Sound.ENTITY_DONKEY_DEATH, 2, 1);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }
}
