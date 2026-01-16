package com.proudlobster.wumpus.script.js;

/**
 * Interface for bootstrap scripts.
 */
@FunctionalInterface
public interface JsBootstrap extends Runnable, JsModule {

    public static JsBootstrap load(final JsModule mod) {
        return () -> mod.module();
    }

    @Override
    default void run() {
        module().execute("run");
    }

}
