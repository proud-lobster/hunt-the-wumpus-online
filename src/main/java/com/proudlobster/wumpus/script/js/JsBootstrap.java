package com.proudlobster.wumpus.script.js;

import java.util.List;
import java.util.function.Supplier;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.script.ScriptService;

/**
 * Interface for bootstrap scripts.
 */
public class JsBootstrap implements JsModule, Supplier<List<Entity>> {

    final JsModule mod;

    public JsBootstrap(final JsModule mod) {
        this.mod = mod;
    }

    public JsBootstrap(final ScriptService scripts, final Source src) {
        this(JsModule.load(src, scripts));
    }

    @Override
    public Value module() {
        return mod.module();
    }

    @Override
    public List<Entity> get() {
        return this.execute("run");
    }

}
