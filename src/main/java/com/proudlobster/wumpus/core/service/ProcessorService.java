package com.proudlobster.wumpus.core.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.proudlobster.wumpus.core.Engine;
import com.proudlobster.wumpus.core.processor.Processor;
import com.proudlobster.wumpus.core.worker.ProcessorWorker;

public class ProcessorService implements LifecycleService, Repository<Processor> {

    private final List<Processor> processors;
    private final Map<String, Processor> m = new ConcurrentHashMap<>();
    private SettingService settings;
    private EntityService entities;
    private ProcessorWorker worker;

    public ProcessorService(final Processor... ps) {
        processors = new CopyOnWriteArrayList<>(Arrays.asList(ps));
        Arrays.stream(ps).forEach(p -> m.put(p.getClass().getName(), p));
    }

    @Override
    public Processor apply(String t) {
        return m.get(t);
    }

    @Override
    public void register(Processor c) {
        m.put(c.getClass().getName(), c);
        processors.add(c);
    }

    @Override
    public void handleInitialized(final Engine e) {
        settings = e.service(SettingService.class);
        entities = e.service(EntityService.class);
    }

    @Override
    public void handleRunning() {
        final long interval = settings.requireNumber("processor.schedule.intervalMillis");
        worker = new ProcessorWorker(interval, processors, entities);
        worker.start();
    }

}
