package com.proudlobster.wumpus.server;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proudlobster.wumpus.core.Engine;
import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.error.CriticalError;
import com.proudlobster.wumpus.core.service.CommandService;
import com.proudlobster.wumpus.core.service.EntityService;
import com.proudlobster.wumpus.core.service.LifecycleService;
import com.proudlobster.wumpus.core.service.SettingService;
import com.proudlobster.wumpus.core.worker.DelegateWorker;
import com.proudlobster.wumpus.core.worker.Worker;
import com.proudlobster.wumpus.server.configuration.SessionHandler;
import com.proudlobster.wumpus.server.configuration.SslConnector;
import com.proudlobster.wumpus.server.configuration.WumpusConfigurator;
import com.proudlobster.wumpus.server.io.ClientMessage;
import com.proudlobster.wumpus.server.io.Directive;
import com.proudlobster.wumpus.server.io.InboxWorker;
import com.proudlobster.wumpus.server.io.OutboxWorker;
import com.proudlobster.wumpus.server.service.AccountService;

public class WebSocketService implements LifecycleService {

    private final Logger log;
    private final Server server;
    private SettingService settings;
    private EntityService entities;
    private AccountService accounts;
    private CommandService commands;
    private InboxWorker inbox;
    private OutboxWorker outbox;

    public WebSocketService() {
        this.log = LoggerFactory.getLogger("WEB-SERVER");
        server = new Server();
    }

    @Override
    public void handleInitialized(final Engine eng) {
        settings = eng.service(SettingService.class);
        entities = eng.service(EntityService.class);
        accounts = eng.service(AccountService.class);
        commands = eng.service(CommandService.class);
    }

    @Override
    public void handleRunning() {
        // Directive Handlers
        Directive.initHandlers(accounts, commands);

        // IO Config
        final AtomicReference<Worker<ClientMessage>> outboxRef = new AtomicReference<>();
        final DelegateWorker<ClientMessage> outboxRefDel = () -> outboxRef.get();
        inbox = new InboxWorker(settings.requireNumber("server.inbox.schedule.intervalMillis"), outboxRefDel);
        final SessionHandler sessions = new SessionHandler(inbox, entities);
        outbox = new OutboxWorker(settings.requireNumber("server.outbox.schedule.intervalMillis"), sessions);
        outboxRef.set(outbox);

        // SSL Config
        final SslConnector sslConnector = SslConnector.create(server,
                settings.require("server.keystore.path"),
                settings.require("server.keystore.password"),
                settings.requireNumber("server.port").intValue());
        server.addConnector(sslConnector);

        // Servlet Container Config
        final ServletContextHandler handler = new ServletContextHandler(settings.require("server.context.path"));
        server.setHandler(handler);
        final WumpusConfigurator configurator = new WumpusConfigurator(sessions,
                settings.getCritical("server.endpoint.path"));
        JakartaWebSocketServletContainerInitializer.configure(handler, configurator);

        log.info("+--- Starting server outbox listener...");
        outbox.start();
        log.info("+--- Starting server inbox listener...");
        inbox.start();
        log.info("+--- Starting web socket server...");
        try {
            server.start();
        } catch (Exception e) {
            throw new CriticalError("Failed to start web socket server", e);
        }
    }

    @Override
    public void handleShutdown() {
        log.info("+--- Stopping server inbox listener...");
        inbox.stop();
        log.info("+--- Stopping server outbox listener...");
        outbox.stop();
        log.info("+--- Stopping web socket server...");
        try {
            server.stop();
        } catch (Exception e) {
            throw new CriticalError("Failed to stop web socket server", e);
        }
    }

    public void send(final Long playerId, final String directive, final String args) {
        final Directive dir = Directive.valueOf(directive.toUpperCase());
        accounts.sessionByPlayer(playerId)
                .map(Entity::identifier)
                .map(s -> dir.create(s, args))
                .ifPresent(outbox::submit);
    }
}
