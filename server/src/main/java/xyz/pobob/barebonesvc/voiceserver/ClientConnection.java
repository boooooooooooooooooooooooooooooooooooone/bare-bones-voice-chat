package xyz.pobob.barebonesvc.voiceserver;

import java.util.UUID;

public class ClientConnection {

    private final String username;
    private final UUID uuid;
    private boolean disabled;
    private long lastKeepAliveResponse;

    public ClientConnection(String username, UUID uuid, boolean disabled) {
        this.username = username;
        this.uuid = uuid;
        this.disabled = disabled;
        this.lastKeepAliveResponse = System.currentTimeMillis();
    }

    public String getUsername() {
        return this.username;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean val) {
        this.disabled = val;
    }

    public synchronized long getLastKeepAliveSynced() {
        return lastKeepAliveResponse;
    }

    public void setLastKeepAliveResponse(long lastKeepAliveResponse) {
        this.lastKeepAliveResponse = lastKeepAliveResponse;
    }

}
