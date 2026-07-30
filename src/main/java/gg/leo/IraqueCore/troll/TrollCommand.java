package gg.leo.IraqueCore.troll;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrollCommand implements TabExecutor {

    private final TrollManager manager;
    private final TrollMenu menu;

    public TrollCommand(TrollManager manager, TrollMenu menu) {
        this.manager = manager;
        this.menu = menu;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("troll.use")) {
            player.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "undo" -> handleUndo(player, args);
            case "reload" -> handleReload(player);
            case "toggle-troll-op" -> handleToggleTrollOp(player);
            case "add-blocked" -> handleAddBlocked(player, args);
            case "remove-blocked" -> handleRemoveBlocked(player, args);
            case "giveskull" -> handleGiveSkull(player, args);
            default -> {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found.");
                    return true;
                }
                menu.openMainMenu(player, target);
            }
        }
        return true;
    }

    private void handleUndo(Player player, String[] args) {
        if (!player.hasPermission("troll.undo")) {
            player.sendMessage("§cYou don't have permission to undo troll effects.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsage: /troll undo <player>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found.");
            return;
        }
        manager.undoAll(target);
        player.sendMessage("§aAll troll effects removed from §e" + target.getName() + "§a.");
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("troll.reload")) {
            player.sendMessage("§cYou don't have permission.");
            return;
        }
        manager.reloadConfig();
        player.sendMessage("§aTroll config reloaded.");
    }

    private void handleToggleTrollOp(Player player) {
        if (!player.hasPermission("troll.reload")) {
            player.sendMessage("§cYou don't have permission.");
            return;
        }
        boolean current = manager.isAllowTrollOp();
        manager.setAllowTrollOp(!current);
        player.sendMessage("§aTroll-op " + (!current ? "§aenabled" : "§cdisabled") + "§a.");
    }

    private void handleAddBlocked(Player player, String[] args) {
        if (!player.hasPermission("troll.reload")) {
            player.sendMessage("§cYou don't have permission.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsage: /troll add-blocked <player>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found.");
            return;
        }
        manager.addBlocked(target);
        player.sendMessage("§aAdded §e" + target.getName() + " §ato the blocklist.");
    }

    private void handleRemoveBlocked(Player player, String[] args) {
        if (!player.hasPermission("troll.reload")) {
            player.sendMessage("§cYou don't have permission.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsage: /troll remove-blocked <player>");
            return;
        }
        manager.removeBlocked(args[1]);
        player.sendMessage("§aRemoved §e" + args[1] + " §afrom the blocklist.");
    }

    private void handleGiveSkull(Player player, String[] args) {
        if (!player.hasPermission("troll.reload")) {
            player.sendMessage("§cYou don't have permission.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsage: /troll giveskull <player>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found.");
            return;
        }
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setPlayerProfile(target.getPlayerProfile());
        skull.setItemMeta(meta);
        player.getInventory().addItem(skull);
        player.sendMessage("§aGiven skull of §e" + target.getName() + "§a.");
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6===== Troll Help =====");
        player.sendMessage("§e/troll <player> §7- Open troll menu");
        player.sendMessage("§e/troll undo <player> §7- Remove all troll effects");
        player.sendMessage("§e/troll reload §7- Reload troll config");
        player.sendMessage("§e/troll toggle-troll-op §7- Toggle op troll bypass");
        player.sendMessage("§e/troll add-blocked <player> §7- Block a player from being trolled");
        player.sendMessage("§e/troll remove-blocked <player> §7- Unblock a player");
        player.sendMessage("§e/troll giveskull <player> §7- Get player skull");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                       @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(List.of(
                    "undo", "reload", "toggle-troll-op", "add-blocked", "remove-blocked", "giveskull"));
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList());
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("undo")
                || args[0].equalsIgnoreCase("add-blocked")
                || args[0].equalsIgnoreCase("remove-blocked")
                || args[0].equalsIgnoreCase("giveskull"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
