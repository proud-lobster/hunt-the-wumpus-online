package com.proudlobster.wumpus.core.service;

import java.util.function.Function;
import java.util.stream.Stream;

import com.proudlobster.wumpus.core.error.CriticalError;
import com.proudlobster.wumpus.core.error.OperatingError;
import com.proudlobster.wumpus.core.utility.Template;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public interface Repository<E> extends Function<String, E>, Service {

    public static Template ERROR_NO_CONTENT = Template.of("No repository content named ''{0}''");

    public static <E> Repository<E> create() {
        return new Repository<E>() {
            private final Map<Class<?>, E> services = new ConcurrentHashMap<>();

            @Override
            public E apply(final String t) {
                try {
                    final Class<?> c = Class.forName(t);
                    return services.get(c);
                } catch (final ClassNotFoundException e) {
                    throw new CriticalError(ERROR_NO_CONTENT.parse(t), e);
                }
            }

            @Override
            public void register(final E c) {
                services.put(c.getClass(), c);
            }

            @Override
            public java.util.stream.Stream<E> stream() {
                return services.values().stream();
            }
        };
    }

    default void register(final E e) {
        throw new OperatingError("Repository does not support registration of new content.");
    }

    default public Optional<E> get(final String n) {
        return Optional.of(n).map(this::apply);
    }

    default public E getOperational(final String n) {
        return get(n).orElseThrow(() -> new OperatingError(ERROR_NO_CONTENT.parse(n)));
    }

    default public E getCritical(final String n) {
        return get(n).orElseThrow(() -> new CriticalError(ERROR_NO_CONTENT.parse(n)));
    }

    default public Repository<E> extend(final Repository<E> extension) {
        return n -> extension.get(n).orElse(this.apply(n));
    }

    default public Stream<E> stream() {
        throw new OperatingError("Repository does not support streaming of content.");
    }
}