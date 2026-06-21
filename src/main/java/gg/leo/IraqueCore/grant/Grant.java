package gg.leo.IraqueCore.grant;

import java.util.UUID;

public class Grant {

    private final UUID id;
    private final UUID target;
    private final UUID granter;
    private final String rankName;
    private final String reason;
    private final long addedAt;
    private final long duration;
    private boolean revoked;
    private UUID revokedBy;
    private String revokeReason;
    private long revokedAt;

    public Grant(UUID id, UUID target, UUID granter, String rankName,
                 String reason, long addedAt, long duration) {
        this.id = id;
        this.target = target;
        this.granter = granter;
        this.rankName = rankName;
        this.reason = reason;
        this.addedAt = addedAt;
        this.duration = duration;
        this.revoked = false;
    }

    public UUID getId() { return id; }
    public UUID getTarget() { return target; }
    public UUID getGranter() { return granter; }
    public String getRankName() { return rankName; }
    public String getReason() { return reason; }
    public long getAddedAt() { return addedAt; }
    public long getDuration() { return duration; }
    public boolean isRevoked() { return revoked; }
    public UUID getRevokedBy() { return revokedBy; }
    public String getRevokeReason() { return revokeReason; }
    public long getRevokedAt() { return revokedAt; }

    public void revoke(UUID revokedBy, String reason) {
        this.revoked = true;
        this.revokedBy = revokedBy;
        this.revokeReason = reason;
        this.revokedAt = System.currentTimeMillis();
    }

    public boolean isActive() {
        if (revoked) return false;
        if (duration == Long.MAX_VALUE) return true;
        return (System.currentTimeMillis() - addedAt) < duration;
    }

    public long getRemaining() {
        if (revoked || duration == Long.MAX_VALUE) return Long.MAX_VALUE;
        long remaining = duration - (System.currentTimeMillis() - addedAt);
        return Math.max(remaining, 0);
    }

    public boolean isPermanent() {
        return duration == Long.MAX_VALUE;
    }

    public boolean isExpired() {
        return !revoked && !isActive();
    }
}
