package gg.leo.IraqueCore.tpa;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPAManager {

    private final IraqueCore plugin;
    private final Map<UUID, TPARequest> pendingRequests = new HashMap<>();
    private final long requestTimeout;

    public TPAManager(IraqueCore plugin) {
        this.plugin = plugin;
        this.requestTimeout = 60000L; // 60 seconds
    }

    public void sendRequest(Player sender, Player target) {
        UUID senderId = sender.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Cancel any existing request from this sender
        pendingRequests.remove(senderId);

        pendingRequests.put(senderId, new TPARequest(senderId, targetId, System.currentTimeMillis()));

        sender.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("tpa.request-sent",
                                "&aTeleport request sent to &e{player}&a.")))
                .replaceText(b -> b.matchLiteral("{player}").replacement(target.getName())));

        target.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("tpa.request-received",
                                "&e{player} &awants to teleport to you. &e/tpa accept &7or &e/tpa deny")))
                .replaceText(b -> b.matchLiteral("{player}").replacement(sender.getName())));
    }

    public boolean acceptRequest(Player target) {
        UUID targetId = target.getUniqueId();
        TPARequest request = findByTarget(targetId);

        if (request == null) {
            target.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("tpa.no-pending",
                                "&cYou have no pending teleport requests."))));
            return false;
        }

        Player sender = Bukkit.getPlayer(request.getSenderId());
        pendingRequests.remove(request.getSenderId());

        if (sender == null || !sender.isOnline()) {
            target.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("tpa.sender-left",
                                "&cThat player is no longer online."))));
            return false;
        }

        sender.teleport(target.getLocation());
        sender.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("tpa.accepted",
                            "&aTeleport request accepted. Teleporting to &e{player}&a.")))
                .replaceText(b -> b.matchLiteral("{player}").replacement(target.getName())));

        target.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("tpa.target-accepted",
                            "&aYou accepted &e{player}&a's teleport request.")))
                .replaceText(b -> b.matchLiteral("{player}").replacement(sender.getName())));

        return true;
    }

    public boolean denyRequest(Player target) {
        UUID targetId = target.getUniqueId();
        TPARequest request = findByTarget(targetId);

        if (request == null) {
            target.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("tpa.no-pending",
                                "&cYou have no pending teleport requests."))));
            return false;
        }

        Player sender = Bukkit.getPlayer(request.getSenderId());
        pendingRequests.remove(request.getSenderId());

        target.sendMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("tpa.denied",
                            "&cTeleport request denied."))));

        if (sender != null && sender.isOnline()) {
            sender.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("tpa.target-denied",
                                "&e{player} &crejected your teleport request.")))
                    .replaceText(b -> b.matchLiteral("{player}").replacement(target.getName())));
        }

        return true;
    }

    public boolean hasPendingRequest(Player target) {
        return findByTarget(target.getUniqueId()) != null;
    }

    public void cleanupExpired() {
        long now = System.currentTimeMillis();
        pendingRequests.entrySet().removeIf(entry -> {
            if (now - entry.getValue().getTimestamp() > requestTimeout) {
                Player sender = Bukkit.getPlayer(entry.getKey());
                if (sender != null && sender.isOnline()) {
                    sender.sendMessage(plugin.getConfigManager().deserialize(
                            plugin.getConfigManager().translate(
                                    plugin.getConfigManager().getMessage("tpa.expired",
                                        "&cYour teleport request has expired."))));
                }
                return true;
            }
            return false;
        });
    }

    private TPARequest findByTarget(UUID targetId) {
        for (TPARequest request : pendingRequests.values()) {
            if (request.getTargetId().equals(targetId)) {
                // Check if expired
                if (System.currentTimeMillis() - request.getTimestamp() > requestTimeout) {
                    pendingRequests.remove(request.getSenderId());
                    return null;
                }
                return request;
            }
        }
        return null;
    }

    private static class TPARequest {
        private final UUID senderId;
        private final UUID targetId;
        private final long timestamp;

        public TPARequest(UUID senderId, UUID targetId, long timestamp) {
            this.senderId = senderId;
            this.targetId = targetId;
            this.timestamp = timestamp;
        }

        public UUID getSenderId() { return senderId; }
        public UUID getTargetId() { return targetId; }
        public long getTimestamp() { return timestamp; }
    }
}
