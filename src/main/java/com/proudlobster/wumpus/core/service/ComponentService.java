package com.proudlobster.wumpus.core.service;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.proudlobster.wumpus.core.entity.Component;
import com.proudlobster.wumpus.core.entity.CoreComponent;

public class ComponentService implements LifecycleService, Repository<Component> {

    private final Map<String, Component> m = new ConcurrentHashMap<>();

    @Override
    public Component apply(String t) {
        return m.get(t);
    }

    @Override
    public void register(final Component c) {
        m.put(c.name(), c);
    }

    @Override
    public void handleRunning() {
        Arrays.stream(CoreComponent.values()).forEach(this::register);
    }

}