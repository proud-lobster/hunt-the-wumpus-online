package com.proudlobster.wumpus.server.io;

import com.proudlobster.wumpus.core.worker.QueueWorker;
import com.proudlobster.wumpus.core.worker.Worker;

public class InboxWorker extends QueueWorker<ClientMessage> {

    private final Worker<ClientMessage> outbox;

    public InboxWorker(final long interval, final Worker<ClientMessage> outbox) {
        super(interval);
        this.outbox = outbox;
    }

    @Override
    public void work(ClientMessage item) {
        item.directive().handle(item).ifPresent(outbox::submit);
    }
}
