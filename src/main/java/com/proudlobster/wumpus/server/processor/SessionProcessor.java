package com.proudlobster.wumpus.server.processor;

import java.util.function.Predicate;

import com.proudlobster.wumpus.core.entity.CoreComponent;
import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.processor.ComponentProcessor;
import com.proudlobster.wumpus.server.ServerComponent;

public class SessionProcessor extends ComponentProcessor {

    private static void process(final Entity e, final Predicate<Long> isOpenSession) {
        if (!isOpenSession.test(e.identifier()) && !e.is(CoreComponent.EXPIRED) && e.is(ServerComponent.ACCOUNT_REF)) {
            e.reference(ServerComponent.ACCOUNT_REF)
                    .addComponent(ServerComponent.DISCONNECT_TIMESTAMP, System.currentTimeMillis());
            e.expire();
        }
    }

    public SessionProcessor(final Predicate<Long> isOpenSession) {
        super(ServerComponent.SESSION, e -> process(e, isOpenSession));
    }

}
