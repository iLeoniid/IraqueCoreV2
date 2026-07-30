package gg.leo.IraqueCore.troll.effects;

import gg.leo.IraqueCore.troll.TrollEffect;
import gg.leo.IraqueCore.troll.TrollManager;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public final class BedEffects {

    private BedEffects() {}

    public static void register(TrollManager manager) {
        manager.registerEffect(new BedMissingEffect());
    }

    private static class BedMissingEffect extends TrollEffect {
        BedMissingEffect() {
            super("bed-missing", "&6Cama Perdida", Material.RED_BED,
                    Arrays.asList("&7Mostra uma mensagem falsa de", "&7cama perdida ou obstruida.", "", "&e\u25B8 Clique para aplicar"),
                    "beds", "troll.effect.bed-missing", 0, 15);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.sendMessage(ChatColor.RED + "Voce nao tem uma cama ou ancora de renascimento, ou ela foi obstruida");
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }
}
