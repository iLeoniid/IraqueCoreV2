package gg.leo.IraqueCore.troll;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UntrollCommand implements TabExecutor {

    private final TrollManager manager;

    public UntrollCommand(TrollManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUso: /untroll <jogador> [troll] ou /untroll all");
            return true;
        }

        if (args[0].equalsIgnoreCase("all")) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                manager.undoAll(online);
            }
            sender.sendMessage("§aTodos os efeitos de troll removidos de todos os jogadores.");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cJogador nao encontrado.");
            return true;
        }

        if (args.length >= 2) {
            String effectId = args[1].toLowerCase();
            if (manager.hasActiveEffect(target, effectId)) {
                manager.removeEffect(target, effectId, false);
                sender.sendMessage("§aRemovido §e" + effectId + " §ade §e" + target.getName());
            } else {
                sender.sendMessage("§c" + target.getName() + " nao tem esse efeito ativo.");
            }
        } else {
            manager.undoAll(target);
            sender.sendMessage("§aTodos os efeitos de troll removidos de §e" + target.getName());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                       @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("all");
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList());
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                return manager.getActiveEffects(target).stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }
}
