package com.proudlobster.wumpus.core.utility;

import java.text.MessageFormat;

/**
 * Helper for building pre-formated strings.
 */
@FunctionalInterface
public interface Template {

    public static Template of(final String t) {
        return args -> MessageFormat.format(t, args);
    }

    /**
     * @param args the arguments to fill in to the template
     * @return the formatted string
     */
    public String parse(final Object... args);

}