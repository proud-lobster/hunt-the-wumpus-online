package com.proudlobster.wumpus.script.js;

import java.util.Arrays;
import java.util.List;

import org.graalvm.polyglot.PolyglotException;
import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.error.OperatingError;
import com.proudlobster.wumpus.core.processor.Command;

@FunctionalInterface
public interface JsCommand extends JsProcessor, Command {

    public static JsCommand load(final JsModule mod) {
        return () -> mod.module();
    }

    @Override
    default List<String> aliases() {
        return Arrays.stream(memberString("aliases").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty()).toList();
    }

    @Override
    default String name() {
        return memberString("name");
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
