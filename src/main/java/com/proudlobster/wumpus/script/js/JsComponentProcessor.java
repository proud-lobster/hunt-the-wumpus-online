package com.proudlobster.wumpus.script.js;

import org.graalvm.polyglot.Value;

import com.proudlobster.wumpus.core.processor.ComponentProcessor;
import com.proudlobster.wumpus.core.service.ComponentService;

/**
 * A Javascript script implementation of ComponentProcessor.
 */
public class JsComponentProcessor extends ComponentProcessor implements JsProcessor {

    private final Value mod;

    public JsComponentProcessor(final JsModule mod, final ComponentService components) {
        super(components.getOperational(mod.getMember("component").asString()), JsProcessor.load(mod));
        this.mod = mod.module();
    }

    @Override
    public Value module() {
        return mod;
    }

}
