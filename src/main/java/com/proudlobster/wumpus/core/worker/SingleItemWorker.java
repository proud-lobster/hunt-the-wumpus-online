package com.proudlobster.wumpus.core.worker;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.LoggerFactory;

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

    @Override
    public void run() {
        System.out.println("Running single item worker at " + System.currentTimeMillis());
        try {
            fetch().ifPresent(this::work);
        } catch (final Exception e) {
            LoggerFactory.getLogger("WORKER").error("Worker encountered an error: " + e.getMessage(), e);
        }
    }

}
