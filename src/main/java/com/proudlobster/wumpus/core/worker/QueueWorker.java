package com.proudlobster.wumpus.core.worker;

import java.util.Optional;
import java.util.Queue;
import com.proudlobster.wumpus.core.utility.DeduplicatingQueue;

public abstract class QueueWorker<W> extends ScheduledWorker<W> {

    private final Queue<W> q;

    public QueueWorker(final long interval) {
        super(interval);
        this.q = new DeduplicatingQueue<>();
    }

    @Override
    public Optional<W> fetch() {
        return Optional.ofNullable(q.poll());
    }

    @Override
    public void submit(final W item) {
        q.offer(item);
    }
}
