package com.proudlobster.wumpus.core.entity;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Represents a data element which can be associated with entities.
 * 
 * The component object itself does not contain any data, but its presence with
 * an entity signifies that the entity has data for that component. The type of
 * the data being carried is noted by the components type.
 * 
 * It is recommended that components are unique named in a game namespace so
 * that they can be globally registered and easily serializable in many
 * different formats.
 */
@FunctionalInterface
public interface Component extends Predicate<Entity> {

    /**
     * The type of data carried by an entity for a given component.
     */
    public static enum DataType {

        /**
         * The entity carries no additional data for this component.
         * 
         * The presence of the component itself is significant. This is often referred
         * to as a "flag" component.
         */
        NONE(Optional.empty()),

        /**
         * The entity carries string data for this component.
         */
        STRING(Optional.of(String.class)),

        /**
         * The entity carries numeric data for this component.
         * 
         * In Java, the numeric type used is Long.
         */
        NUMBER(Optional.of(Long.class)),

        /**
         * The entitity carries a reference to another entity related to this component.
         * 
         * This is often used to signify a one-to-one relationship between two entities
         * which may or may not be bidirectional.
         */
        REFERENCE(Optional.of(Long.class)),

        /**
         * The entity carries a reference to multiple other entities related to this
         * component.
         * 
         * This is often used to signify a one-to-many relationship between the entity
         * and other entities.
         */
        MULTIREF(Optional.of(String.class));

        public Optional<Class<?>> valueType;

        DataType(final Optional<Class<?>> valueType) {
            this.valueType = valueType;
        }

    }

    /**
     * @return the type of data carried by an entity for this component.
     */
    DataType type();

    /**
     * @return the unique name of this component.
     */
    default String name() {
        return "UNDEFINED";
    }

    @Override
    default boolean test(Entity t) {
        return t.is(this);
    }
}