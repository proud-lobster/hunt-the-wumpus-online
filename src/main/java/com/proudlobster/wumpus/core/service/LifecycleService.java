package com.proudlobster.wumpus.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proudlobster.wumpus.core.Engine;

/**
 * A service which responds to engine lifecycle stages.
 */
public interface LifecycleService extends Service {

    /**
     * NOTE: Implementers should implement EITHER handleStage or the individual
     * stage handler methods, not both.
     * 
     * @param eng   the engine changing stages.
     * @param stage the new engine stage.
     */
    default void handleStage(final Engine eng, final Engine.Stage stage) {
        final Logger l = LoggerFactory.getLogger(getClass());
        l.debug("Lifecycle service {} handling stage {}", this.getClass().getSimpleName(), stage.name());
        switch (stage) {
            case INITIALIZED:
                handleInitialized(eng);
                break;
            case NEW:
                handleNew(eng);
                break;
            case RUNNING:
                handleRunning();
                break;
            case SHUTDOWN:
                handleShutdown();
                break;
            default:
                break;
        }
        l.debug("Lifecycle service {} leaving stage {}", this.getClass().getSimpleName(), stage.name());
    }

    /**
     * @param eng the engine that has entered the NEW stage
     */
    default void handleNew(final Engine eng) {

    }

    /**
     * @param eng the engine that has entered the INITIALIZED stage
     */
    default void handleInitialized(final Engine eng) {

    }

    default void handleRunning() {

    }

    default void handleShutdown() {

    }

}