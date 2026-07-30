package gg.leo.IraqueCore.troll.effects;

import gg.leo.IraqueCore.troll.TrollEffect;
import gg.leo.IraqueCore.troll.TrollManager;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Random;

public final class ChatEffectsTFR {

    private static final Random RANDOM = new Random();

    private ChatEffectsTFR() {}

    public static void register(TrollManager manager) {
        manager.registerEffect(new NickEffect());
    }

    private static class NickEffect extends TrollEffect {
        NickEffect() {
            super("nick", "&dNick Cambiante", Material.NAME_TAG,
                    Arrays.asList("&7Cambia el nickname del jugador", "&7a un nombre aleatorio.", "", "&e\u25B8 Click para aplicar"),
                    "chat2", "troll.effect.nick", 15, 90);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            String randomNick = ChatColor.translateAlternateColorCodes('&', "&8[&7TROLLED&8] &f" + target.getName());
            target.displayName(Component.text(randomNick));
            target.playerListName(Component.text(randomNick));
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            target.displayName(null);
            target.playerListName(null);
        }

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 40; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            String color = ChatColor.values()[RANDOM.nextInt(ChatColor.values().length)].toString();
            target.playerListName(Component.text(color + target.getName()));
        }
    }
}
