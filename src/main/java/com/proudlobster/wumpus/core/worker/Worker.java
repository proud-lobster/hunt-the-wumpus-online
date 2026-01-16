package com.proudlobster.wumpus.core.worker;

import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

public interface Worker<W> extends Runnable {

    void work(W item);

    Optional<W> fetch();

    void submit(W item);

    default void run() {
        try {
            Stream.generate(this::fetch)
                    .takeWhile(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(this::work);
        } catch (final Exception e) {
            LoggerFactory.getLogger("WORKER").error("Worker encountered an error: " + e.getMessage(), e);
        }
    }

}
