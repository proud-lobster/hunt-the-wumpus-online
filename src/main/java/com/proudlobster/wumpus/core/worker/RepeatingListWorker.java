package com.proudlobster.wumpus.core.worker;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class RepeatingListWorker<W> extends ScheduledWorker<W> {

    private final List<W> items;
    private int currentIndex = 0;

    public RepeatingListWorker(final long interval) {
        super(interval);
        this.items = new CopyOnWriteArrayList<>();
    }

    public RepeatingListWorker(final long interval, final List<W> items) {
        super(interval);
        this.items = items;
    }

    @Override
    public Optional<W> fetch() {
        if (items.isEmpty()) {
            return Optional.empty();
        }
        W item = items.get(currentIndex);
        currentIndex = (currentIndex + 1) % items.size();
        return Optional.of(item);
    }

    @Override
    public void submit(final W item) {
        items.add(item);
    }
}
