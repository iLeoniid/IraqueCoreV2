package gg.leo.IraqueCore.troll.effects;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.troll.TrollEffect;
import gg.leo.IraqueCore.troll.TrollManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class CombatWorldEffects {

    private static final Random RANDOM = new Random();

    private CombatWorldEffects() {}

    public static void register(TrollManager manager) {
        manager.registerEffect(new FakeMobsEffect());
        manager.registerEffect(new ArrowRainEffect());
        manager.registerEffect(new FakeExplosionEffect());
    }

    private static class FakeMobsEffect extends TrollEffect {
        FakeMobsEffect() {
            super("fake-mobs", "&cMobs Falsos", Material.ZOMBIE_HEAD,
                    List.of("&7Rodea al jugador de mobs", "&7inofensivos.", "", "&e\u25B8 Click para aplicar"),
                    "combatworld", "troll.effect.fake-mobs", 15, 90);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            Location loc = target.getLocation();
            List<LivingEntity> mobs = new ArrayList<>();
            EntityType[] types = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER,
                    EntityType.SPIDER, EntityType.WITCH, EntityType.ENDERMAN};

            for (int i = 0; i < 3 + RANDOM.nextInt(3); i++) {
                double angle = 2 * Math.PI * i / 4;
                double x = loc.getX() + Math.cos(angle) * (2 + RANDOM.nextDouble());
                double z = loc.getZ() + Math.sin(angle) * (2 + RANDOM.nextDouble());
                Location spawnLoc = new Location(loc.getWorld(), x, loc.getY(), z, 0, 0);

                spawnLoc.getWorld().spawnEntity(spawnLoc, types[RANDOM.nextInt(types.length)]).setMetadata(
                        "troll_mob", new FixedMetadataValue(IraqueCore.getInstance(), target.getUniqueId().toString()));
            }

            manager.addRevertTask(target, () -> {
                loc.getWorld().getEntities().forEach(entity -> {
                    if (entity.hasMetadata("troll_mob")) {
                        String owner = entity.getMetadata("troll_mob").get(0).asString();
                        if (owner.equals(target.getUniqueId().toString())) {
                            entity.remove();
                        }
                    }
                });
            });
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }

    private static class ArrowRainEffect extends TrollEffect {
        ArrowRainEffect() {
            super("arrow-rain", "&aLluvia de Flechas", Material.ARROW,
                    List.of("&7Particulas de flechas + sonido", "&7(Sin daño real, configurable).", "", "&e\u25B8 Click para aplicar"),
                    "combatworld", "troll.effect.arrow-rain", 8, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            doArrowEffect(target);
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
            doArrowEffect(target);
        }

        private void doArrowEffect(Player target) {
            Location loc = target.getLocation().add(0, 5, 0);
            loc.getWorld().spawnParticle(Particle.ITEM, loc, 5, 2, 0.5, 2, 0.1,
                    new org.bukkit.inventory.ItemStack(Material.ARROW));
            loc.getWorld().playSound(target.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1.0f);
            loc.getWorld().playSound(target.getLocation(), Sound.ENTITY_ARROW_HIT, 0.3f, 0.8f);
        }
    }

    private static class FakeExplosionEffect extends TrollEffect {
        FakeExplosionEffect() {
            super("fake-explosion", "&6Fake Explosion", Material.TNT,
                    List.of("&7Particulas de explosion + sonido", "&7(Sin destruir bloques).", "", "&e\u25B8 Click para aplicar"),
                    "combatworld", "troll.effect.fake-explosion", 0, 30);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            Location loc = target.getLocation();
            loc.getWorld().createExplosion(loc, 0f, false, false);
            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 10, 2, 1, 2, 0.1);
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 20, 2, 1, 2, 0.05);
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }
}
