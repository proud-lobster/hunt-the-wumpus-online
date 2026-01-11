package com.proudlobster.wumpus.core.worker;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public abstract class SingleItemWorker<W> extends ScheduledWorker<W> {

    final AtomicReference<W> itemRef = new AtomicReference<>(null);

    protected SingleItemWorker(long interval) {
        super(interval);
    }

    @Override
    public Optional<W> fetch() {
        return Optional.ofNullable(itemRef.get());
    }

    @Override
    public void submit(W item) {
        itemRef.set(item);
    }

}
