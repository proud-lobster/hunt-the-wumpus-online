package com.proudlobster.wumpus.script.js;

import org.graalvm.polyglot.Value;

import com.proudlobster.wumpus.core.entity.Component;

public class JsComponent implements Component, JsModule {

    private final Value mod;
    private final String name;
    private final DataType type;

    public JsComponent(final JsModule mod) {
        this.mod = mod.module();
        this.name = mod.getMember("name").asString();
        this.type = DataType.valueOf(mod.getMember("type").asString());
    }

    @Override
    public DataType type() {
        return type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Value module() {
        return mod;
    }
}
