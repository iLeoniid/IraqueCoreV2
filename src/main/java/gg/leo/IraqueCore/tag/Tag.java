package gg.leo.IraqueCore.tag;

import org.bukkit.Material;

import java.util.List;

public class Tag {
    private String id;
    private String displayName;
    private String tag;
    private String permission;
    private Material material;
    private List<String> lore;
    private String category;

    public Tag(String id, String displayName, String tag, String permission, Material material, List<String> lore) {
        this.id = id;
        this.displayName = displayName;
        this.tag = tag;
        this.permission = permission;
        this.material = material;
        this.lore = lore;
        this.category = "emojis";
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getTag() { return tag; }
    public String getPermission() { return permission; }
    public Material getMaterial() { return material; }
    public List<String> getLore() { return lore; }
    public String getCategory() { return category != null ? category : "emojis"; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setTag(String tag) { this.tag = tag; }
    public void setPermission(String permission) { this.permission = permission; }
    public void setMaterial(Material material) { this.material = material; }
    public void setLore(List<String> lore) { this.lore = lore; }
    public void setCategory(String category) { this.category = category; }
}
