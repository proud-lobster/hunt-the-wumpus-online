package com.proudlobster.wumpus.core.worker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ScheduledWorker<W> implements Worker<W> {

    private final ScheduledExecutorService exec;
    private final long interval;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private ScheduledFuture<?> scheduledTask;

    protected ScheduledWorker(final long interval) {
        exec = Executors.newScheduledThreadPool(1);
        this.interval = interval;
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            System.out.println(
                    System.currentTimeMillis() + " Scheduling worker " + this.getClass().getName() + " at " + interval);
            scheduledTask = exec.scheduleWithFixedDelay(this::run, 0, interval, TimeUnit.MILLISECONDS);
        } else {
            System.out.println(System.currentTimeMillis() + " Worker " + this.getClass().getName()
                    + " already started; skipping re-schedule");
        }
    }

    public void stop() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
        exec.shutdown();
        started.set(false);
    }

}
