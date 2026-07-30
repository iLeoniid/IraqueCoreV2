package gg.leo.IraqueCore.troll.effects;

import gg.leo.IraqueCore.troll.TrollEffect;
import gg.leo.IraqueCore.troll.TrollEventListener;
import gg.leo.IraqueCore.troll.TrollManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class EventDrivenEffects {

    private EventDrivenEffects() {}

    public static void register(TrollManager manager) {
        manager.registerEffect(new FreezeEffect());
        manager.registerEffect(new BreakEffect());
        manager.registerEffect(new DeafenEffect());
        manager.registerEffect(new ExplodeOnChatEffect());
        manager.registerEffect(new RandomChatEffect());
        manager.registerEffect(new ReverseChatEffect());
        manager.registerEffect(new BedExplodeEffect());
        manager.registerEffect(new StopSleepEffect());
        manager.registerEffect(new TNTPlaceEffect());
        manager.registerEffect(new LightningEffect());
        manager.registerEffect(new ForceJumpEffect());
        manager.registerEffect(new SneakDestroyEffect());
        manager.registerEffect(new InstaToolBreakEffect());
        manager.registerEffect(new AquaphobiaEffect());
        manager.registerEffect(new InventoryStopEffect());
        manager.registerEffect(new EntityMultiplyEffect());
    }

    private static abstract class EventToggleEffect extends TrollEffect {
        protected final Set<UUID> active = new HashSet<>();

        EventToggleEffect(String id, String name, Material icon, java.util.List<String> description,
                          String category, String permission, int defaultDuration, int defaultCooldown) {
            super(id, name, icon, description, category, permission, defaultDuration, defaultCooldown);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            active.add(target.getUniqueId());
            register(manager.getPlugin().getTrollEventListener(), target);
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            active.remove(target.getUniqueId());
            unregister(manager.getPlugin().getTrollEventListener(), target);
        }

        protected abstract void register(TrollEventListener listener, Player target);
        protected abstract void unregister(TrollEventListener listener, Player target);
    }

    private static class FreezeEffect extends EventToggleEffect {
        FreezeEffect() {
            super("event-freeze", "&bCongelar (Eventos)", Material.PACKED_ICE,
                    Arrays.asList("&7Congela o jogador impedindo", "&7que se mova (por evento).", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.event-freeze", 10, 60);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.frozenPlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.frozenPlayers.remove(t.getUniqueId()); }
    }

    private static class BreakEffect extends EventToggleEffect {
        BreakEffect() {
            super("break", "&7Romper/Colocar", Material.BARRIER,
                    Arrays.asList("&7Impede quebrar e colocar", "&7blocos do jogador.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.break", 12, 60);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.breakPlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.breakPlayers.remove(t.getUniqueId()); }
    }

    private static class DeafenEffect extends EventToggleEffect {
        DeafenEffect() {
            super("deafen", "&7Ensordecer", Material.STRUCTURE_VOID,
                    Arrays.asList("&7Impede o jogador de escrever", "&7no chat.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.deafen", 10, 45);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.deafenedPlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.deafenedPlayers.remove(t.getUniqueId()); }
    }

    private static class ExplodeOnChatEffect extends EventToggleEffect {
        ExplodeOnChatEffect() {
            super("explode-on-chat", "&6Explotar al Chat", Material.TNT,
                    Arrays.asList("&7Explode o jogador quando", "&7escreve no chat.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.explode-on-chat", 12, 60);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.explodeOnChatPlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.explodeOnChatPlayers.remove(t.getUniqueId()); }
    }

    private static class RandomChatEffect extends EventToggleEffect {
        RandomChatEffect() {
            super("random-chat", "&eChat Aleatorio", Material.PAPER,
                    Arrays.asList("&7Substitui as mensagens do", "&7jogador por mensagens aleatorias.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.random-chat", 12, 60);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.randomChatPlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.randomChatPlayers.remove(t.getUniqueId()); }
    }

    private static class ReverseChatEffect extends EventToggleEffect {
        ReverseChatEffect() {
            super("reverse-chat", "&dChat Invertido", Material.MAP,
                    Arrays.asList("&7Inverte as mensagens do", "&7jogador no chat.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.reverse-chat", 12, 60);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.reverseChatPlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.reverseChatPlayers.remove(t.getUniqueId()); }
    }

    private static class BedExplodeEffect extends EventToggleEffect {
        BedExplodeEffect() {
            super("bed-explode", "&4Cama Explosiva", Material.RED_BED,
                    Arrays.asList("&7Explode a cama quando", "&7o jogador tenta dormir.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.bed-explode", 10, 60);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.bedExplodePlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.bedExplodePlayers.remove(t.getUniqueId()); }
    }

    private static class StopSleepEffect extends EventToggleEffect {
        StopSleepEffect() {
            super("stop-sleep", "&6No Dormir", Material.RED_BED,
                    Arrays.asList("&7Impede o jogador de dormir", "&7em camas.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.stop-sleep", 10, 45);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.stopSleepPlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.stopSleepPlayers.remove(t.getUniqueId()); }
    }

    private static class TNTPlaceEffect extends EventToggleEffect {
        TNTPlaceEffect() {
            super("tnt-place", "&cTNT al Colocar", Material.TNT,
                    Arrays.asList("&7Quando o jogador coloca um", "&7bloco, ele vira TNT.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.tnt-place", 10, 60);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.tntPlacePlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.tntPlacePlayers.remove(t.getUniqueId()); }
    }

    private static class LightningEffect extends EventToggleEffect {
        LightningEffect() {
            super("lightning", "&eRayo al Moverse", Material.NETHER_STAR,
                    Arrays.asList("&7Cai um raio quando o", "&7jogador se move.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.lightning", 12, 60);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.lightningPlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.lightningPlayers.remove(t.getUniqueId()); }
    }

    private static class ForceJumpEffect extends EventToggleEffect {
        ForceJumpEffect() {
            super("force-jump-event", "&aSalto Forzado (Evento)", Material.SLIME_BLOCK,
                    Arrays.asList("&7Salta forcadamente ao", "&7se mover.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.force-jump-event", 10, 45);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.forceJumpPlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.forceJumpPlayers.remove(t.getUniqueId()); }
    }

    private static class SneakDestroyEffect extends EventToggleEffect {
        SneakDestroyEffect() {
            super("sneak-destroy", "&7Destruir al Agacharse", Material.OAK_TRAPDOOR,
                    Arrays.asList("&7Destroi o bloco abaixo", "&7do jogador ao se agachar.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.sneak-destroy", 10, 45);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.sneakDestroyPlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.sneakDestroyPlayers.remove(t.getUniqueId()); }
    }

    private static class InstaToolBreakEffect extends EventToggleEffect {
        InstaToolBreakEffect() {
            super("insta-tool-break", "&6Herramientas Rotas", Material.STONE_PICKAXE,
                    Arrays.asList("&7As ferramentas do jogador", "&7quebram na hora.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.insta-tool-break", 12, 60);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.instaToolBreakPlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.instaToolBreakPlayers.remove(t.getUniqueId()); }
    }

    private static class AquaphobiaEffect extends EventToggleEffect {
        AquaphobiaEffect() {
            super("aquaphobia", "&9Acuafobia", Material.WATER_BUCKET,
                    Arrays.asList("&7Envenena o jogador quando", "&7esta na agua.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.aquaphobia", 12, 60);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.aquaphobiaPlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.aquaphobiaPlayers.remove(t.getUniqueId()); }
    }

    private static class InventoryStopEffect extends EventToggleEffect {
        InventoryStopEffect() {
            super("inventory-stop", "&5Sin Inventarios", Material.CHEST,
                    Arrays.asList("&7Impede o jogador de abrir", "&7qualquer inventario.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.inventory-stop", 12, 60);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.inventoryStopPlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.inventoryStopPlayers.remove(t.getUniqueId()); }
    }

    private static class EntityMultiplyEffect extends EventToggleEffect {
        EntityMultiplyEffect() {
            super("entity-multiply", "&aMultiplicar Entidades", Material.CHICKEN_SPAWN_EGG,
                    Arrays.asList("&7As entidades que o jogador", "&7mata se multiplicam x2.", "", "&e\u25B8 Clique para aplicar"),
                    "event", "troll.effect.entity-multiply", 15, 90);
        }
        @Override protected void register(TrollEventListener l, Player t) { l.entityMultiplyPlayers.add(t.getUniqueId()); }
        @Override protected void unregister(TrollEventListener l, Player t) { l.entityMultiplyPlayers.remove(t.getUniqueId()); }
    }
}
