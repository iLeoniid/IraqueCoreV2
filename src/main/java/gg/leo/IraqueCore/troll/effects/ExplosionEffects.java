package gg.leo.IraqueCore.troll.effects;

import gg.leo.IraqueCore.troll.TrollEffect;
import gg.leo.IraqueCore.troll.TrollManager;
import gg.leo.IraqueCore.utils.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Random;

public final class ExplosionEffects {

    private static final Random RANDOM = new Random();

    private ExplosionEffects() {}

    public static void register(TrollManager manager) {
        manager.registerEffect(new ExplodingChickenEffect());
        manager.registerEffect(new KittyCannonEffect());
        manager.registerEffect(new FakeNukeEffect());
        manager.registerEffect(new NukeEffect());
    }

    private static class ExplodingChickenEffect extends TrollEffect {
        ExplodingChickenEffect() {
            super("exploding-chicken", "&6Galinha Explosiva", Material.COOKED_CHICKEN,
                    Arrays.asList("&7Invoca uma galinha que explode", "&7com fogos de artificio!", "", "&e\u25B8 Clique para aplicar"),
                    "explosion", "troll.effect.exploding-chicken", 0, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            Location loc = target.getEyeLocation().add(target.getLocation().getDirection().multiply(3));
            org.bukkit.entity.Chicken chicken = (org.bukkit.entity.Chicken)
                    loc.getWorld().spawn(loc, org.bukkit.entity.Chicken.class);
            chicken.setInvulnerable(true);
            chicken.setAI(false);

            loc.getWorld().createExplosion(loc, 2.0f, false, false);
            loc.getWorld().spawnParticle(Particle.FIREWORK, loc, 50, 1, 1, 1, 0.1);
            loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2, 1);

            SchedulerUtil.runLater(
                    gg.leo.IraqueCore.IraqueCore.getInstance(), () -> {
                        if (chicken.isValid()) chicken.remove();
                        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.COOKED_CHICKEN));
                    }, 20L);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }

    private static class KittyCannonEffect extends TrollEffect {
        KittyCannonEffect() {
            super("kitty-cannon", "&eCatapulta de Gatinhos", Material.OCELOT_SPAWN_EGG,
                    Arrays.asList("&7Dispara ocelots explosivos", "&7no jogador repetidamente.", "", "&e\u25B8 Clique para aplicar"),
                    "explosion", "troll.effect.kitty-cannon", 12, 90);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            spawnKitty(target);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 15; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            spawnKitty(target);
        }

        private void spawnKitty(Player target) {
            Location loc = target.getEyeLocation().add(target.getLocation().getDirection().multiply(5));
            org.bukkit.entity.Ocelot ocelot = (org.bukkit.entity.Ocelot)
                    loc.getWorld().spawn(loc, org.bukkit.entity.Ocelot.class);
            ocelot.setVelocity(target.getLocation().getDirection().multiply(-2));
            loc.getWorld().createExplosion(loc, 0.0f, false, false);
            loc.getWorld().playSound(loc, Sound.ENTITY_CAT_HURT, 2, 1);

            SchedulerUtil.runLater(
                    gg.leo.IraqueCore.IraqueCore.getInstance(), () -> {
                        if (ocelot.isValid()) ocelot.remove();
                    }, 60L);
        }
    }

    private static class FakeNukeEffect extends TrollEffect {
        FakeNukeEffect() {
            super("fake-nuke", "&cFalsa Nuke", Material.TNT,
                    Arrays.asList("&7Invoca 16 TNTs falsas", "&7(se auto-removem).", "", "&e\u25B8 Clique para aplicar"),
                    "explosion", "troll.effect.fake-nuke", 4, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            Location loc = target.getLocation().add(0, 5, 0);
            for (int i = 0; i < 16; i++) {
                double angle = 2 * Math.PI * i / 16;
                Location tntLoc = loc.clone().add(Math.cos(angle) * 3, 0, Math.sin(angle) * 3);
                tntLoc.getWorld().spawn(tntLoc, TNTPrimed.class);
            }
            SchedulerUtil.runLater(
                    gg.leo.IraqueCore.IraqueCore.getInstance(), () -> {
                        target.getWorld().getEntities().forEach(e -> {
                            if (e instanceof TNTPrimed) e.remove();
                        });
                    }, 60L);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }

    private static class NukeEffect extends TrollEffect {
        NukeEffect() {
            super("nuke", "&4\u00a1NUKE!", Material.TNT_MINECART,
                    Arrays.asList("&4\u00a1INVOCA 320 TNTs!", "&7(Se auto-removem depois).", "", "&e\u25B8 Clique para aplicar"),
                    "explosion", "troll.effect.nuke", 0, 300);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            Location loc = target.getLocation().add(0, 5, 0);
            for (int cycle = 0; cycle < 20; cycle++) {
                SchedulerUtil.runLater(
                        gg.leo.IraqueCore.IraqueCore.getInstance(), () -> {
                            for (int i = 0; i < 16; i++) {
                                double angle = 2 * Math.PI * i / 16;
                                Location tntLoc = loc.clone().add(
                                        Math.cos(angle) * (4 + RANDOM.nextDouble() * 3),
                                        RANDOM.nextDouble() * 3,
                                        Math.sin(angle) * (4 + RANDOM.nextDouble() * 3));
                                tntLoc.getWorld().spawn(tntLoc, TNTPrimed.class);
                            }
                        }, cycle * 3L);
            }
            SchedulerUtil.runLater(
                    gg.leo.IraqueCore.IraqueCore.getInstance(), () -> {
                        target.getWorld().getEntities().forEach(e -> {
                            if (e instanceof TNTPrimed) e.remove();
                        });
                    }, 120L);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }
}
