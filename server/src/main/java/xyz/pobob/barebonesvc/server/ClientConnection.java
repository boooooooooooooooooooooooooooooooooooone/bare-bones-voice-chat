package xyz.pobob.barebonesvc.server;

import java.util.UUID;

public class ClientConnection {

    private final String username;
    private final UUID uuid;
    private long lastKeepAliveResponse;

    public ClientConnection(String username, UUID uuid) {
        this.username = username;
        this.uuid = uuid;
        this.lastKeepAliveResponse = System.currentTimeMillis();
    }

    public String getUsername() {
        return this.username;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public synchronized long getLastKeepAliveSynced() {
        return lastKeepAliveResponse;
    }

    public void setLastKeepAliveResponse(long lastKeepAliveResponse) {
        this.lastKeepAliveResponse = lastKeepAliveResponse;
    }

}
