package gg.leo.IraqueCore.troll.effects;

import gg.leo.IraqueCore.troll.TrollEffect;
import gg.leo.IraqueCore.troll.TrollManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Random;

public final class MovementEffects {

    private static final Random RANDOM = new Random();

    private MovementEffects() {}

    public static void register(TrollManager manager) {
        manager.registerEffect(new RandomTeleportEffect());
        manager.registerEffect(new SlipperyFloorEffect());
        manager.registerEffect(new ForcedJumpEffect());
        manager.registerEffect(new InvertedGravityEffect());
        manager.registerEffect(new FreezeEffect());
    }

    private static class RandomTeleportEffect extends TrollEffect {
        RandomTeleportEffect() {
            super("random-tp", "&bTeletransporte Bucle", Material.ENDER_PEARL,
                    Arrays.asList("&7Teletransporta al jugador a", "&7coordenadas random cercanas.", "", "&e\u25B8 Click para aplicar"),
                    "movement", "troll.effect.random-tp", 12, 120);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            doRandomTeleport(target);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 30; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            doRandomTeleport(target);
        }

        private void doRandomTeleport(Player target) {
            Location loc = target.getLocation();
            double x = loc.getX() + (RANDOM.nextDouble() - 0.5) * 10;
            double z = loc.getZ() + (RANDOM.nextDouble() - 0.5) * 10;
            double y = loc.getY() + (RANDOM.nextDouble() - 0.5) * 3;
            y = Math.max(loc.getWorld().getMinHeight() + 1, Math.min(loc.getWorld().getMaxHeight() - 1, y));
            Location targetLoc = new Location(loc.getWorld(), x, y, z, loc.getYaw(), loc.getPitch());
            target.teleport(targetLoc);
        }
    }

    private static class SlipperyFloorEffect extends TrollEffect {
        SlipperyFloorEffect() {
            super("slippery-floor", "&fSuelo Resbaladizo", Material.ICE,
                    Arrays.asList("&7Altera la velocidad del jugador", "&7aleatoriamente cada 3s.", "", "&e\u25B8 Click para aplicar"),
                    "movement", "troll.effect.slippery-floor", 18, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.setWalkSpeed(0.05f);
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.setWalkSpeed(0.2f);
        }

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 60; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            float speed = 0.02f + RANDOM.nextFloat() * 0.3f;
            target.setWalkSpeed(Math.min(0.5f, Math.max(0.01f, speed)));
        }
    }

    private static class ForcedJumpEffect extends TrollEffect {
        ForcedJumpEffect() {
            super("forced-jump", "&aSalto Forzado", Material.SLIME_BLOCK,
                    Arrays.asList("&7Aplica velocity hacia arriba", "&7en intervalos.", "", "&e\u25B8 Click para aplicar"),
                    "movement", "troll.effect.forced-jump", 14, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            doJump(target);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 35; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            doJump(target);
        }

        private void doJump(Player target) {
            Vector vel = target.getVelocity();
            target.setVelocity(vel.setY(1.0 + RANDOM.nextDouble()));
        }
    }

    private static class InvertedGravityEffect extends TrollEffect {
        InvertedGravityEffect() {
            super("inverted-gravity", "&dGravedad Invertida", Material.FEATHER,
                    Arrays.asList("&7Aplica levitacion al jugador", "&7simulando gravedad invertida.", "", "&e\u25B8 Click para aplicar"),
                    "movement", "troll.effect.inverted-gravity", 12, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20 * getDefaultDuration(), 1, false, false));
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.removePotionEffect(PotionEffectType.LEVITATION);
        }
    }

    private static class FreezeEffect extends TrollEffect {
        FreezeEffect() {
            super("freeze", "&bCongelar Movimiento", Material.BLUE_ICE,
                    Arrays.asList("&7Congela al jugador en su lugar", "&7(sin moverse, pero puede chatear).", "", "&e\u25B8 Click para aplicar"),
                    "movement", "troll.effect.freeze", 8, 120);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.setWalkSpeed(0.0f);
            target.setFlySpeed(0.0f);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 999999, 255, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 999999, 128, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 0, false, false));
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.setWalkSpeed(0.2f);
            target.setFlySpeed(0.1f);
            target.removePotionEffect(PotionEffectType.SLOWNESS);
            target.removePotionEffect(PotionEffectType.JUMP_BOOST);
            target.removePotionEffect(PotionEffectType.BLINDNESS);
        }
    }
}
