package gg.leo.IraqueCore.msg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MsgManager {

    private final Map<UUID, UUID> lastMessagers = new HashMap<>();

    public void setLastMessager(UUID target, UUID sender) {
        lastMessagers.put(target, sender);
    }

    public Optional<UUID> getLastMessager(UUID player) {
        return Optional.ofNullable(lastMessagers.get(player));
    }

    public void remove(UUID player) {
        lastMessagers.remove(player);
    }
}
