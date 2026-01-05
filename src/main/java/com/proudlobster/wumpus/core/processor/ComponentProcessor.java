package com.proudlobster.wumpus.core.processor;

import java.util.Optional;

import com.proudlobster.wumpus.core.entity.Component;
import com.proudlobster.wumpus.core.entity.Entity;

/**
 * Performs some operation on an entity if that entity has the given component.
 */
public class ComponentProcessor implements Processor {

    /**
     * @param c the component associated with this processor
     * @param p the internal processor for entities which satisfy the component
     * @return a component processor which processors only if an entity has the
     *         given component
     */
    public static ComponentProcessor forComponent(final Component c, final Processor p) {
        return new ComponentProcessor(c, p);
    }

    private final Component component;
    private final Processor processor;

    protected ComponentProcessor(final Component component, final Processor processor) {
        this.component = component;
        this.processor = Processor.forCondition(component, processor);
    }

    @Override
    public Optional<Component> component() {
        return Optional.of(component);
    }

    @Override
    public void accept(Entity e) {
        processor.accept(e);
    }

}