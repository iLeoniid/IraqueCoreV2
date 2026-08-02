package gg.leo.IraqueCore.tpa;

import gg.leo.IraqueCore.IraqueCore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TPAManager {
    private final IraqueCore plugin;
    private final Map<UUID, TPARequest> pendingRequests = new HashMap<>();
    private final long requestTimeout;

    public TPAManager(IraqueCore plugin) {
        this.plugin = plugin;
        this.requestTimeout = 60000L;
    }

    public void sendRequest(Player sender, Player target) {
        UUID senderId = sender.getUniqueId();
        UUID targetId = target.getUniqueId();
        this.pendingRequests.remove(senderId);
        this.pendingRequests.put(senderId, new TPARequest(senderId, targetId, System.currentTimeMillis()));
        sender.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("tpa.request-sent", "&aTeleport request sent to &e{player}&a."))).replaceText(b -> b.matchLiteral("{player}").replacement(target.getName())));
        target.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("tpa.request-received", "&e{player} &awants to teleport to you. &e/tpa accept &7or &e/tpa deny"))).replaceText(b -> b.matchLiteral("{player}").replacement(sender.getName())));
    }

    public boolean acceptRequest(Player target) {
        UUID targetId = target.getUniqueId();
        TPARequest request = this.findByTarget(targetId);
        if (request == null) {
            target.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("tpa.no-pending", "&cYou have no pending teleport requests."))));
            return false;
        }
        Player sender = Bukkit.getPlayer(request.getSenderId());
        this.pendingRequests.remove(request.getSenderId());
        if (sender == null || !sender.isOnline()) {
            target.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("tpa.sender-left", "&cThat player is no longer online."))));
            return false;
        }
        plugin.getTeleportManager().requestTeleport(sender, target.getLocation());
        sender.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("tpa.accepted", "&aTeleport request accepted. Teleporting to &e{player}&a."))).replaceText(b -> b.matchLiteral("{player}").replacement(target.getName())));
        target.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("tpa.target-accepted", "&aYou accepted &e{player}&a's teleport request."))).replaceText(b -> b.matchLiteral("{player}").replacement(sender.getName())));
        return true;
    }

    public boolean denyRequest(Player target) {
        UUID targetId = target.getUniqueId();
        TPARequest request = this.findByTarget(targetId);
        if (request == null) {
            target.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("tpa.no-pending", "&cYou have no pending teleport requests."))));
            return false;
        }
        Player sender = Bukkit.getPlayer(request.getSenderId());
        this.pendingRequests.remove(request.getSenderId());
        target.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("tpa.denied", "&cTeleport request denied."))));
        if (sender != null && sender.isOnline()) {
            sender.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("tpa.target-denied", "&e{player} &crejected your teleport request."))).replaceText(b -> b.matchLiteral("{player}").replacement(target.getName())));
        }
        return true;
    }

    public boolean hasPendingRequest(Player target) {
        return this.findByTarget(target.getUniqueId()) != null;
    }

    public void cleanupExpired() {
        long now = System.currentTimeMillis();
        this.pendingRequests.entrySet().removeIf(entry -> {
            if (now - entry.getValue().getTimestamp() > this.requestTimeout) {
                Player sender = Bukkit.getPlayer(entry.getKey());
                if (sender != null && sender.isOnline()) {
                    sender.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("tpa.expired", "&cYour teleport request has expired."))));
                }
                return true;
            }
            return false;
        });
    }

    private TPARequest findByTarget(UUID targetId) {
        for (TPARequest request : this.pendingRequests.values()) {
            if (!request.getTargetId().equals(targetId)) continue;
            if (System.currentTimeMillis() - request.getTimestamp() > this.requestTimeout) {
                this.pendingRequests.remove(request.getSenderId());
                return null;
            }
            return request;
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

        public UUID getSenderId() {
            return this.senderId;
        }

        public UUID getTargetId() {
            return this.targetId;
        }

        public long getTimestamp() {
            return this.timestamp;
        }
    }
}
