package com.proudlobster.wumpus.server.configuration;

import java.util.Optional;
import org.slf4j.LoggerFactory;

import com.proudlobster.wumpus.core.error.OperatingError;
import com.proudlobster.wumpus.core.worker.Worker;
import com.proudlobster.wumpus.server.io.ClientMessage;

import jakarta.websocket.MessageHandler;

@FunctionalInterface
public interface WumpusMessageHandler extends MessageHandler.Whole<String> {

    private static String logPeek(final String s) {
        LoggerFactory.getLogger("WEB-SERVER").debug("Inbound Socket Message: " + s);
        return s;
    }

    public static WumpusMessageHandler create(final Worker<ClientMessage> inbox, final Long sessionId) {
        return msg -> Optional.of(msg)
                .map(WumpusMessageHandler::logPeek)
                .map(ClientMessage::create)
                .filter(m -> m.sessionId().equals(sessionId))
                .ifPresentOrElse(inbox::submit, () -> invalidMessage(msg));
    }

    public static void invalidMessage(final String msg) {
        throw new OperatingError("Invalid message: " + msg);
    }
}
