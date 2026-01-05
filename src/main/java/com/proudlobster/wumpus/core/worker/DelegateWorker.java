package com.proudlobster.wumpus.core.worker;

import java.util.Optional;

@FunctionalInterface
public interface DelegateWorker<T> extends Worker<T> {

    Worker<T> getDelegate();

    @Override
    default void work(final T item) {
        getDelegate().submit(item);
    }

    @Override
    default Optional<T> fetch() {
        return getDelegate().fetch();
    }

    @Override
    default void submit(T item) {
        getDelegate().submit(item);
    }

}
