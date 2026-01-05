package com.proudlobster.wumpus.core.error;

/**
 * Base class for implementing game engine errors.
 */
public abstract class BaseError extends RuntimeException {

    public BaseError(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BaseError(final String message) {
        super(message);
    }

}