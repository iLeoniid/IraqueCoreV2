package gg.leo.IraqueCore.utils.menu.buttons;

import gg.leo.IraqueCore.utils.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Toggle button that shows enabled/disabled state and runs an action on click.
 * Call {@code refresh.accept(slot)} from the action if you need an in-place redraw,
 * or use {@link gg.leo.IraqueCore.utils.menu.Menu#updateSlot(int)}.
 */
public class ToggleButton extends Button {

    private final Material material;
    private final String name;
    private final BooleanSupplier state;
    private final Consumer<Player> onToggle;
    private final String enabledLabel;
    private final String disabledLabel;

    public ToggleButton(Material material, String name, BooleanSupplier state, Consumer<Player> onToggle) {
        this(material, name, state, onToggle, "&a&l✔ Enabled", "&c&l✘ Disabled");
    }

    public ToggleButton(Material material, String name, BooleanSupplier state, Consumer<Player> onToggle,
                        String enabledLabel, String disabledLabel) {
        this.material = material;
        this.name = name;
        this.state = state;
        this.onToggle = onToggle;
        this.enabledLabel = enabledLabel;
        this.disabledLabel = disabledLabel;
    }

    @Override
    public Material getMaterial(Player player) {
        return material;
    }

    @Override
    public String getDisplayName(Player player) {
        return (state.getAsBoolean() ? "&a" : "&c") + name;
    }

    @Override
    public List<String> getDescription(Player player) {
        boolean enabled = state.getAsBoolean();
        return List.of(
                "",
                enabled ? enabledLabel : disabledLabel,
                "",
                "&7Click to " + (enabled ? "&cdisable" : "&aenable") + "&7 this feature",
                ""
        );
    }

    @Override
    public void onClick(Player player, int slot, ClickType type) {
        if (onToggle != null) {
            onToggle.accept(player);
        }
    }
}
