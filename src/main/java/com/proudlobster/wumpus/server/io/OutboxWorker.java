package com.proudlobster.wumpus.server.io;

import com.proudlobster.wumpus.core.worker.QueueWorker;
import com.proudlobster.wumpus.server.configuration.SessionHandler;

public class OutboxWorker extends QueueWorker<ClientMessage> {

    private final SessionHandler sessions;

    public OutboxWorker(final long interval, final SessionHandler sessions) {
        super(interval);
        this.sessions = sessions;
    }

    @Override
    public void work(ClientMessage item) {
        sessions.send(item);
    }
}
