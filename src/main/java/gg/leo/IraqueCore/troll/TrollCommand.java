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
            sender.sendMessage("§cApenas jogadores podem usar esse comando.");
            return true;
        }

        if (!player.hasPermission("troll.use")) {
            player.sendMessage("§cVoce nao tem permissao para usar esse comando.");
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
                    player.sendMessage("§cJogador nao encontrado.");
                    return true;
                }
                menu.openMainMenu(player, target);
            }
        }
        return true;
    }

    private void handleUndo(Player player, String[] args) {
        if (!player.hasPermission("troll.undo")) {
            player.sendMessage("§cVoce nao tem permissao para remover efeitos de troll.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cUso: /troll undo <jogador>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cJogador nao encontrado.");
            return;
        }
        manager.undoAll(target);
        player.sendMessage("§aTodos os efeitos de troll removidos de §e" + target.getName() + "§a.");
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("troll.reload")) {
            player.sendMessage("§cVoce nao tem permissao.");
            return;
        }
        manager.reloadConfig();
        player.sendMessage("§aConfiguracao de troll recarregada.");
    }

    private void handleToggleTrollOp(Player player) {
        if (!player.hasPermission("troll.reload")) {
            player.sendMessage("§cVoce nao tem permissao.");
            return;
        }
        boolean current = manager.isAllowTrollOp();
        manager.setAllowTrollOp(!current);
        player.sendMessage("§aTroll-op " + (!current ? "§aativo" : "§cdesativado") + "§a.");
    }

    private void handleAddBlocked(Player player, String[] args) {
        if (!player.hasPermission("troll.reload")) {
            player.sendMessage("§cVoce nao tem permissao.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cUso: /troll add-blocked <jogador>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cJogador nao encontrado.");
            return;
        }
        manager.addBlocked(target);
        player.sendMessage("§aAdicionado §e" + target.getName() + " §aa lista de bloqueio.");
    }

    private void handleRemoveBlocked(Player player, String[] args) {
        if (!player.hasPermission("troll.reload")) {
            player.sendMessage("§cVoce nao tem permissao.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cUso: /troll remove-blocked <jogador>");
            return;
        }
        manager.removeBlocked(args[1]);
        player.sendMessage("§aRemovido §e" + args[1] + " §ada lista de bloqueio.");
    }

    private void handleGiveSkull(Player player, String[] args) {
        if (!player.hasPermission("troll.reload")) {
            player.sendMessage("§cVoce nao tem permissao.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cUso: /troll giveskull <jogador>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cJogador nao encontrado.");
            return;
        }
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setPlayerProfile(target.getPlayerProfile());
        skull.setItemMeta(meta);
        player.getInventory().addItem(skull);
        player.sendMessage("§aVoce recebeu a cabeca de §e" + target.getName() + "§a.");
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6===== Ajuda Troll =====");
        player.sendMessage("§e/troll <jogador> §7- Abrir menu de troll");
        player.sendMessage("§e/troll undo <jogador> §7- Remover todos os efeitos");
        player.sendMessage("§e/troll reload §7- Recarregar configuracao");
        player.sendMessage("§e/troll toggle-troll-op §7- Ativar/desativar troll-op");
        player.sendMessage("§e/troll add-blocked <jogador> §7- Bloquear jogador de ser trollado");
        player.sendMessage("§e/troll remove-blocked <jogador> §7- Desbloquear jogador");
        player.sendMessage("§e/troll giveskull <jogador> §7- Pegar cabeca do jogador");
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
