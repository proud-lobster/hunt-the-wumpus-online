package com.proudlobster.wumpus.core.worker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ScheduledWorker<W> implements Worker<W> {

    private final ScheduledExecutorService exec;
    private final long interval;

    protected ScheduledWorker(final long interval) {
        exec = Executors.newScheduledThreadPool(1);
        this.interval = interval;
    }

    public void start() {
        exec.scheduleAtFixedRate(this::run, 0, interval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        exec.shutdown();
    }
}
