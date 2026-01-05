package com.proudlobster.wumpus.core.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.proudlobster.wumpus.core.Engine;
import com.proudlobster.wumpus.core.entity.Component;
import com.proudlobster.wumpus.core.entity.CoreComponent;
import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.error.OperatingError;
import com.proudlobster.wumpus.core.utility.Template;

/**
 * Handles reading entities from storage.
 */
public class EntityService implements LifecycleService {

    private static class InMemoryEntity implements Entity {

        private final Long identifier;
        private final EntityService service;
        private final ComponentService componentService;

        public InMemoryEntity(final Long identifier, final EntityService service,
                final ComponentService componentService) {
            this.identifier = identifier;
            this.service = service;
            this.componentService = componentService;
            service.entities.putIfAbsent(identifier, new ConcurrentHashMap<>());
            this.addComponent(CoreComponent.IDENTIFIER, identifier);
        }

        @Override
        public Map<Component, String> delegate() {
            return service.entities.get(identifier);
        }

        @Override
        public Entity addComponent(Component c, String v) {
            delegate().put(c, v);
            service.componentIndex.computeIfAbsent(c, k -> ConcurrentHashMap.newKeySet()).add(identifier);
            return this;
        }

        @Override
        public Entity removeComponent(Component c) {
            delegate().remove(c);
            service.componentIndex.computeIfAbsent(c, k -> ConcurrentHashMap.newKeySet()).remove(identifier);
            return this;
        }

        @Override
        public Entity copyFrom(Entity e) {
            e.delegate().forEach((c, v) -> {
                if (c != CoreComponent.IDENTIFIER) {
                    delegate().put(c, v);
                }
            });
            return this;
        }

        @Override
        public EntityService service() {
            return service;
        }

        @Override
        public ComponentService componentService() {
            return componentService;
        }

    }

    private static final Template ERROR_NO_ENTITY_WITH_ID = Template.of("No entity with identifier ''{0}'' exists.");
    private static final AtomicLong counter = new AtomicLong(0);
    private final Map<Long, Map<Component, String>> entities = new ConcurrentHashMap<>();
    private final Map<Component, Set<Long>> componentIndex = new ConcurrentHashMap<>();
    private ComponentService componentService;

    private Entity wrap(final Long id) {
        return new InMemoryEntity(id, this, this.componentService);
    }

    /**
     * @return the next unique entity identifier
     */
    public static Long nextId() {
        return (System.currentTimeMillis() * 1000) + counter.incrementAndGet();
    }

    @Override
    public void handleInitialized(final Engine eng) {
        componentService = eng.service(ComponentService.class);
    }

    /**
     * @param id the identifier for the new entity
     * @return a new entity with that identifier
     */
    public Entity create(Long id) {
        return new InMemoryEntity(id, this, this.componentService);
    }

    /**
     * @return a stream of all entities in storage
     */
    public Stream<Entity> streamAll() {
        return entities
                .keySet()
                .stream()
                .map(this::wrap);
    }

    /**
     * @param c the component to find entities for
     * @return the entities with that component
     */
    public Stream<Entity> streamByComponent(Component c) {
        return componentIndex
                .getOrDefault(c, Set.of())
                .stream()
                .map(this::wrap);
    }

    /**
     * @param id the identifier of the entity to find
     * @return the entity with that identifier
     */
    public Optional<Entity> checkByIdentifier(Long id) {
        return Optional.ofNullable(entities.get(id))
                .map(e -> wrap(id));
    }

    /**
     * @return a new entity with a unique identifier
     */
    public Entity create() {
        return create(nextId());
    }

    /**
     * @return a list of all entities in storage
     */
    public List<Entity> all() {
        return streamAll().collect(Collectors.toList());
    }

    /**
     * @param c the component to find entities for
     * @return the entities with that component
     */
    public List<Entity> byComponent(final Component c) {
        return streamByComponent(c).collect(Collectors.toList());
    }

    /**
     * @param id the identifier of the entity to find
     * @return the entity with that identifier
     * @throws OperatingError if there is no such entity
     */
    public Entity byIdentifier(final Long id) {
        return checkByIdentifier(id).orElseThrow(() -> new OperatingError(ERROR_NO_ENTITY_WITH_ID.parse(id)));
    }

    /**
     * @param c the component to find entities for
     * @param s the component value to match for those entities
     * @return the entities with that component and value
     */
    public Stream<Entity> streamByComponentWithValue(final Component c, final String s) {
        return streamByComponent(c).filter(e -> s.equals(e.stringValue(c)));
    }

    /**
     * @param c the component to find entities for
     * @param s the component value to match for those entities
     * @return the entities with that component and value
     */
    public Stream<Entity> streamByComponentWithValue(final Component c, final Long l) {
        return streamByComponent(c).filter(e -> l.equals(e.longValue(c)));
    }
}