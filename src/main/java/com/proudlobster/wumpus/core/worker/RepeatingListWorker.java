package com.proudlobster.wumpus.core.worker;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.LoggerFactory;

public abstract class RepeatingListWorker<W> extends ScheduledWorker<W> {

    private final List<W> items;

    public RepeatingListWorker(final long interval) {
        super(interval);
        this.items = new CopyOnWriteArrayList<>();
    }

    public RepeatingListWorker(final long interval, final List<W> items) {
        super(interval);
        this.items = items;
    }

    @Override
    public void run() {
        try {
            items.stream().forEach(this::work);
        } catch (final Exception e) {
            LoggerFactory.getLogger("WORKER").error("Worker encountered an error: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<W> fetch() {
        return Optional.empty();
    }

    @Override
    public void submit(final W item) {
        items.add(item);
    }
}
