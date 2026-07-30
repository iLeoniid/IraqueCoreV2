package gg.leo.IraqueCore.troll.effects;

import gg.leo.IraqueCore.troll.TrollEffect;
import gg.leo.IraqueCore.troll.TrollManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public final class InterfaceEffects {

    private static final Random RANDOM = new Random();

    private static final List<String> ACTION_BAR_MESSAGES = List.of(
            "&c\u00a1Has sido hackeado!",
            "&6\u2728 Server Lag Detectado \u2728",
            "&4\u00a1ALERTA! Tu conexion es inestable",
            "&a\u00a1Has ganado 1000$ en el juego!",
            "&d\u00a1Felicidades! Eres el visitante #" + getRandomNumber(),
            "&e\u26A1 Tu ping: " + getRandomPing() + "ms",
            "&c\u00a1Tu cuenta sera eliminada en 10s!",
            "&5\u00a1Un Admin te esta observando!",
            "&7\u26A0 No olvides votar por el server \u26A0",
            "&b\u00a1Has recibido un regalo misterioso!"
    );

    private InterfaceEffects() {}

    public static void register(TrollManager manager) {
        manager.registerEffect(new RandomInventoryOpenEffect());
        manager.registerEffect(new TabNameChangeEffect());
        manager.registerEffect(new AnnoyingActionBarEffect());
    }

    private static class RandomInventoryOpenEffect extends TrollEffect {
        RandomInventoryOpenEffect() {
            super("random-inv-open", "&5Inventario Random", Material.BOOK,
                    List.of("&7Abre y cierra inventarios", "&7simulando lag.", "", "&e\u25B8 Click para aplicar"),
                    "interface", "troll.effect.random-inv-open", 18, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            doOpenRandomInventory(target);
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.closeInventory();
        }

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 40; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            doOpenRandomInventory(target);
        }

        private void doOpenRandomInventory(Player target) {
            target.closeInventory();
            Bukkit.getScheduler().runTaskLater(
                    gg.leo.IraqueCore.IraqueCore.getInstance(),
                    () -> {
                        if (!target.isOnline()) return;
                        target.openInventory(Bukkit.createInventory(null, 9,
                                ChatColor.DARK_PURPLE + "¿Que esta pasando?"));
                        Bukkit.getScheduler().runTaskLater(
                                gg.leo.IraqueCore.IraqueCore.getInstance(),
                                () -> {
                                    if (target.isOnline()) target.closeInventory();
                                }, 10L);
                    }, 5L);
        }
    }

    private static class TabNameChangeEffect extends TrollEffect {
        TabNameChangeEffect() {
            super("tab-name", "&bNombre en Tab", Material.NAME_TAG,
                    List.of("&7Cambia temporalmente el nombre", "&7del jugador en el tablist.", "", "&e\u25B8 Click para aplicar"),
                    "interface", "troll.effect.tab-name", 25, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            String originalName = target.getPlayerListName();
            if (originalName == null) originalName = target.getName();
            String funnyName = ChatColor.translateAlternateColorCodes('&',
                    getRandomTabName());
            target.playerListName(Component.text(funnyName));

            String finalOriginalName = originalName;
            manager.addRevertTask(target, () -> {
                if (target.isOnline()) {
                    target.playerListName(Component.text(finalOriginalName));
                }
            });
        }

        @Override
        public void revert(Player target, TrollManager manager) {}

        private String getRandomTabName() {
            List<String> names = List.of(
                    "&4[HACKER] " + getRandomName(),
                    "&c[CRASH] " + getRandomName(),
                    "&8[AFK] " + getRandomName(),
                    "&5[VIP] " + getRandomName(),
                    "&a[GOD] " + getRandomName(),
                    "&e[NEW] " + getRandomName(),
                    "&7[PLAYER] " + getRandomName(),
                    "&6[ADMIN] " + getRandomName()
            );
            return names.get(RANDOM.nextInt(names.size()));
        }

        private String getRandomName() {
            List<String> bases = List.of("Trol", "Hack", "Noob", "Pro", "God", "Xx", "xX", "Null", "Error", "Admin");
            return bases.get(RANDOM.nextInt(bases.size())) + RANDOM.nextInt(1000);
        }
    }

    private static class AnnoyingActionBarEffect extends TrollEffect {
        AnnoyingActionBarEffect() {
            super("action-bar", "&eAction Bar Molesta", Material.COMPASS,
                    List.of("&7Muestra mensajes aleatorios", "&7en la action bar en loop.", "", "&e\u25B8 Click para aplicar"),
                    "interface", "troll.effect.action-bar", 25, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            sendActionBar(target);
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.sendActionBar(Component.text(""));
        }

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 40; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            sendActionBar(target);
        }

        private void sendActionBar(Player target) {
            String msg = ACTION_BAR_MESSAGES.get(RANDOM.nextInt(ACTION_BAR_MESSAGES.size()));
            target.sendActionBar(Component.text(
                    ChatColor.translateAlternateColorCodes('&', msg)));
        }
    }

    private static int getRandomNumber() {
        return 1000 + RANDOM.nextInt(9000);
    }

    private static int getRandomPing() {
        return 500 + RANDOM.nextInt(2000);
    }
}
