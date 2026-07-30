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
    private final Map<UUID, Set<String>> activeEffects = new HashMap<>();
    private final Map<UUID, Map<String, BukkitTask>> activeTasks = new HashMap<>();
    private final Map<UUID, List<Runnable>> revertTasks = new HashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Map<String, BukkitTask>> autoRemoveTasks = new HashMap<>();

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
        Set<String> active = activeEffects.get(target.getUniqueId());
        return active != null && active.contains(effectId);
    }

    public boolean hasAnyActiveEffect(Player target) {
        Set<String> active = activeEffects.get(target.getUniqueId());
        return active != null && !active.isEmpty();
    }

    public Set<String> getActiveEffects(Player target) {
        return activeEffects.getOrDefault(target.getUniqueId(), new HashSet<>());
    }

    public void applyEffect(Player target, String effectId, Player source) {
        TrollEffect effect = effects.get(effectId);
        if (effect == null) return;

        if (isOnCooldown(target.getUniqueId(), effectId)) {
            long remaining = getCooldownRemaining(target.getUniqueId(), effectId);
            if (source != null) source.sendMessage("§cEsse efeito esta em cooldown para " + target.getName() +
                    "! Espere " + remaining + "s.");
            return;
        }

        if (hasActiveEffect(target, effectId)) {
            if (source != null) source.sendMessage("§c" + target.getName() + " ja tem esse efeito ativo!");
            return;
        }

        activeEffects.computeIfAbsent(target.getUniqueId(), k -> new HashSet<>()).add(effectId);

        setCooldown(target.getUniqueId(), effectId,
                getConfigValue("troll.cooldowns." + effectId, effect.getDefaultCooldown()));

        effect.apply(target, this);

        int duration = getConfigValue("troll.durations." + effectId, effect.getDefaultDuration());
        if (duration > 0) {
            BukkitTask removeTask = Bukkit.getScheduler().runTaskLater(plugin,
                    () -> removeEffect(target, effectId, false), duration * 20L);
            autoRemoveTasks.computeIfAbsent(target.getUniqueId(), k -> new HashMap<>()).put(effectId, removeTask);
        }

        if (effect.requiresTask()) {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (!target.isOnline()) {
                    removeEffect(target, effectId, true);
                    return;
                }
                effect.onTick(target, this);
            }, effect.getInterval(), effect.getInterval());
            activeTasks.computeIfAbsent(target.getUniqueId(), k -> new HashMap<>()).put(effectId, task);
        }

        String sourceName = source != null ? source.getName() : "Console";
        plugin.getPluginLogger().info("Troll", sourceName + " applied §c" + effect.getName() +
                " §fon §e" + target.getName());
        if (source != null) source.sendMessage("§aVoce aplicou §e" + effect.getName() + " §aem §e" + target.getName() + "§a!");
    }

    public void removeEffect(Player target, String effectId, boolean silent) {
        if (!hasActiveEffect(target, effectId)) return;

        activeEffects.get(target.getUniqueId()).remove(effectId);

        Map<String, BukkitTask> tasks = activeTasks.get(target.getUniqueId());
        if (tasks != null) {
            BukkitTask task = tasks.remove(effectId);
            if (task != null) task.cancel();
        }

        Map<String, BukkitTask> removes = autoRemoveTasks.get(target.getUniqueId());
        if (removes != null) {
            BukkitTask task = removes.remove(effectId);
            if (task != null) task.cancel();
        }

        List<Runnable> reverts = revertTasks.get(target.getUniqueId());
        if (reverts != null) {
            reverts.removeIf(r -> {
                r.run();
                return true;
            });
        }

        TrollEffect effect = effects.get(effectId);
        if (effect != null) {
            effect.revert(target, this);
            if (!silent && target.isOp()) {
                target.sendMessage("§aO efeito §e" + effect.getName() + " §afoi removido.");
            }
        }
    }

    public void undoAll(Player target) {
        Set<String> active = activeEffects.get(target.getUniqueId());
        if (active == null || active.isEmpty()) return;

        List<String> toRemove = new ArrayList<>(active);
        for (String effectId : toRemove) {
            removeEffect(target, effectId, true);
        }

        Map<String, BukkitTask> tasks = activeTasks.remove(target.getUniqueId());
        if (tasks != null) tasks.values().forEach(BukkitTask::cancel);

        Map<String, BukkitTask> removes = autoRemoveTasks.remove(target.getUniqueId());
        if (removes != null) removes.values().forEach(BukkitTask::cancel);

        List<Runnable> reverts = revertTasks.remove(target.getUniqueId());
        if (reverts != null) reverts.forEach(Runnable::run);

        activeEffects.remove(target.getUniqueId());
        cooldowns.remove(target.getUniqueId());
    }

    public void addRevertTask(Player target, Runnable task) {
        revertTasks.computeIfAbsent(target.getUniqueId(), k -> new ArrayList<>()).add(task);
    }

    public boolean isOnCooldown(UUID targetId, String effectId) {
        Map<String, Long> playerCooldowns = cooldowns.get(targetId);
        if (playerCooldowns == null) return false;
        Long expiry = playerCooldowns.get(effectId);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            playerCooldowns.remove(effectId);
            return false;
        }
        return true;
    }

    public long getCooldownRemaining(UUID targetId, String effectId) {
        Map<String, Long> playerCooldowns = cooldowns.get(targetId);
        if (playerCooldowns == null) return 0;
        Long expiry = playerCooldowns.get(effectId);
        if (expiry == null) return 0;
        long remaining = (expiry - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    private void setCooldown(UUID targetId, String effectId, int seconds) {
        if (seconds <= 0) return;
        cooldowns.computeIfAbsent(targetId, k -> new HashMap<>())
                .put(effectId, System.currentTimeMillis() + (seconds * 1000L));
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
