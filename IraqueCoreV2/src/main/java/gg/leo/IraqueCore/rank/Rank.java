package gg.leo.IraqueCore.rank;

import java.util.List;

public record Rank(
        String name,
        String prefix,
        String suffix,
        int weight,
        String color,
        List<String> permissions
) {
    public boolean hasPermission(String permission) {
        return permissions.contains("*") || permissions.contains(permission);
    }
}
