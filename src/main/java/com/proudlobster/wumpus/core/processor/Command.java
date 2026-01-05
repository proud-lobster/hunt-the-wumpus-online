package com.proudlobster.wumpus.core.processor;

import java.util.List;

/**
 * Represents a game command as a processor.
 */
public interface Command extends Processor {

    /**
     * @return the command name, such as what is typed to invoke it
     */
    public String name();

    /**
     * @return any aliases for the command name
     */
    public List<String> aliases();
}