package gg.leo.IraqueCore.chatcolor;

public class ChatColor {

    private final String id;
    private final String displayName;
    private final String chatColor;
    private final String permission;

    public ChatColor(String id, String displayName, String chatColor, String permission) {
        this.id = id;
        this.displayName = displayName;
        this.chatColor = chatColor;
        this.permission = permission;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getChatColor() { return chatColor; }
    public String getPermission() { return permission; }
}
