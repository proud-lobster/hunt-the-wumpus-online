package com.proudlobster.wumpus.core;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proudlobster.wumpus.core.service.LifecycleService;
import com.proudlobster.wumpus.core.service.Repository;
import com.proudlobster.wumpus.core.service.Service;

public class Engine {

    public static enum Stage {
        /**
         * Indicates the engine has been created.
         * 
         * Services should not access any external resources or process requests. The
         * exception to this is SettingService, which must always be available.
         */
        NEW,

        /**
         * The engine is getting ready to start.
         * 
         * At this point all services should be registered. Services can begin loading
         * external resources and can reference other services though those services may
         * not be operational yet.
         */
        INITIALIZED,

        /**
         * The engine is running.
         * 
         * Services should be fully available to serve requests.
         */
        RUNNING,

        /**
         * The engine is stopped.
         * 
         * Services should release any resources they can and stop serving requests.
         */
        SHUTDOWN;
    }

    public static Logger LOG = LoggerFactory.getLogger("ENGINE");
    private final Repository<Service> services;

    private Stage stage;

    public Engine(final Service... s) {
        this.services = Repository.create();
        Arrays.stream(s).forEach(t -> services.register(t));
        setStage(Stage.NEW);
    }

    public Stage stage() {
        return stage;
    }

    public <S> S service(final Class<S> s) {
        return s.cast(services.getCritical(s.getName()));
    }

    private void setStage(final Stage s) {
        this.stage = s;
        services.stream()
                .filter(t -> t instanceof LifecycleService)
                .map(LifecycleService.class::cast)
                .forEach(t -> t.handleStage(this, s));
    }

    public void start() {
        LOG.info("+-- Starting game engine...");
        setStage(Stage.INITIALIZED);
        setStage(Stage.RUNNING);
        LOG.info("+-- Startup complete.");
    }

    public void shutdown() {
        setStage(Stage.SHUTDOWN);
    }
}
