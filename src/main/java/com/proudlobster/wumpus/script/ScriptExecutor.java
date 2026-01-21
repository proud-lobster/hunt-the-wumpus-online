package com.proudlobster.wumpus.script;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
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

        private final String moduleName;
        private final Value module;
        private final ScriptExecutor exec;

        public ModuleProxy(final String moduleName, final Value module, final ScriptExecutor exec) {
            this.moduleName = moduleName;
            this.module = module;
            this.exec = exec;
        }

        public String memberString(final String m) {
            return exec.submit(() -> module.getMember("default").getMember(m).asString());
        }

        public ScriptInterface scriptInterface() {
            return exec.submit(() -> module.getContext()
                    .getBindings("js")
                    .getMember("app")
                    .as(ScriptInterface.class));
        }

        public void execute(final String f, final Entity... es) {
            exec.submit(() -> {
                final ScriptInterface intf = module.getContext().getBindings("js").getMember("app")
                        .as(ScriptInterface.class);
                intf.setDebugModule(moduleName);
                module
                        .getMember("default")
                        .getMember(f)
                        .execute(Arrays.stream(es)
                                .map(intf::wrap)
                                .toArray());
                return null;
            });
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
        return new ModuleProxy(src.getName(), submit(() -> ctx.eval(src)), this);
    }

    public List<ModuleProxy> batchModules(final List<Source> sources) {
        return submit(() -> {
            final List<ModuleProxy> result = new ArrayList<>();
            for (final Source s : sources) {
                final Value v = ctx.eval(s);
                result.add(new ModuleProxy(s.getName(), v, this));
            }
            return result;
        });
    }

    public static final class ModuleDescriptor {
        public final ModuleProxy proxy;
        public final String name;
        public final String type;

        public ModuleDescriptor(final ModuleProxy proxy, final String name, final String type) {
            this.proxy = proxy;
            this.name = name;
            this.type = type;
        }
    }

    public List<ModuleDescriptor> batchDescribeModules(final List<Source> sources) {
        return submit(() -> {
            final List<ModuleDescriptor> result = new ArrayList<>();
            for (final Source s : sources) {
                final Value v = ctx.eval(s);
                final ModuleProxy proxy = new ModuleProxy(s.getName(), v, this);
                final Value def = v.getMember("default");
                String name;
                try {
                    final Value nv = def.getMember("name");
                    name = nv != null && !nv.isNull() ? nv.asString() : s.getName();
                } catch (Exception ex) {
                    name = s.getName();
                }

                String type;
                try {
                    final Value tv = def.getMember("type");
                    type = tv != null && !tv.isNull() ? tv.asString() : "NONE";
                } catch (Exception ex) {
                    type = "NONE";
                }
                result.add(new ModuleDescriptor(proxy, name, type));
            }
            return result;
        });
    }

    private <E> E submit(final Callable<E> call) {
        try {
            return executor.submit(call).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new OperatingError(e.getCause() != null ? e.getCause().getMessage().replace("Error: ", "")
                    : e.getMessage(), e);
        }
    }

}
