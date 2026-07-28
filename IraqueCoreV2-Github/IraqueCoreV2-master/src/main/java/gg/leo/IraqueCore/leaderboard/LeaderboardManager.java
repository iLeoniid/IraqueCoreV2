package gg.leo.IraqueCore.leaderboard;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.menu.LeaderboardMenu;
import org.bukkit.entity.Player;

public class LeaderboardManager {

    private final LeaderboardMenu menu;

    public LeaderboardManager(IraqueCore plugin) {
        this.menu = new LeaderboardMenu(plugin);
    }

    public void openMainMenu(Player player) {
        menu.openMain(player);
    }
}
