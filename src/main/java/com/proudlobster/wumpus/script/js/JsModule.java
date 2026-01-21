package com.proudlobster.wumpus.script.js;

import org.graalvm.polyglot.Source;
import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.script.ScriptExecutor;
import com.proudlobster.wumpus.script.ScriptExecutor.ModuleProxy;

/**
 * Base interface for javascript script files.
 */
@FunctionalInterface
public interface JsModule {

    public static JsModule load(final Source src, final ScriptExecutor exec) {
        final ModuleProxy proxy = exec.module(src);
        return () -> proxy;
    }

    ModuleProxy module();

    default String memberString(final String m) {
        return module().memberString(m);
    }

    default String name() {
        return memberString("name");
    }

    default void execute(final String f, final Entity... es) {
        module().execute(f, es);
    }

}
