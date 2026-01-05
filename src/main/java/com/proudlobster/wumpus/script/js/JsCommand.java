package com.proudlobster.wumpus.script.js;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.error.OperatingError;
import com.proudlobster.wumpus.core.processor.Command;

public interface JsCommand extends JsProcessor, Command {

    public static JsCommand load(final JsModule mod) {
        return () -> mod.module();
    }

    @Override
    default List<String> aliases() {
        final List<String> aliasesTemp = new ArrayList<>();
        final Value aliasesRaw = getMember("aliases");
        for (int i = 0; aliasesRaw.hasArrayElements() && i < aliasesRaw.getArraySize(); i++) {
            aliasesTemp.add(aliasesRaw.getArrayElement(i).asString());
        }
        aliasesTemp.add(name());

        return List.copyOf(aliasesTemp);
    }

    @Override
    default String name() {
        return getMember("name").asString();
    }

    @Override
    default void accept(Entity e) {
        try {
            this.execute("apply", e);
        } catch (PolyglotException ex) {
            throw new OperatingError(ex.getMessage(), ex);
        }
    }

}
