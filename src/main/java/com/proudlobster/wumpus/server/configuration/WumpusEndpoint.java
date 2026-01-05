package com.proudlobster.wumpus.server.configuration;

import com.proudlobster.wumpus.core.error.OperatingError;

import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

public class WumpusEndpoint extends Endpoint {

    private final SessionHandler sessions;

    public WumpusEndpoint(final SessionHandler sessions) {
        this.sessions = sessions;
    }

    @Override
    public void onOpen(final Session session, final EndpointConfig config) {
        sessions.register(session);
    }

    @Override
    public void onError(final Session session, final Throwable thr) {
        throw new OperatingError("Session error.", thr);
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        sessions.close(session);
    }

}
