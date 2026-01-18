package com.proudlobster.wumpus.core.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proudlobster.wumpus.core.Engine;
import com.proudlobster.wumpus.core.entity.Component;
import com.proudlobster.wumpus.core.entity.CoreComponent;
import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.error.OperatingError;
import com.proudlobster.wumpus.core.utility.Template;
import com.proudlobster.wumpus.core.worker.QueueWorker;
import com.proudlobster.wumpus.core.worker.ScheduledWorker;
import com.proudlobster.wumpus.core.worker.SingleItemWorker;

/**
 * Handles reading entities from storage.
 */
public class EntityService implements LifecycleService {

    public static Logger LOG = LoggerFactory.getLogger("STORAGE");

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

        public InMemoryEntity(final Long identifier, final EntityService service,
                final ComponentService componentService,
                final Map<String, String> initialData) {
            this(identifier, service, componentService);
            initialData.forEach((c, v) -> this.addComponent(
                    componentService.get(c).orElseThrow(() -> new OperatingError("No component found with name: " + c)),
                    v));
        }

        @Override
        public Map<Component, String> delegate() {
            return service.entities.get(identifier);
        }

        @Override
        public Entity addComponent(Component c, String v) {
            delegate().put(c, v);
            service.componentIndex.computeIfAbsent(c, k -> ConcurrentHashMap.newKeySet()).add(identifier);
            service.activityWorker.submit(identifier);
            return this;
        }

        @Override
        public Entity removeComponent(Component c) {
            delegate().remove(c);
            service.componentIndex.computeIfAbsent(c, k -> ConcurrentHashMap.newKeySet()).remove(identifier);
            service.activityWorker.submit(identifier);
            return this;
        }

        @Override
        public Entity copyFrom(Entity e) {
            e.delegate().forEach((c, v) -> {
                if (c != CoreComponent.IDENTIFIER) {
                    delegate().put(c, v);
                }
            });
            service.activityWorker.submit(identifier);
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

        @Override
        public boolean equals(final Object o) {
            return o != null &&
                    o instanceof Entity &&
                    this.compareTo((Entity) o) == 0;
        }

    }

    private static final Template ERROR_NO_ENTITY_WITH_ID = Template.of("No entity with identifier ''{0}'' exists.");
    private static final AtomicLong counter = new AtomicLong(0);
    private final Map<Long, Map<Component, String>> entities = new ConcurrentHashMap<>();
    private final Map<Component, Set<Long>> componentIndex = new ConcurrentHashMap<>();
    private ComponentService componentService;
    private StorageService storageService;
    private SettingService settingService;
    private ScheduledWorker<EntityService> snapshotWorker;
    private QueueWorker<Long> activityWorker;

    private Entity wrap(final Long id) {
        return new InMemoryEntity(id, this, this.componentService);
    }

    private Optional<Entity> loadFromStorage(final Long id) {
        final Map<String, String> data = storageService.readById(id);
        if (data.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new InMemoryEntity(id, this, this.componentService, data));
        }
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
        storageService = eng.service(StorageService.class);
        settingService = eng.service(SettingService.class);

        snapshotWorker = new SingleItemWorker<EntityService>(
                settingService.requireNumber("storage.snapshot.intervalMillis")) {
            @Override
            public void work(final EntityService item) {
                LOG.info("+- Starting entity snapshot...");
                storageService.write(item.entities);
                LOG.info("+- Entity snapshot complete.");
            }
        };

        activityWorker = new QueueWorker<Long>(
                settingService.requireNumber("storage.activity.intervalMillis")) {
            @Override
            public void work(final Long item) {
                LOG.info("+- Writing entity ID {} to storage...", item);
                storageService.write(item, entities.get(item));
                LOG.info("+- Entity ID {} write complete.", item);
            }
        };
    }

    @Override
    public void handleRunning() {
        snapshotWorker.start();
        activityWorker.start();
    }

    @Override
    public void handleShutdown() {
        snapshotWorker.stop();
        activityWorker.stop();
    }

    /**
     * @param id the identifier for the new entity
     * @return a new entity with that identifier
     */
    public Entity create(Long id) {
        return checkByIdentifier(id)
                .orElseGet(() -> new InMemoryEntity(id, this, this.componentService));
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
                .map(e -> wrap(id))
                .or(() -> loadFromStorage(id));
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

    public void persist(final Entity e) {
        storageService.write(e.identifier(), e.delegate());
    }

    public Stream<Entity> lookup(final Component c, final String v) {
        final Set<Entity> inMemory = streamByComponentWithValue(c, v).collect(Collectors.toSet());
        final Stream<InMemoryEntity> inStorage = storageService.readByComponentWithValue(c.name(), v)
                .entrySet()
                .stream()
                .map(entry -> new InMemoryEntity(entry.getKey(), this, this.componentService, entry.getValue()))
                .filter(e -> !inMemory.contains(e));
        return Stream.concat(inMemory.stream(), inStorage);
    }

    public Stream<Entity> lookup(final Component c, final Long v) {
        return lookup(c, v.toString());
    }
}