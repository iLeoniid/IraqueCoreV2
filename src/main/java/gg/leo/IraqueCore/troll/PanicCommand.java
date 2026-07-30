package gg.leo.IraqueCore.troll;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PanicCommand implements CommandExecutor {

    private final TrollManager manager;
    private final Set<UUID> pendingConfirmation = new HashSet<>();

    public PanicCommand(TrollManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("troll.panic")) {
            player.sendMessage("§cYou don't have permission.");
            return true;
        }

        if (pendingConfirmation.contains(player.getUniqueId())) {
            pendingConfirmation.remove(player.getUniqueId());
            Bukkit.getScheduler().cancelTasks(manager.getPlugin());
            for (Player online : Bukkit.getOnlinePlayers()) {
                manager.undoAll(online);
            }
            Bukkit.broadcast(Component.text(ChatColor.RED + "PANIC STOP: All troll effects have been forcefully removed!"));
            return true;
        }

        pendingConfirmation.add(player.getUniqueId());
        player.sendMessage("§c\u00a1PANIC! Type /panicstoptroll again to confirm and stop ALL trolls!");
        Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () ->
                pendingConfirmation.remove(player.getUniqueId()), 200L);
        return true;
    }
}
