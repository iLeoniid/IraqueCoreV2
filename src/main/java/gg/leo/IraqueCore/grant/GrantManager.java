package gg.leo.IraqueCore.grant;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.rank.Rank;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GrantManager {

    private final IraqueCore plugin;
    private final Map<UUID, List<Grant>> grants = new HashMap<>();
    private File dataFile;
    private int taskId = -1;

    public GrantManager(IraqueCore plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "grants.yml");
    }

    public void load() {
        grants.clear();
        if (!dataFile.exists()) return;
        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : data.getKeys(false)) {
            try {
                UUID target = UUID.fromString(key);
                List<Map<?, ?>> raw = data.getMapList(key);
                List<Grant> playerGrants = new ArrayList<>();
                for (Map<?, ?> map : raw) {
                    Grant grant = deserialize(map);
                    if (grant != null) playerGrants.add(grant);
                }
                grants.put(target, playerGrants);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void saveAll() {
        YamlConfiguration data = new YamlConfiguration();
        for (Map.Entry<UUID, List<Grant>> entry : grants.entrySet()) {
            List<Map<String, Object>> raw = entry.getValue().stream()
                    .map(this::serialize)
                    .collect(Collectors.toList());
            data.set(entry.getKey().toString(), raw);
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save grants", e);
        }
    }

    public Grant grant(UUID target, UUID granter, String rankName, String reason, long duration) {
        UUID id = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong());
        Grant grant = new Grant(id, target, granter, rankName, reason, System.currentTimeMillis(), duration);
        grants.computeIfAbsent(target, k -> new ArrayList<>()).add(grant);
        saveAll();
        applyGrant(target);
        return grant;
    }

    public boolean revoke(UUID target, UUID revoker, String reason) {
        List<Grant> playerGrants = grants.get(target);
        if (playerGrants == null) return false;
        boolean revoked = false;
        for (Grant g : playerGrants) {
            if (g.isActive()) {
                g.revoke(revoker, reason);
                revoked = true;
            }
        }
        if (revoked) {
            saveAll();
            removeGrant(target);
        }
        return revoked;
    }

    public boolean revokeSpecific(UUID grantId, UUID revoker, String reason) {
        for (List<Grant> list : grants.values()) {
            for (Grant g : list) {
                if (g.getId().equals(grantId) && g.isActive()) {
                    g.revoke(revoker, reason);
                    saveAll();
                    applyOrRemoveForTarget(g.getTarget());
                    return true;
                }
            }
        }
        return false;
    }

    public List<Grant> getActiveGrants(UUID target) {
        List<Grant> playerGrants = grants.get(target);
        if (playerGrants == null) return List.of();
        return playerGrants.stream().filter(Grant::isActive).collect(Collectors.toList());
    }

    public List<Grant> getAllGrants(UUID target) {
        List<Grant> playerGrants = grants.get(target);
        return playerGrants == null ? List.of() : new ArrayList<>(playerGrants);
    }

    public Optional<Grant> getHighestActiveGrant(UUID target) {
        return getActiveGrants(target).stream()
                .max(Comparator.comparingInt(g -> plugin.getRankManager()
                        .getRank(g.getRankName()).map(Rank::weight).orElse(-1)));
    }

    public void applyGrant(UUID target) {
        Player player = plugin.getServer().getPlayer(target);
        if (player == null) return;

        getHighestActiveGrant(target).ifPresent(grant -> {
            plugin.getRankManager().getRank(grant.getRankName()).ifPresent(rank -> {
                plugin.getRankManager().setRank(target, rank.name());
                plugin.getRankManager().applyPermissions(player);
                plugin.getRankManager().updatePlayerRankVisuals(player);
            });
        });
    }

    public void removeGrant(UUID target) {
        Player player = plugin.getServer().getPlayer(target);
        if (player == null) return;

        String defaultRank = plugin.getConfigManager().getDefaultRankName();
        plugin.getRankManager().setRank(target, defaultRank);
        plugin.getRankManager().applyPermissions(player);
        plugin.getRankManager().updatePlayerRankVisuals(player);
    }

    private void applyOrRemoveForTarget(UUID target) {
        if (getActiveGrants(target).isEmpty()) {
            removeGrant(target);
        } else {
            applyGrant(target);
        }
    }

    public void checkExpirations() {
        for (Map.Entry<UUID, List<Grant>> entry : new HashMap<>(grants).entrySet()) {
            boolean changed = false;
            for (Grant g : entry.getValue()) {
                if (g.isExpired()) {
                    g.revoke(UUID.fromString("00000000-0000-0000-0000-000000000000"), "Expired");
                    changed = true;
                }
            }
            if (changed) {
                saveAll();
                applyOrRemoveForTarget(entry.getKey());
            }
        }
    }

    public void startTask() {
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::checkExpirations, 600L, 600L).getTaskId();
    }

    private Map<String, Object> serialize(Grant g) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", g.getId().toString());
        map.put("granter", g.getGranter().toString());
        map.put("rank", g.getRankName());
        map.put("reason", g.getReason());
        map.put("addedAt", g.getAddedAt());
        map.put("duration", g.getDuration());
        map.put("revoked", g.isRevoked());
        if (g.isRevoked()) {
            map.put("revokedBy", g.getRevokedBy().toString());
            map.put("revokeReason", g.getRevokeReason());
            map.put("revokedAt", g.getRevokedAt());
        }
        return map;
    }

    private Grant deserialize(Map<?, ?> map) {
        try {
            UUID id = UUID.fromString((String) map.get("id"));
            UUID target = UUID.fromString((String) map.get("target"));
            UUID granter = UUID.fromString((String) map.get("granter"));
            String rank = (String) map.get("rank");
            String reason = (String) map.get("reason");
            long addedAt = ((Number) map.get("addedAt")).longValue();
            long duration = ((Number) map.get("duration")).longValue();

            Grant grant = new Grant(id, target, granter, rank, reason, addedAt, duration);
            if (map.containsKey("revoked") && Boolean.TRUE.equals(map.get("revoked"))) {
                UUID revokedBy = UUID.fromString((String) map.get("revokedBy"));
                String revokeReason = (String) map.get("revokeReason");
                long revokedAt = ((Number) map.get("revokedAt")).longValue();
                grant.revoke(revokedBy, revokeReason);
            }
            return grant;
        } catch (Exception e) {
            return null;
        }
    }

    public Set<UUID> getActiveGrantTargets() {
        Set<UUID> targets = new HashSet<>();
        for (Map.Entry<UUID, List<Grant>> entry : grants.entrySet()) {
            for (Grant g : entry.getValue()) {
                if (g.isActive()) {
                    targets.add(entry.getKey());
                    break;
                }
            }
        }
        return targets;
    }
}
