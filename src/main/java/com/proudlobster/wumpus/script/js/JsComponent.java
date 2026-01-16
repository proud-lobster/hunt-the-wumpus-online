package com.proudlobster.wumpus.script.js;

import com.proudlobster.wumpus.core.entity.Component;
import com.proudlobster.wumpus.script.ScriptExecutor.ModuleProxy;

public class JsComponent implements Component, JsModule {

    private final JsModule mod;
    private final String name;
    private final DataType type;

    public JsComponent(final JsModule mod) {
        this.mod = mod;
        this.name = mod.memberString("name");
        this.type = DataType.valueOf(mod.memberString("type"));
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
    public ModuleProxy module() {
        return mod.module();
    }
}
