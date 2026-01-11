package com.proudlobster.wumpus.core.utility;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DeduplicatingQueue<E> extends AbstractQueue<E> {

    private final ConcurrentLinkedQueue<E> queue = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<E, Boolean> seen = new ConcurrentHashMap<>();

    @Override
    public boolean offer(E e) {
        if (seen.putIfAbsent(e, Boolean.TRUE) == null) {
            return queue.offer(e);
        }
        return false;
    }

    @Override
    public E poll() {
        E e = queue.poll();
        if (e != null) {
            seen.remove(e);
        }
        return e;
    }

    @Override
    public E peek() {
        return queue.peek();
    }

    @Override
    public Iterator<E> iterator() {
        return queue.iterator();
    }

    @Override
    public int size() {
        return queue.size();
    }
}
