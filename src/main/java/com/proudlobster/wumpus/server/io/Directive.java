package com.proudlobster.wumpus.server.io;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.slf4j.LoggerFactory;

import com.proudlobster.wumpus.core.error.OperatingError;
import com.proudlobster.wumpus.core.service.CommandService;
import com.proudlobster.wumpus.server.service.AccountService;

public enum Directive {
    LOGIN,
    LOGOUT,
    TOKEN,
    SUCCESS,
    FAILURE,
    PRINT,
    EXECUTE,
    DATA,
    PING;

    @FunctionalInterface
    public interface DirectiveHandler extends UnaryOperator<ClientMessage> {
    }

    public static void initHandlers(final AccountService accounts, final CommandService commands) {
        LOGIN.handler = accounts::handleLoginMessage;
        LOGOUT.handler = m -> accounts.processLogout(m.sessionId());
        TOKEN.handler = m -> accounts.processTokenRequest(m.payloadPart(0), m.sessionId(), m.payloadPart(1));
        SUCCESS.handler = m -> null;
        FAILURE.handler = m -> null;
        PRINT.handler = m -> null;
        EXECUTE.handler = m -> commands.handleExecute(m.payloadPart(0), m.sessionId(), m.payloadTail(1));
        DATA.handler = m -> commands.handleExecute("data", m.sessionId(), m.payload());
        PING.handler = m -> Directive.PING.create(m.sessionId(), Long.toString(System.currentTimeMillis()));
    }

    private DirectiveHandler handler;

    public Optional<ClientMessage> handle(final ClientMessage m) {
        try {
            return Optional.ofNullable(handler.apply(m));
        } catch (final OperatingError e) {
            LoggerFactory.getLogger("WEB-SERVER").error("Operating error for client message: " + m.whole(), e);
            return Optional.of(Directive.FAILURE.create(m.sessionId(), e.getMessage()));
        }
    }

    public ClientMessage create(final Long sessionId, final String payload) {
        return ClientMessage.create(sessionId, this, payload);
    }
}
