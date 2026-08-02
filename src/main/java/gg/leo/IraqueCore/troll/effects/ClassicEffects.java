package gg.leo.IraqueCore.troll.effects;

import gg.leo.IraqueCore.troll.TrollEffect;
import gg.leo.IraqueCore.troll.TrollManager;
import gg.leo.IraqueCore.utils.SchedulerUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class ClassicEffects {

    private static final Random RANDOM = new Random();

    private ClassicEffects() {}

    public static void register(TrollManager manager) {
        manager.registerEffect(new SpinEffect());
        manager.registerEffect(new FakeOpEffect());
        manager.registerEffect(new FakeUnOpEffect());
        manager.registerEffect(new PumpkinEffect());
        manager.registerEffect(new RickRollEffect());
        manager.registerEffect(new SlendermanEffect());
        manager.registerEffect(new AnvilDropEffect());
        manager.registerEffect(new FakeCrashEffect());
        manager.registerEffect(new FakeBanEffect());
        manager.registerEffect(new FakeReloadEffect());
    }

    private static class SpinEffect extends TrollEffect {
        SpinEffect() {
            super("spin", "&bGiro Infinito", Material.COMPASS,
                    Arrays.asList("&7Faz o jogador girar", "&7constantemente 90 graus.", "", "&e\u25B8 Clique para aplicar"),
                    "classic", "troll.effect.spin", 10, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            doSpin(target);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 5; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            doSpin(target);
        }

        private void doSpin(Player target) {
            Location loc = target.getLocation();
            loc.setYaw(loc.getYaw() + 90);
            target.teleport(loc);
        }
    }

    private static class FakeOpEffect extends TrollEffect {
        FakeOpEffect() {
            super("fake-op", "&aFalso OP", Material.DIAMOND,
                    Arrays.asList("&7Mostra uma mensagem falsa de", "&7que o jogador e OP.", "", "&e\u25B8 Clique para aplicar"),
                    "classic", "troll.effect.fake-op", 0, 15);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.sendMessage(ChatColor.GRAY + "[Server: " + target.getName() + " foi promovido a operador do servidor]");
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }

    private static class FakeUnOpEffect extends TrollEffect {
        FakeUnOpEffect() {
            super("fake-unop", "&cFalso De-OP", Material.DIAMOND_AXE,
                    Arrays.asList("&7Mostra uma mensagem falsa de", "&7remocao de OP do jogador.", "", "&e\u25B8 Clique para aplicar"),
                    "classic", "troll.effect.fake-unop", 0, 15);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.sendMessage(ChatColor.GRAY + "[Server: " + target.getName() + " nao e mais operador do servidor]");
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }

    private static class PumpkinEffect extends TrollEffect {
        PumpkinEffect() {
            super("pumpkin", "&6Abobora Eterna", Material.CARVED_PUMPKIN,
                    Arrays.asList("&7Coloca uma abobora na cabeca", "&7do jogador constantemente.", "", "&e\u25B8 Clique para aplicar"),
                    "classic", "troll.effect.pumpkin", 15, 45);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.getInventory().setHelmet(new ItemStack(Material.CARVED_PUMPKIN));
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            ItemStack helmet = target.getInventory().getHelmet();
            if (helmet != null && helmet.getType() == Material.CARVED_PUMPKIN) {
                target.getInventory().setHelmet(null);
            }
        }

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 10; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            ItemStack helmet = target.getInventory().getHelmet();
            if (helmet == null || helmet.getType() != Material.CARVED_PUMPKIN) {
                target.getInventory().setHelmet(new ItemStack(Material.CARVED_PUMPKIN));
            }
        }
    }

    private static class RickRollEffect extends TrollEffect {
        RickRollEffect() {
            super("rickroll", "&dRick Roll", Material.WRITTEN_BOOK,
                    Arrays.asList("&7Abre um livro com letras", "&7e musica do Rick Astley!", "", "&e\u25B8 Clique para aplicar"),
                    "classic", "troll.effect.rickroll", 8, 90);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta) book.getItemMeta();
            meta.setTitle("Never Gonna Give You Up");
            meta.setAuthor("Rick Astley");
            meta.setPages(
                    "We're no strangers to love\nYou know the rules and so do I\nA full commitment's what I'm thinking of\nYou wouldn't get this from any other guy",
                    "I just wanna tell you how I'm feeling\nGotta make you understand\nNever gonna give you up\nNever gonna let you down",
                    "Never gonna run around and desert you\nNever gonna make you cry\nNever gonna say goodbye\nNever gonna tell a lie and hurt you",
                    "We've known each other for so long\nYour heart's been aching but you're too shy to say it\nInside we both know what's been going on\nWe know the game and we're gonna play it",
                    "And if you ask me how I'm feeling\nDon't tell me you're too blind to see\nNever gonna give you up\nNever gonna let you down",
                    "Never gonna run around and desert you\nNever gonna make you cry\nNever gonna say goodbye\nNever gonna tell a lie and hurt you",
                    "Never gonna give you up\nNever gonna let you down\nNever gonna run around and desert you\nNever gonna make you cry",
                    "Never gonna say goodbye\nNever gonna tell a lie and hurt you\nWe've known each other for so long\nYour heart's been aching but you're too shy to say it",
                    "Inside we both know what's been going on\nWe know the game and we're gonna play it\nI just wanna tell you how I'm feeling\nGotta make you understand",
                    "Never gonna give you up\nNever gonna let you down\nNever gonna run around and desert you\nNever gonna make you cry\nNever gonna say goodbye\nNever gonna tell a lie and hurt you"
            );
            book.setItemMeta(meta);
            target.openBook(book);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }

    private static class SlendermanEffect extends TrollEffect {
        SlendermanEffect() {
            super("slenderman", "&8Slenderman", Material.ENDER_PEARL,
                    Arrays.asList("&7Invoca um Enderman", "&7assustador que te persegue.", "", "&e\u25B8 Clique para aplicar"),
                    "classic", "troll.effect.slenderman", 12, 120);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            Location loc = target.getLocation();
            org.bukkit.entity.Enderman enderman = (org.bukkit.entity.Enderman)
                    loc.getWorld().spawn(loc, org.bukkit.entity.Enderman.class);
            enderman.setTarget(target);
            enderman.setInvulnerable(true);
            enderman.setCustomName("Slenderman");
            enderman.setCustomNameVisible(true);

            manager.addRevertTask(target, () -> {
                if (enderman.isValid()) enderman.remove();
            });
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }

    private static class AnvilDropEffect extends TrollEffect {
        AnvilDropEffect() {
            super("anvil-drop", "&7Chuva de Bigornas", Material.ANVIL,
                    Arrays.asList("&7Faz cair bigornas sobre", "&7o jogador repetidamente.", "", "&e\u25B8 Clique para aplicar"),
                    "classic", "troll.effect.anvil-drop", 12, 90);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            dropAnvil(target);
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
            dropAnvil(target);
        }

        private void dropAnvil(Player target) {
            Location loc = target.getLocation().add(0, 20, 0);
            loc.getBlock().setType(Material.DAMAGED_ANVIL, false);
        }
    }

    private static class FakeCrashEffect extends TrollEffect {
        FakeCrashEffect() {
            super("fake-crash", "&4Falso Crash", Material.BARRIER,
                    Arrays.asList("&7Simula um crash do cliente", "&7com tela de desconexao.", "", "&e\u25B8 Clique para aplicar"),
                    "classic", "troll.effect.fake-crash", 0, 30);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.kickPlayer(ChatColor.RED + "Internal exception: java.net.SocketException: Connection reset");
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }

    private static class FakeBanEffect extends TrollEffect {
        FakeBanEffect() {
            super("fake-ban", "&4Falso Ban", Material.REDSTONE_BLOCK,
                    Arrays.asList("&7Simula um ban com tela", "&7de ban do servidor.", "", "&e\u25B8 Clique para aplicar"),
                    "classic", "troll.effect.fake-ban", 0, 30);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.kickPlayer(ChatColor.RED + "Voce foi banido deste servidor!");
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }

    private static class FakeReloadEffect extends TrollEffect {
        FakeReloadEffect() {
            super("fake-reload", "&eFalso Reload", Material.COMMAND_BLOCK,
                    Arrays.asList("&7Simula um reload do servidor", "&7com mensagens de console.", "", "&e\u25B8 Clique para aplicar"),
                    "classic", "troll.effect.fake-reload", 6, 120);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.sendMessage(ChatColor.GREEN + "[CONSOLE: Observe que este comando nao e suportado...]");
            SchedulerUtil.runLater(
                    gg.leo.IraqueCore.IraqueCore.getInstance(), () -> {
                        if (!target.isOnline()) return;
                        target.sendMessage(ChatColor.YELLOW + "[CONSOLE: Recarga concluida.]");
                    }, 100L);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }
}
