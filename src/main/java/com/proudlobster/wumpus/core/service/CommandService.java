package com.proudlobster.wumpus.core.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.proudlobster.wumpus.core.Engine;
import com.proudlobster.wumpus.core.entity.CoreComponent;
import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.processor.Command;
import com.proudlobster.wumpus.core.utility.Template;
import com.proudlobster.wumpus.server.ServerComponent;
import com.proudlobster.wumpus.server.io.ClientMessage;
import com.proudlobster.wumpus.server.io.Directive;

public class CommandService implements LifecycleService, Repository<Command> {

    private static final Template ERR_NO_COMMAND = Template.of("Unknown command ''{0}''.");

    private final Map<String, Command> m = new ConcurrentHashMap<>();
    private final Map<String, String> ma = new ConcurrentHashMap<>();

    private EntityService entities;

    @Override
    public void handleInitialized(final Engine eng) {
        entities = eng.service(EntityService.class);
    }

    @Override
    public Command apply(String t) {
        return ma.containsKey(t) ? m.get(ma.get(t)) : m.get(t);
    }

    @Override
    public void register(Command c) {
        m.put(c.name(), c);
        ma.put(c.name(), c.name());
        c.aliases().stream().forEach(a -> ma.put(a, c.name()));
    }

    public ClientMessage handleExecute(final String cmdName, final Long sessionId, final String args) {
        final Long playerId = entities.byIdentifier(sessionId)
                .reference(ServerComponent.ACCOUNT_REF)
                .reference(CoreComponent.PLAYER_REF)
                .identifier();

        return get(cmdName).map(c -> {
            final Entity cmdEntity = entities.create()
                    .addComponent(CoreComponent.COMMAND, cmdName)
                    .addComponent(CoreComponent.PLAYER_REF, playerId)
                    .addComponent(CoreComponent.ARGUMENTS, args);
            c.accept(cmdEntity);
            return Directive.SUCCESS.create(sessionId, "");
        }).orElseGet(() -> Directive.FAILURE.create(sessionId, ERR_NO_COMMAND.parse(cmdName)));

    }

}