package gg.leo.IraqueCore.troll;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class TrollEffect {

    protected final String id;
    protected final String name;
    protected final Material icon;
    protected final List<String> description;
    protected final String category;
    protected final String permission;
    protected final int defaultDuration;
    protected final int defaultCooldown;

    public TrollEffect(String id, String name, Material icon, List<String> description,
                       String category, String permission, int defaultDuration, int defaultCooldown) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.category = category;
        this.permission = permission;
        this.defaultDuration = defaultDuration;
        this.defaultCooldown = defaultCooldown;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Material getIcon() { return icon; }
    public List<String> getDescription() { return description; }
    public String getCategory() { return category; }
    public String getPermission() { return permission; }
    public int getDefaultDuration() { return defaultDuration; }
    public int getDefaultCooldown() { return defaultCooldown; }

    public abstract void apply(Player target, TrollManager manager);

    public abstract void revert(Player target, TrollManager manager);

    public boolean requiresTask() { return false; }

    public int getInterval() { return 20; }

    public void onTick(Player target, TrollManager manager) {}

    public boolean isActive(Player target, TrollManager manager) {
        return manager.hasActiveEffect(target, id);
    }
}
