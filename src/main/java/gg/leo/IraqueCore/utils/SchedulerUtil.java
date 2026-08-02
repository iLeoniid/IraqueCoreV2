package gg.leo.IraqueCore.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Punto único de acceso al scheduler de Bukkit.
 *
 * <p>Centraliza {@code Bukkit.getScheduler()} para mantener un estilo uniforme
 * en todo el plugin y poder adaptar la implementación a futuro (p. ej. Folia).
 * Si el callback ya se está ejecutando en el hilo principal, {@code runSync}
 * lo ejecuta de inmediato en vez de encolarlo.</p>
 */
public final class SchedulerUtil {

    private SchedulerUtil() {}

    public static void runSync(Plugin plugin, Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runAsync(Plugin plugin, Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public static BukkitTask runLater(Plugin plugin, Runnable task, long delay) {
        return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    public static BukkitTask runLaterAsync(Plugin plugin, Runnable task, long delay) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
    }

    public static BukkitTask runTimer(Plugin plugin, Runnable task, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
    }

    public static BukkitTask runTimerAsync(Plugin plugin, Runnable task, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
    }

    public static void cancelAll(Plugin plugin) {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
