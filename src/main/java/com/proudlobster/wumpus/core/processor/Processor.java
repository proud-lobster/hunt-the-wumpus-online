package com.proudlobster.wumpus.core.processor;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import com.proudlobster.wumpus.core.entity.Component;
import com.proudlobster.wumpus.core.entity.Entity;

/**
 * Performs some operation on an entity, returning any number of affected
 * transactional entities.
 */
@FunctionalInterface
public interface Processor extends Consumer<Entity> {

    /**
     * @param procs the processors to wrap in a new processor
     * @return a new processor which applies the given processors in sequence
     */
    public static Processor forSubprocessors(final Processor... procs) {
        return e -> Arrays.stream(procs).forEach(p -> p.accept(e));
    }

    /**
     * @param r a condition to evaluate when determining whether a given entity
     *          should be processed
     * @param p a processor to wrap in a conditional processor
     * @return a new processor which applies the given processor only if the entity
     *         fulfills the condition
     */
    public static Processor forCondition(final Predicate<Entity> r, final Processor p) {
        return e -> Optional.of(e).filter(r).ifPresent(p);
    }

    @Override
    void accept(final Entity e);

    /**
     * @return the component associated with this processor, if any
     */
    default Optional<Component> component() {
        return Optional.empty();
    }

}