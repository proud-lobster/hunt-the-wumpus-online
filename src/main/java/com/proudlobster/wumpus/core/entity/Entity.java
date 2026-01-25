package com.proudlobster.wumpus.core.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.proudlobster.wumpus.core.entity.Component.DataType;
import com.proudlobster.wumpus.core.error.OperatingError;
import com.proudlobster.wumpus.core.service.ComponentService;
import com.proudlobster.wumpus.core.service.EntityService;
import com.proudlobster.wumpus.core.utility.Template;
import com.proudlobster.wumpus.core.utility.Validator;

/**
 * Represents an engine entity with some number of components.
 */
public interface Entity extends Comparable<Entity> {

    public static final Template ERROR_NO_STRING_VALUE = Template
            .of("Entity ''{0}'' has no string value for component {1}.");

    public static final Template ERROR_NO_LONG_VALUE = Template
            .of("Entity ''{0}'' has no long value for component {1}.");

    /**
     * @return the map of this entity's components to its component values
     */
    Map<Component, String> delegate();

    /**
     * @param c the component to add to this entity
     * @param v the component value to add to this entity
     * @return this entity, in a transactional form if it was not already
     */
    Entity addComponent(final Component c, final String v);

    /**
     * @param c the component to remove from this entity
     * @return this entity, in a transactional form if it was not already
     */
    Entity removeComponent(final Component c);

    /**
     * @param e the entity from which all of its components and component values -
     *          other than the IDENTIFIER - will be copied
     * @return this entity, in a transactional form if it was not already
     */
    Entity copyFrom(final Entity e);

    /**
     * @return the EntityFinder for this entity's engine
     */
    EntityService service();

    ComponentService componentService();

    /**
     * @return the unique identifier for this entity
     */
    default Long identifier() {
        return Validator.validateEntityIdentifier(this);
    }

    /**
     * @param c a component to check on this entity
     * @return true if the entity has this component, false otherwise
     */
    default boolean is(final Component c) {
        return delegate().containsKey(c) && delegate().get(c) != null;
    }

    /**
     * @param c the component to check on this entity
     * @return the string value for the component
     * @throws OperatingError if this entity does not have a string value for this
     *                        component
     */
    default String stringValue(final Component c) {
        return delegate().getOrDefault(c, "");
    }

    /**
     * @param c the component to check on this entity
     * @return the long value for the component
     * @throws OperatingError if this entity does not have a long value for this
     *                        component
     */
    default Long longValue(final Component c) {
        return Validator.toLong(stringValue(c), () -> ERROR_NO_LONG_VALUE.parse(identifier(), c.name()));
    }

    /**
     * @param c the component to add to this entity
     * @return this entity, in transactional form if it was not already
     */
    default Entity addComponent(final Component c) {
        return addComponent(c, "");
    }

    /**
     * @param c the component to add to this entity
     * @param l the component value to add to this entity
     * @return this entity, in transactional form if it was not already
     */
    default Entity addComponent(final Component c, final Long... l) {
        return addComponent(c, Arrays.stream(l).map(Object::toString).collect(Collectors.joining(",")));
    }

    /**
     * @return this entity made expired, in transactional form if it was not already
     */
    default Entity expire() {
        return addComponent(CoreComponent.EXPIRED).persist();
    }

    default void vacate() {
        service().vacate(this);
    }

    /**
     * @param c the component to check
     * @return the entity referenced by this component
     */
    default Optional<Entity> checkReference(final Component c) {
        return service().checkByIdentifier(longValue(c));
    }

    /**
     * @param c the component to check
     * @return the entity referenced by this component
     */
    default Entity reference(final Component c) {
        return service().byIdentifier(longValue(c));
    }

    default List<Long> referenceIdentifiers(final Component c) {
        if (c.type() == DataType.REFERENCE) {
            return is(c) ? List.of(longValue(c)) : List.of();
        } else if (c.type() == DataType.MULTIREF) {
            final String content = is(c) ? stringValue(c) : "";
            return Arrays.stream(content.split(","))
                    .filter(s -> !s.trim().isEmpty())
                    .map(s -> Validator.toLong(s, () -> ERROR_NO_LONG_VALUE.parse(identifier(), c.name())))
                    .toList();
        } else {
            return List.of();
        }
    }

    /**
     * @param c the component to check
     * @return the entities referenced by this component
     */
    default List<Entity> references(final Component c) {
        if (c.type() == DataType.REFERENCE) {
            return List.of(reference(c));
        } else if (c.type() == DataType.MULTIREF) {
            return referenceIdentifiers(c)
                    .stream()
                    .map(service()::checkByIdentifier)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } else {
            return List.of();
        }
    }

    /**
     * @param c the component to add references for
     * @param v the IDENTIFIER values of the entities to add as references
     * @return this entity, in transactional form if it was not already
     */
    default Entity addReferences(final Component c, final Long... v) {
        final String ids = Stream.concat(references(c).stream().map(Entity::identifier), Arrays.stream(v))
                .distinct()
                .map(Object::toString)
                .collect(Collectors.joining(","));
        return addComponent(c, ids);
    }

    /**
     * @param c  the component to add references for
     * @param es the entities to add as references
     * @return this entity, in transactional form if it was not already
     */
    default Entity addReferences(final Component c, final Entity... es) {
        final String ids = Stream.concat(referenceIdentifiers(c).stream(), Arrays.stream(es).map(Entity::identifier))
                .distinct()
                .map(Object::toString)
                .collect(Collectors.joining(","));
        return addComponent(c, ids);
    }

    /**
     * @param c   the component to remove references for
     * @param ids the entity IDs to remove as references
     * @return this entity, in transactional form if it was not already
     */
    default Entity removeReferences(final Component c, final Long... ids) {
        final Set<Long> removeIds = Arrays.stream(ids).collect(Collectors.toSet());
        final String newIds = referenceIdentifiers(c).stream()
                .filter(i -> !removeIds.contains(i))
                .map(Object::toString)
                .collect(Collectors.joining(","));
        return addComponent(c, newIds);
    }

    /**
     * @param c  the component to remove references for
     * @param es the entities to remove as references
     * @return this entity, in transactional form if it was not already
     */
    default Entity removeReferences(final Component c, final Entity... es) {
        final Long[] removeIds = Arrays.stream(es).map(Entity::identifier).toArray(Long[]::new);
        return removeReferences(c, removeIds);
    }

    /**
     * @return the LINK reference for this entity
     */
    default Entity link() {
        return reference(CoreComponent.LINK);
    }

    /**
     * @return the CONTAINER references for this entity
     */
    default List<Entity> contents() {
        return references(CoreComponent.CONTAINER);
    }

    /**
     * @param es the entities to add to this entity's CONTAINER references
     * @return this entity, in transactional form if it was not already
     */
    default Entity addContents(final Entity... es) {
        return addReferences(CoreComponent.CONTAINER, es);
    }

    /**
     * @param es the entities to remove from this entity's CONTAINER references
     * @return this entity, in transactional form if it was not already
     */
    default Entity removeContents(final Entity... es) {
        return removeReferences(CoreComponent.CONTAINER, es);
    }

    default Entity persist() {
        service().persist(this);
        return this;
    }

    default String asString() {
        return identifier().toString() + "\n" +
                delegate().entrySet().stream()
                        .map(es -> "+-- " + es.getKey().name() + ":" + es.getValue())
                        .collect(Collectors.joining("\n"));
    }

    @Override
    default int compareTo(final Entity e) {
        return identifier().compareTo(e.identifier());
    }
}