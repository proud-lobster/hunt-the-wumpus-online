package com.proudlobster.wumpus.script;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.IOAccess;

import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.error.OperatingError;

public class ScriptExecutor {

    public static class ModuleProxy {

        private final Value module;
        private final ScriptExecutor exec;

        public ModuleProxy(final Value module, final ScriptExecutor exec) {
            this.module = module;
            this.exec = exec;
        }

        public String memberString(final String m) {
            return exec.submit(() -> module.getMember("default").getMember(m).asString());
        }

        public ScriptInterface scriptInterface() {
            return module.getContext().getBindings("js").getMember("app").as(ScriptInterface.class);
        }

        public void execute(final String f, final Entity... es) {
            exec.submit(() -> module
                    .getMember("default")
                    .getMember(f)
                    .execute(Arrays.stream(es)
                            .map(scriptInterface()::wrap)
                            .toArray()));
        }

    }

    private final ExecutorService executor;

    private final Context ctx;

    public ScriptExecutor(final ScriptInterface scriptInterface) {
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("js-worker-thread");
            return t;
        });

        ctx = submit(() -> {
            final Context ctx = Context.newBuilder("js")
                    .engine(org.graalvm.polyglot.Engine.newBuilder("js").build())
                    .allowIO(IOAccess.ALL)
                    .allowAllAccess(true)
                    .option("js.esm-eval-returns-exports", "true")
                    .build();
            ctx.getBindings("js").putMember("app", scriptInterface);
            return ctx;
        });

    }

    public ModuleProxy module(final Source src) {
        return new ModuleProxy(submit(() -> ctx.eval(src)), this);
    }

    private <E> E submit(final Callable<E> call) {
        try {
            return executor.submit(call).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new OperatingError(e.getCause().getMessage().replace("Error: ", ""), e);
        }
    }

}
