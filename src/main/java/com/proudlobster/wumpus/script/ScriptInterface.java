package com.proudlobster.wumpus.script;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.graalvm.polyglot.Value;

import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.service.ComponentService;
import com.proudlobster.wumpus.core.service.EntityService;
import com.proudlobster.wumpus.server.WebSocketService;

/**
 * Manages the handling of entity state between a script context and the actual
 * entity space.
 */
public class ScriptInterface {

    private final EntityService entities;
    private final ComponentService components;
    private final WebSocketService webSockets;
    private final Map<Long, EntityProxyObject> es;

    public ScriptInterface(
            final EntityService entities,
            final ComponentService components,
            final WebSocketService webSockets) {
        this.es = new HashMap<>();
        this.entities = entities;
        this.components = components;
        this.webSockets = webSockets;
    }

    private EntityProxyObject _wrap(final Entity e) {
        return new EntityProxyObject(this, entities.create(e.identifier()).copyFrom(e));
    }

    private String unwrapToString(final Value v) {
        if (v.isProxyObject() && v.asProxyObject() instanceof EntityProxyObject) {
            return unwrap(v).identifier().toString();
        } else {
            return v.asString();
        }
    }

    // Script Engine Internal Methods

    protected Entity wrap(final Entity e) {
        return es.computeIfAbsent(e.identifier(), i -> _wrap(e));
    }

    protected Entity unwrap(final Value v) {
        return ((EntityProxyObject) v.asProxyObject()).original();
    }

    protected Entity create(final Entity e) {
        return wrap(e);
    }

    protected Optional<Entity> resolveReference(final Long id) {
        final Supplier<Optional<Entity>> fromStorage = () -> entities
                .checkByIdentifier(id)
                .map(this::wrap);
        final Optional<Entity> fromInterface = Optional
                .ofNullable(es.containsKey(id))
                .filter(t -> t)
                .map(t -> es.get(id));
        return fromInterface.or(fromStorage);
    }

    // Script-Facing Methods
    public Entity createEntity() {
        return create(entities.create());
    }

    public Entity createEntity(final Long id) {
        return create(entities.create(id));
    }

    public Entity entityByIdentifier(final Long id) {
        return wrap(entities.byIdentifier(id));
    }

    public List<Entity> entitiesByComponent(final String c) {
        return entities.streamByComponent(components.getOperational(c))
                .map(this::wrap)
                .collect(Collectors.toList());
    }

    public List<Entity> entitiesByComponentValue(final String c, final Value value) {
        return entities.streamByComponentWithValue(components.getOperational(c), unwrapToString(value))
                .map(this::wrap)
                .collect(Collectors.toList());
    }

    public Entity lookup(final String c) {
        return entities.lookup(components.getOperational(c)).orElse(null);
    }

    public void sendMessage(final Value player, final String directive, final String args) {
        webSockets.send(unwrap(player).identifier(), directive, args);
    }

}
