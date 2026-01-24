package com.proudlobster.wumpus.server.configuration;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.LoggerFactory;

import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.error.OperatingError;
import com.proudlobster.wumpus.core.service.EntityService;
import com.proudlobster.wumpus.core.worker.Worker;
import com.proudlobster.wumpus.server.ServerComponent;
import com.proudlobster.wumpus.server.io.ClientMessage;
import com.proudlobster.wumpus.server.io.Directive;

import jakarta.websocket.Session;

public class SessionHandler {

    private final Map<Long, Session> sessions;
    private final EntityService publisher;
    private final Worker<ClientMessage> inbox;

    public SessionHandler(final Worker<ClientMessage> inbox, final EntityService publisher) {
        this.sessions = new ConcurrentHashMap<>();
        this.publisher = publisher;
        this.inbox = inbox;
    }

    public void register(final Session session) {
        final Entity e = publisher.create()
                .addComponent(ServerComponent.SESSION);
        sessions.put(e.identifier(), session);
        session.addMessageHandler(WumpusMessageHandler.create(inbox, e.identifier()));
        send(Directive.SUCCESS.create(e.identifier(), "You are now connected."));
    }

    public void send(final ClientMessage msg) {
        send(msg.sessionId(), msg.whole());
    }

    public void send(final Long sessionId, final String msg) {
        LoggerFactory.getLogger("WEB-SERVER").debug("Outbound Socket Message: " + msg);
        final Session s = sessions.get(sessionId);
        try {
            s.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            try {
                s.close();
            } catch (IOException e1) {
                throw new OperatingError("Could not close session after failed send", e1);
            }
        }
    }

    public void close(final Session session) {
        try {
            session.close();
        } catch (IOException e) {
            throw new OperatingError("Could not close session", e);
        }
    }

    public boolean isOpen(final Long sessionId) {
        return sessions.containsKey(sessionId) && sessions.get(sessionId).isOpen();
    }
}
