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
            super("fake-disconnect", "&4Falsa Desconexao", Material.REDSTONE_BLOCK,
                    List.of("&7Simula uma perda de conexao", "&7com titulos dramaticos.", "", "&e\u25B8 Clique para aplicar"),
                    "visual", "troll.effect.fake-disconnect", 8, 30);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.showTitle(Title.title(
                    Component.text("\u00A7c\u00A7l\u00A1CONEXAO PERDIDA!"),
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
                    List.of("&7Desorienta o jogador aplicando", "&7efeitos que invertem sua percepcao.", "", "&e\u25B8 Clique para aplicar"),
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
            super("blindness-pulse", "&8Cegueira Intermitente", Material.INK_SAC,
                    List.of("&7Aplica cegueira por pulsos", "&7alternando visao a cada 3s.", "", "&e\u25B8 Clique para aplicar"),
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
            super("nausea", "&2Tela Borrada", Material.SLIME_BALL,
                    List.of("&7Aplica NAUSEA (confusao)", "&7deixando a tela borrada.", "", "&e\u25B8 Clique para aplicar"),
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
                "&8[&4\u00a1ATENCAO!&8] &cVoce foi banido permanentemente por: HACKING",
                "&8[&6Sistema&8] &eSeu IP: &f" + getRandomIp() + " &e foi registrado.",
                "&8[&4\u00a1SUSPENSO!&8] &7Motivo: &fUso de clientes nao permitidos",
                "&8[&5Staff&8] &dUm administrador esta revisando seu inventario...",
                "&c\u00a1Todos os seus items serao deletados em 5 segundos!",
                "&8[&aServer&8] &7Seu ping esta muito alto: &c" + getRandomPing() + "ms",
                "&8[&4ANTICHEAT&8] &7Nivel de suspeita: &c" + getRandomSuspicion() + "%",
                "&6\u2728 &eParabens! Voce ganhou o sorteio semanal. &6\u2728",
                "&c\u00a1Sua conta foi comprometida! Mude sua senha agora.",
                "&8[&eNotificacao&8] &7Voce recebeu um report de &f" + getRandomPlayer() + " &7por: &cHACKING"
        );

        FakeChatMessagesEffect() {
            super("fake-chat", "&5Mensagens Falsas", Material.MAP,
                    List.of("&7Envia mensagens falsas do sistema", "&7simulando bans, alertas, etc.", "", "&e\u25B8 Clique para aplicar"),
                    "visual", "troll.effect.fake-chat", 12, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.sendMessage(" ");
            target.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    "&8[&4\u00a1CONEXAO INSTAVEL!&8] &7Injetando mensagens de simulacao..."));
            target.sendMessage(" ");
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.sendMessage(" ");
            target.sendMessage("&aSimulacao de mensagens desativada.");
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
                "&4&l\u00a1VOCE FOI HACKEADO!",
                "&6&l\u00a1VOCE GANHOU UM IPHONE!",
                "&a&l\u00a1HACKER DETECTADO!",
                "&c&l\u00a1SERVER CRASH IMINENTE!",
                "&5&l\u00a1VOCE E O JOGADOR #" + getRandomNumber() + "!",
                "&e&l\u00a1ALGUEM ENTROU NA SUA CONTA!",
                "&4&l\u00a1TROLLEADO!",
                "&d&l\u00a1PARABENS, VOCE FOI TROLLADO!"
        );

        private static final List<String> SUBTITLES = List.of(
                "&7Nao se assuste, e so um troll",
                "&7Calma, tudo esta bem",
                "&7Aproveite o momento",
                "&7Isso vai desaparecer logo",
                "&7Continue jogando normalmente"
        );

        RandomTitleEffect() {
            super("random-title", "&dTitulos Aleatorios", Material.NAME_TAG,
                    List.of("&7Mostra titulos aleatorios", "&7na tela a cada 5s.", "", "&e\u25B8 Clique para aplicar"),
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
