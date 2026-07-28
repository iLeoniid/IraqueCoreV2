package gg.leo.IraqueCore.settings;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import gg.leo.IraqueCore.utils.menu.buttons.ToggleButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SettingsMenu extends Menu {

    private final IraqueCore plugin;

    public SettingsMenu(IraqueCore plugin, Player player) {
        super(player);
        this.plugin = plugin;
        this.staticSize = 36;
        this.placeholder = true;
    }

    @Override
    public String getTitle(Player player) {
        return "&6&lSettings";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new LinkedHashMap<>();

        buttons.put(10, toggle(Material.PAPER, "Scoreboard", "scoreboard.enabled", true));
        buttons.put(11, toggle(Material.RED_BED, "AFK System", "afk.enabled", true));
        buttons.put(12, toggle(Material.NETHER_STAR, "Sleep Voting", "sleep.enabled", true));
        buttons.put(13, toggle(Material.CLOCK, "Playtime Tracking", "playtime.enabled", true));
        buttons.put(14, toggle(Material.ANVIL, "Anvil Colors", "anvil.enabled", true));
        buttons.put(15, toggle(Material.ARMOR_STAND, "Armor Stand Editor", "armorstand.enabled", true));
        buttons.put(16, toggle(Material.DIAMOND_PICKAXE, "Durability Warning", "durability-warning.enabled", true));

        buttons.put(20, toggle(Material.MAP, "Image MOTD", "motd-image.enabled", false));
        buttons.put(22, toggle(Material.CHEST, "Graves", "grave.enabled", true));

        buttons.put(31, Button.of(Material.BARRIER, "&cClose",
                List.of("", "&7Click to close"), p -> p.closeInventory()));

        return buttons;
    }

    private Button toggle(Material material, String name, String path, boolean defaultValue) {
        return new ToggleButton(
                material,
                name,
                () -> plugin.getConfig().getBoolean(path, defaultValue),
                null
        ) {
            @Override
            public void onClick(Player player, int slot, ClickType type) {
                boolean current = plugin.getConfig().getBoolean(path, defaultValue);
                plugin.getConfig().set(path, !current);
                plugin.saveConfig();
                plugin.reload();
                updateSlot(slot);
            }
        };
    }
}
