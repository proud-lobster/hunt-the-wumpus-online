package com.proudlobster.wumpus.core.error;

import com.proudlobster.wumpus.core.utility.Template;

/**
 * Errors that could happen regularly but should not halt the engine.
 * 
 * Processors should catch, log, and stifle these.
 */
public class OperatingError extends BaseError {

    public OperatingError(final String message, final Throwable cause) {
        super(message, cause);
    }

    public OperatingError(final Template template, final Throwable cause, final Object... args) {
        super(template.parse(args), cause);
    }

    public OperatingError(final String message) {
        super(message);
    }

    public OperatingError(final Template template, final Object... args) {
        super(template.parse(args));
    }

}