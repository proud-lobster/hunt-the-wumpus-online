package com.proudlobster.wumpus.script.js;

import java.util.Arrays;
import java.util.List;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.IOAccess;

import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.script.ScriptService;

/**
 * Base interface for javascript script files.
 */
@FunctionalInterface
public interface JsModule {

    public static JsModule load(final Source src, final ScriptService scripts) {
        return load(src, org.graalvm.polyglot.Engine.newBuilder(ScriptService.LANG_ID).build(), scripts);
    }

    public static JsModule load(final Source src, final org.graalvm.polyglot.Engine eng, final ScriptService scripts) {
        final Context ctx = Context.newBuilder()
                .engine(eng)
                .allowIO(IOAccess.ALL)
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build();
        final Value module = ctx.eval(src);
        ctx.getBindings("js").putMember("app", scripts.createScriptInterface());
        return () -> module;
    }

    Value module();

    default Context getContext() {
        return module().getContext();
    }

    default Value getMember(final String m) {
        return module().getMember("default").getMember(m);
    }

    default String name() {
        return getMember("name").asString();
    }

    default List<Entity> execute(final String f, final Entity... es) {
        final Value run = this.getMember(f);
        final ScriptInterface intf = this.getContext().getBindings("js").getMember("app").as(ScriptInterface.class);
        run.execute(Arrays.stream(es).map(intf::wrap).toArray());
        return intf.receiveTouchedEntities();
    }

}
