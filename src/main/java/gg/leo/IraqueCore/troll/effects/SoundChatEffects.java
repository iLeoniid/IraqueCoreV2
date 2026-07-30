package gg.leo.IraqueCore.troll.effects;

import gg.leo.IraqueCore.troll.TrollEffect;
import gg.leo.IraqueCore.troll.TrollManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public final class SoundChatEffects {

    private static final Random RANDOM = new Random();

    private static final List<Sound> ANNOYING_SOUNDS = List.of(
            Sound.ENTITY_VILLAGER_AMBIENT,
            Sound.ENTITY_VILLAGER_HURT,
            Sound.ENTITY_CREEPER_PRIMED,
            Sound.ENTITY_TNT_PRIMED,
            Sound.ENTITY_WITHER_AMBIENT,
            Sound.ENTITY_GHAST_SCREAM,
            Sound.ENTITY_BLAZE_AMBIENT,
            Sound.ENTITY_ENDERMAN_SCREAM,
            Sound.ENTITY_ZOMBIE_AMBIENT,
            Sound.ENTITY_SKELETON_AMBIENT,
            Sound.ENTITY_WITCH_AMBIENT,
            Sound.ENTITY_GUARDIAN_AMBIENT,
            Sound.BLOCK_ANVIL_PLACE,
            Sound.ENTITY_FIREWORK_ROCKET_TWINKLE
    );

    private SoundChatEffects() {}

    public static void register(TrollManager manager) {
        manager.registerEffect(new RandomSoundsEffect());
        manager.registerEffect(new FakeSelfChatEffect());
    }

    private static class RandomSoundsEffect extends TrollEffect {
        RandomSoundsEffect() {
            super("random-sounds", "&6Sons Irritantes", Material.JUKEBOX,
                    List.of("&7Reproduz sons aleatorios", "&7irritantes em intervalos.", "", "&e\u25B8 Clique para aplicar"),
                    "soundchat", "troll.effect.random-sounds", 18, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            playRandomSound(target);
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
            playRandomSound(target);
        }

        private void playRandomSound(Player target) {
            Sound sound = ANNOYING_SOUNDS.get(RANDOM.nextInt(ANNOYING_SOUNDS.size()));
            float pitch = 0.5f + RANDOM.nextFloat() * 1.5f;
            target.playSound(target.getLocation(), sound, 2.0f, pitch);
        }
    }

    private static class FakeSelfChatEffect extends TrollEffect {
        private static final List<String> FAKE_PLAYER_MESSAGES = List.of(
                "alguem me trollou xd",
                "o que eu faco agora?",
                "meu inventario se reorganiza sozinho D:",
                "ajuda! minha tela esta estranha",
                "o que esta acontecendo?",
                "admin? tem alguem ai?",
                "acho que meu pc esta ficando louco",
                "wtf",
                "HAHAHA alguem esta me zuando",
                "nao consigo parar de pular",
                "por que esta tocando uma TNT?",
                "quero sair desse server",
                "alguem sabe o que esse efeito faz?",
                "puts que medo",
                "isso e engraçado pra falar a verdade"
        );

        FakeSelfChatEffect() {
            super("fake-self-chat", "&2Auto-Mensagens", Material.PAPER,
                    List.of("&7Simula que o jogador envia", "&7mensagens no chat (so local).", "", "&e\u25B8 Clique para aplicar"),
                    "soundchat", "troll.effect.fake-self-chat", 14, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            sendFakeMessage(target);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 60; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            sendFakeMessage(target);
        }

        private void sendFakeMessage(Player target) {
            String msg = FAKE_PLAYER_MESSAGES.get(RANDOM.nextInt(FAKE_PLAYER_MESSAGES.size()));
            String format = "<" + target.getName() + "> " + msg;
            target.sendMessage(format);
        }
    }
}
