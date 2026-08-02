package gg.leo.IraqueCore.troll;

import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Estado de troll por jugador: efectos activos, tareas programadas,
 * cooldowns y tareas de revertido. Se crea bajo demanda y se limpia
 * al desconectarse el jugador (ver {@link TrollManager#endSession}).
 */
public class TrollSession {

    private final UUID playerId;
    private final Set<String> activeEffects = new HashSet<>();
    private final Map<String, BukkitTask> activeTasks = new HashMap<>();
    private final Map<String, BukkitTask> autoRemoveTasks = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final List<Runnable> revertTasks = new ArrayList<>();

    public TrollSession(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public boolean hasActiveEffect(String effectId) {
        return activeEffects.contains(effectId);
    }

    public boolean hasAnyActiveEffect() {
        return !activeEffects.isEmpty();
    }

    public Set<String> getActiveEffects() {
        return activeEffects;
    }

    public void addActiveEffect(String effectId) {
        activeEffects.add(effectId);
    }

    public void removeActiveEffect(String effectId) {
        activeEffects.remove(effectId);
    }

    public void addActiveTask(String effectId, BukkitTask task) {
        activeTasks.put(effectId, task);
    }

    public BukkitTask removeActiveTask(String effectId) {
        return activeTasks.remove(effectId);
    }

    public void addAutoRemoveTask(String effectId, BukkitTask task) {
        autoRemoveTasks.put(effectId, task);
    }

    public BukkitTask removeAutoRemoveTask(String effectId) {
        return autoRemoveTasks.remove(effectId);
    }

    public void addRevertTask(Runnable task) {
        revertTasks.add(task);
    }

    public void runRevertTasks() {
        revertTasks.forEach(Runnable::run);
        revertTasks.clear();
    }

    public void setCooldown(String effectId, int seconds) {
        if (seconds <= 0) return;
        cooldowns.put(effectId, System.currentTimeMillis() + (seconds * 1000L));
    }

    public boolean isOnCooldown(String effectId) {
        Long expiry = cooldowns.get(effectId);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            cooldowns.remove(effectId);
            return false;
        }
        return true;
    }

    public long getCooldownRemaining(String effectId) {
        Long expiry = cooldowns.get(effectId);
        if (expiry == null) return 0;
        long remaining = (expiry - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    public void cancelAllTasks() {
        activeTasks.values().forEach(BukkitTask::cancel);
        activeTasks.clear();
        autoRemoveTasks.values().forEach(BukkitTask::cancel);
        autoRemoveTasks.clear();
    }

    public void clear() {
        cancelAllTasks();
        runRevertTasks();
        activeEffects.clear();
        cooldowns.clear();
    }
}
