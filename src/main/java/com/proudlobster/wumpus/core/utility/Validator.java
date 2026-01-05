package com.proudlobster.wumpus.core.utility;

import java.util.Optional;
import java.util.function.Supplier;

import com.proudlobster.wumpus.core.entity.CoreComponent;
import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.error.OperatingError;

/**
 * Collection of utility methods for validating parsed data.
 */
public interface Validator {

    String ERROR_NO_IDENTIFIER = "Invalid entity: no identifier.";
    Template ERROR_INVALID_IDENTIFIER = Template.of("Invalid entity: identifier ''{0}'' is not a long.");

    /**
     * @param <T> the data type of t
     * @param t   the object being checked for nullness
     * @param msg the operating error message if it is null
     * @return t if not null
     * @throws OperatingError if null
     */
    public static <T> T nonNull(final T t, final Supplier<String> msg) {
        if (t == null) {
            throw new OperatingError(msg.get());
        } else {
            return t;
        }
    }

    public static Optional<Long> tryToLong(final String s) {
        try {
            return Optional.of(Long.parseLong(s));
        } catch (final NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * @param s   the string to parse the long value from
     * @param msg the operating error message if the string is not a long
     * @return the long parsed from s
     * @throws OperatingError if s is not a long
     */
    public static Long toLong(final String s, final Supplier<String> msg) {
        try {
            return Long.parseLong(nonNull(s, msg));
        } catch (final NumberFormatException e) {
            throw new OperatingError(msg.get(), e);
        }
    }

    /**
     * @param e the entity to validate
     * @return the identifier of the entity
     * @throws OperatingError if the entity does not have a valid formed identifier
     */
    public static Long validateEntityIdentifier(final Entity e) {
        final String i = e.delegate().get(CoreComponent.IDENTIFIER);

        if (i == null) {
            throw new OperatingError(ERROR_NO_IDENTIFIER);
        }

        return toLong(i, () -> ERROR_INVALID_IDENTIFIER.parse(i));
    }

}