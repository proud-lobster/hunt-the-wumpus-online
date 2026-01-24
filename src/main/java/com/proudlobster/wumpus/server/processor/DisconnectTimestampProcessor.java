package com.proudlobster.wumpus.server.processor;

import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.processor.ComponentProcessor;
import com.proudlobster.wumpus.server.ServerComponent;
import com.proudlobster.wumpus.server.io.Directive;

public class DisconnectTimestampProcessor extends ComponentProcessor {

    private static final Long TIMEOUT = 60000L;

    private static void process(final Entity e) {
        final Long ts = e.longValue(ServerComponent.DISCONNECT_TIMESTAMP);
        if (System.currentTimeMillis() > ts + TIMEOUT) {
            Directive.LOGOUT.create(e.longValue(ServerComponent.SESSION_REF)).handle();
        }
    }

    public DisconnectTimestampProcessor() {
        super(ServerComponent.DISCONNECT_TIMESTAMP, DisconnectTimestampProcessor::process);
    }

}
