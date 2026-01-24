package com.proudlobster.wumpus.server.io;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@FunctionalInterface
public interface ClientMessage {

    String DELIMITER = ((char) 0x1e) + "";
    String PAYLOAD_DELIMITER = " ";

    public static ClientMessage create(final Long sessionId, final Directive directive, final String payload) {
        final String[] parts = { sessionId.toString(), directive.name(), payload };
        return () -> parts;
    }

    public static ClientMessage create(final String whole) {
        final String[] parts = whole.split(Pattern.quote(DELIMITER));
        return () -> parts;
    }

    String[] parts();

    default String whole() {
        return Arrays.stream(parts()).collect(Collectors.joining(DELIMITER));
    }

    default String part(final int p) {
        return parts()[p];
    }

    default Long sessionId() {
        return Long.parseLong(part(0));
    }

    default Directive directive() {
        return Directive.valueOf(part(1).toUpperCase());
    }

    default String payload() {
        return part(2);
    }

    default String[] payloadParts() {
        return payload().split(Pattern.quote(PAYLOAD_DELIMITER));
    }

    default String payloadPart(final int p) {
        final String[] parts = payloadParts();
        return p < parts.length ? parts[p] : "";
    }

    default String payloadTail(final int from) {
        final String[] parts = payloadParts();
        if (from >= parts.length) {
            return "";
        }
        return Arrays.stream(parts, from, parts.length).collect(Collectors.joining(PAYLOAD_DELIMITER));
    }

    default Optional<ClientMessage> handle() {
        return directive().handle(this);
    }

}
