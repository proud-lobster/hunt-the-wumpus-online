package com.proudlobster.wumpus.script.js;

import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.processor.Processor;

/**
 * Interface for processors implemented by a Javascript script.
 */
@FunctionalInterface
public interface JsProcessor extends Processor, JsModule {

    public static JsProcessor load(final JsModule mod) {
        return () -> mod.module();
    }

    @Override
    default void accept(Entity e) {
        this.execute("apply", e);
    }

}
