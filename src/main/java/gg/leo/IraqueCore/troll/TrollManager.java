package gg.leo.IraqueCore.troll;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.troll.effects.BedEffects;
import gg.leo.IraqueCore.troll.effects.ChatEffectsTFR;
import gg.leo.IraqueCore.troll.effects.ClassicEffects;
import gg.leo.IraqueCore.troll.effects.CombatWorldEffects;
import gg.leo.IraqueCore.troll.effects.EventDrivenEffects;
import gg.leo.IraqueCore.troll.effects.ExplosionEffects;
import gg.leo.IraqueCore.troll.effects.InterfaceEffects;
import gg.leo.IraqueCore.troll.effects.InventoryEffects;
import gg.leo.IraqueCore.troll.effects.MovementEffects;
import gg.leo.IraqueCore.troll.effects.RandomEffects;
import gg.leo.IraqueCore.troll.effects.SoundChatEffects;
import gg.leo.IraqueCore.troll.effects.VisualEffects;
import gg.leo.IraqueCore.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TrollManager {

    private final IraqueCore plugin;
    private final TrollEventListener eventListener;
    private final Map<String, TrollEffect> effects = new HashMap<>();
    private final Map<UUID, TrollSession> sessions = new HashMap<>();

    public TrollManager(IraqueCore plugin) {
        this.plugin = plugin;
        this.eventListener = new TrollEventListener(this);
    }

    public TrollEventListener getEventListener() {
        return eventListener;
    }

    public IraqueCore getPlugin() {
        return plugin;
    }

    public void load() {
        registerDefaultEffects();
    }

    public void registerEffect(TrollEffect effect) {
        effects.put(effect.getId(), effect);
    }

    public TrollEffect getEffect(String id) {
        return effects.get(id);
    }

    public Map<String, TrollEffect> getEffects() {
        return effects;
    }

    public List<TrollEffect> getEffectsByCategory(String category) {
        List<TrollEffect> result = new ArrayList<>();
        for (TrollEffect effect : effects.values()) {
            if (effect.getCategory().equals(category)) {
                result.add(effect);
            }
        }
        return result;
    }

    public List<String> getCategories() {
        Set<String> categories = new HashSet<>();
        for (TrollEffect effect : effects.values()) {
            categories.add(effect.getCategory());
        }
        return new ArrayList<>(categories);
    }

    public boolean hasActiveEffect(Player target, String effectId) {
        TrollSession session = sessions.get(target.getUniqueId());
        return session != null && session.hasActiveEffect(effectId);
    }

    public boolean hasAnyActiveEffect(Player target) {
        TrollSession session = sessions.get(target.getUniqueId());
        return session != null && session.hasAnyActiveEffect();
    }

    public Set<String> getActiveEffects(Player target) {
        TrollSession session = sessions.get(target.getUniqueId());
        return session != null ? session.getActiveEffects() : new HashSet<>();
    }

    public TrollSession getSession(Player target) {
        return sessions.computeIfAbsent(target.getUniqueId(), TrollSession::new);
    }

    public void applyEffect(Player target, String effectId, Player source) {
        TrollEffect effect = effects.get(effectId);
        if (effect == null) return;

        TrollSession session = getSession(target);

        if (session.isOnCooldown(effectId)) {
            long remaining = session.getCooldownRemaining(effectId);
            if (source != null) source.sendMessage("§cEsse efeito esta em cooldown para " + target.getName() +
                    "! Espere " + remaining + "s.");
            return;
        }

        if (session.hasActiveEffect(effectId)) {
            if (source != null) source.sendMessage("§c" + target.getName() + " ja tem esse efeito ativo!");
            return;
        }

        session.addActiveEffect(effectId);

        session.setCooldown(effectId,
                getConfigValue("troll.cooldowns." + effectId, effect.getDefaultCooldown()));

        effect.apply(target, this);

        int duration = getConfigValue("troll.durations." + effectId, effect.getDefaultDuration());
        if (duration > 0) {
            BukkitTask removeTask = SchedulerUtil.runLater(plugin,
                    () -> removeEffect(target, effectId, false), duration * 20L);
            session.addAutoRemoveTask(effectId, removeTask);
        }

        if (effect.requiresTask()) {
            BukkitTask task = SchedulerUtil.runTimer(plugin, () -> {
                if (!target.isOnline()) {
                    removeEffect(target, effectId, true);
                    return;
                }
                effect.onTick(target, this);
            }, effect.getInterval(), effect.getInterval());
            session.addActiveTask(effectId, task);
        }

        String sourceName = source != null ? source.getName() : "Console";
        plugin.getPluginLogger().info("Troll", sourceName + " applied §c" + effect.getName() +
                " §fon §e" + target.getName());
        if (source != null) source.sendMessage("§aVoce aplicou §e" + effect.getName() + " §aem §e" + target.getName() + "§a!");
    }

    public void removeEffect(Player target, String effectId, boolean silent) {
        TrollSession session = sessions.get(target.getUniqueId());
        if (session == null || !session.hasActiveEffect(effectId)) return;

        session.removeActiveEffect(effectId);

        BukkitTask task = session.removeActiveTask(effectId);
        if (task != null) task.cancel();

        BukkitTask removeTask = session.removeAutoRemoveTask(effectId);
        if (removeTask != null) removeTask.cancel();

        session.runRevertTasks();

        TrollEffect effect = effects.get(effectId);
        if (effect != null) {
            effect.revert(target, this);
            if (!silent && target.isOp()) {
                target.sendMessage("§aO efeito §e" + effect.getName() + " §afoi removido.");
            }
        }
    }

    public void undoAll(Player target) {
        TrollSession session = sessions.get(target.getUniqueId());
        if (session == null) return;

        for (String effectId : new ArrayList<>(session.getActiveEffects())) {
            removeEffect(target, effectId, true);
        }

        session.clear();
        sessions.remove(target.getUniqueId());
    }

    public void endSession(Player target) {
        TrollSession session = sessions.remove(target.getUniqueId());
        if (session == null) return;
        session.clear();
    }

    public void addRevertTask(Player target, Runnable task) {
        getSession(target).addRevertTask(task);
    }

    public boolean isOnCooldown(UUID targetId, String effectId) {
        TrollSession session = sessions.get(targetId);
        return session != null && session.isOnCooldown(effectId);
    }

    public long getCooldownRemaining(UUID targetId, String effectId) {
        TrollSession session = sessions.get(targetId);
        return session != null ? session.getCooldownRemaining(effectId) : 0;
    }

    private int getConfigValue(String path, int defaultValue) {
        return plugin.getConfig().getInt(path, defaultValue);
    }

    private void registerDefaultEffects() {
        VisualEffects.register(this);
        MovementEffects.register(this);
        InventoryEffects.register(this);
        SoundChatEffects.register(this);
        CombatWorldEffects.register(this);
        InterfaceEffects.register(this);
        ClassicEffects.register(this);
        ExplosionEffects.register(this);
        BedEffects.register(this);
        ChatEffectsTFR.register(this);
        RandomEffects.register(this);
        EventDrivenEffects.register(this);
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    public boolean isAllowTrollOp() {
        return plugin.getConfig().getBoolean("troll-config.allow-troll-op", false);
    }

    public void setAllowTrollOp(boolean value) {
        plugin.getConfig().set("troll-config.allow-troll-op", value);
        plugin.saveConfig();
    }

    public boolean isBlocked(Player target) {
        return plugin.getConfig().getStringList("troll-config.blocklist")
                .contains(target.getName());
    }

    public void addBlocked(Player target) {
        List<String> blocklist = new ArrayList<>(
                plugin.getConfig().getStringList("troll-config.blocklist"));
        if (!blocklist.contains(target.getName())) {
            blocklist.add(target.getName());
            plugin.getConfig().set("troll-config.blocklist", blocklist);
            plugin.saveConfig();
        }
    }

    public void removeBlocked(String targetName) {
        List<String> blocklist = new ArrayList<>(
                plugin.getConfig().getStringList("troll-config.blocklist"));
        if (blocklist.remove(targetName)) {
            plugin.getConfig().set("troll-config.blocklist", blocklist);
            plugin.saveConfig();
        }
    }
}
