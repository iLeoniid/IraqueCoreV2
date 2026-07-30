package gg.leo.IraqueCore.troll.effects;

import gg.leo.IraqueCore.troll.TrollEffect;
import gg.leo.IraqueCore.troll.TrollManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public final class VisualEffects {

    private static final Random RANDOM = new Random();

    private VisualEffects() {}

    public static void register(TrollManager manager) {
        manager.registerEffect(new FakeDisconnectEffect());
        manager.registerEffect(new InvertControlsEffect());
        manager.registerEffect(new BlindnessPulseEffect());
        manager.registerEffect(new NauseaEffect());
        manager.registerEffect(new FakeChatMessagesEffect());
        manager.registerEffect(new RandomTitleEffect());
    }

    private static class FakeDisconnectEffect extends TrollEffect {
        FakeDisconnectEffect() {
            super("fake-disconnect", "&4Falsa Desconexion", Material.REDSTONE_BLOCK,
                    List.of("&7Simula una perdida de conexion", "&7con titulos dramaticos.", "", "&e\u25B8 Click para aplicar"),
                    "visual", "troll.effect.fake-disconnect", 8, 30);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.showTitle(Title.title(
                    Component.text("\u00A7c\u00A7l\u00A1CONEXION PERDIDA!"),
                    Component.text("\u00A77Reconectando..."),
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(4), Duration.ofSeconds(2))
            ));
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.resetTitle();
        }
    }

    private static class InvertControlsEffect extends TrollEffect {
        InvertControlsEffect() {
            super("invert-controls", "&aControles Invertidos", Material.PACKED_ICE,
                    List.of("&7Desorienta al jugador aplicando", "&7efectos que invierten su percepcion.", "", "&e\u25B8 Click para aplicar"),
                    "visual", "troll.effect.invert-controls", 15, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * getDefaultDuration(), 0, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 20 * getDefaultDuration(), 1, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * getDefaultDuration(), 0, false, false));
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.removePotionEffect(PotionEffectType.BLINDNESS);
            target.removePotionEffect(PotionEffectType.NAUSEA);
            target.removePotionEffect(PotionEffectType.SLOWNESS);
        }
    }

    private static class BlindnessPulseEffect extends TrollEffect {
        BlindnessPulseEffect() {
            super("blindness-pulse", "&8Ceguera Intermitente", Material.INK_SAC,
                    List.of("&7Aplica ceguera por pulsos", "&7alternando vision cada 3s.", "", "&e\u25B8 Click para aplicar"),
                    "visual", "troll.effect.blindness-pulse", 18, 45);
        }

        private boolean blindOn = false;

        @Override
        public void apply(Player target, TrollManager manager) {
            blindOn = true;
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 0, false, false));
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.removePotionEffect(PotionEffectType.BLINDNESS);
        }

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 40; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            blindOn = !blindOn;
            if (blindOn) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false));
            } else {
                target.removePotionEffect(PotionEffectType.BLINDNESS);
            }
        }
    }

    private static class NauseaEffect extends TrollEffect {
        NauseaEffect() {
            super("nausea", "&2Pantalla Borrosa", Material.SLIME_BALL,
                    List.of("&7Aplica NAUSEA (confusion)", "&7haciendo la pantalla borrosa.", "", "&e\u25B8 Click para aplicar"),
                    "visual", "troll.effect.nausea", 20, 30);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 20 * getDefaultDuration(), 1, false, false));
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.removePotionEffect(PotionEffectType.NAUSEA);
        }
    }

    private static class FakeChatMessagesEffect extends TrollEffect {
        private static final List<String> FAKE_MESSAGES = List.of(
                "&8[&4\u00a1ATENCION!&8] &cHas sido baneado permanentemente por: HACKING",
                "&8[&6Sistema&8] &eTu IP: &f" + getRandomIp() + " &eha sido registrada.",
                "&8[&4\u00a1SUSPENDIDO!&8] &7Razon: &fUso de clientes no permitidos",
                "&8[&5Staff&8] &dUn administrador esta revisando tu inventario...",
                "&c\u00a1Todos tus items seran eliminados en 5 segundos!",
                "&8[&aServer&8] &7Tu ping es demasiado alto: &c" + getRandomPing() + "ms",
                "&8[&4ANTICHEAT&8] &7Nivel de sospecha: &c" + getRandomSuspicion() + "%",
                "&6\u2728 &eFelicidades! Has ganado el sorteo semanal. &6\u2728",
                "&c\u00a1Tu cuenta ha sido comprometida! Cambia tu contrasena ya.",
                "&8[&eNotificacion&8] &7Has recibido un reporte de &f" + getRandomPlayer() + " &7por: &cHACKING"
        );

        FakeChatMessagesEffect() {
            super("fake-chat", "&5Mensajes Falsos", Material.MAP,
                    List.of("&7Envia mensajes falsos del sistema", "&7simulando baneos, alertas, etc.", "", "&e\u25B8 Click para aplicar"),
                    "visual", "troll.effect.fake-chat", 12, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.sendMessage(" ");
            target.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    "&8[&4\u00a1CONEXION INESTABLE!&8] &7Inyectando mensajes de simulacion..."));
            target.sendMessage(" ");
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.sendMessage(" ");
            target.sendMessage("&aSimulacion de mensajes desactivada.");
            target.sendMessage(" ");
        }

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 60; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            String msg = FAKE_MESSAGES.get(RANDOM.nextInt(FAKE_MESSAGES.size()));
            target.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', msg));
        }

        private static String getRandomIp() {
            return RANDOM.nextInt(256) + "." + RANDOM.nextInt(256) + "." +
                    RANDOM.nextInt(256) + "." + RANDOM.nextInt(256);
        }

        private static int getRandomPing() {
            return 500 + RANDOM.nextInt(2000);
        }

        private static int getRandomSuspicion() {
            return 60 + RANDOM.nextInt(40);
        }

        private static String getRandomPlayer() {
            List<String> names = List.of("Notch", "Steve", "Herobrine", "Admin", "Null", "404", "HackerX");
            return names.get(RANDOM.nextInt(names.size()));
        }
    }

    private static class RandomTitleEffect extends TrollEffect {
        private static final List<String> TITLES = List.of(
                "&4&l\u00a1HAS SIDO HACKEADO!",
                "&6&l\u00a1GANASTE UN IPHONE!",
                "&a&l\u00a1HACKER DETECTADO!",
                "&c&l\u00a1SERVER CRASH INMINENTE!",
                "&5&l\u00a1ERES EL JUGADOR #" + getRandomNumber() + "!",
                "&e&l\u00a1ALGUIEN ENTRO A TU CUENTA!",
                "&4&l\u00a1TROLLEADO!",
                "&d&l\u00a1FELICIDADES, HAS SIDO TROLLEADO!"
        );

        private static final List<String> SUBTITLES = List.of(
                "&7No te asustes, es solo un trolleo",
                "&7Tranquilo, todo esta bien",
                "&7Disfruta el momento",
                "&7Esto desaparecera pronto",
                "&7Sigue jugando normalmente"
        );

        RandomTitleEffect() {
            super("random-title", "&dTitulos Aleatorios", Material.NAME_TAG,
                    List.of("&7Muestra titulos aleatorios", "&7en la pantalla cada 5s.", "", "&e\u25B8 Click para aplicar"),
                    "visual", "troll.effect.random-title", 18, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            sendRandomTitle(target);
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.resetTitle();
        }

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 100; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            sendRandomTitle(target);
        }

        private void sendRandomTitle(Player target) {
            String title = TITLES.get(RANDOM.nextInt(TITLES.size()));
            String subtitle = SUBTITLES.get(RANDOM.nextInt(SUBTITLES.size()));
            target.showTitle(Title.title(
                    Component.text(org.bukkit.ChatColor.translateAlternateColorCodes('&', title)),
                    Component.text(org.bukkit.ChatColor.translateAlternateColorCodes('&', subtitle)),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
            ));
        }

        private static int getRandomNumber() {
            return 1000 + RANDOM.nextInt(9000);
        }
    }
}
