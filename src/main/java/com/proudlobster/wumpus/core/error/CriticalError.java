package com.proudlobster.wumpus.core.error;

import com.proudlobster.wumpus.core.utility.Template;

/**
 * Errors that should halt the operation of the engine.
 * 
 * These do not get handled at any level.
 */
public class CriticalError extends BaseError {

    public CriticalError(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CriticalError(final Template template, final Throwable cause, final Object... args) {
        super(template.parse(args), cause);
    }

    public CriticalError(final String message) {
        super(message);
    }

    public CriticalError(final Template template, final Object... args) {
        super(template.parse(args));
    }

}