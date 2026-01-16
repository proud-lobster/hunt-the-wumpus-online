package com.proudlobster.wumpus.script.js;

import com.proudlobster.wumpus.core.processor.ComponentProcessor;
import com.proudlobster.wumpus.core.service.ComponentService;
import com.proudlobster.wumpus.script.ScriptExecutor.ModuleProxy;

/**
 * A Javascript script implementation of ComponentProcessor.
 */
public class JsComponentProcessor extends ComponentProcessor implements JsProcessor {

    private final JsModule mod;

    public JsComponentProcessor(final JsModule mod, final ComponentService components) {
        super(components.getOperational(mod.memberString("component")), JsProcessor.load(mod));
        this.mod = mod;
    }

    @Override
    public ModuleProxy module() {
        return mod.module();
    }

}
