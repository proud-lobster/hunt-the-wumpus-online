package com.proudlobster.wumpus.core.entity;

public enum CoreComponent implements Component {
    /**
     * Required by all entities to uniquely identify them.
     */
    IDENTIFIER(DataType.REFERENCE),

    /**
     * Denotes an entity that should not be stored by transient storage.
     * 
     * Entities that gain this component should be removed from transient storage.
     */
    PERSISTENT(DataType.NONE),

    /**
     * Denotes an entity that is newly created and may require special handling by
     * its processors.
     */
    NEW(DataType.NONE),

    /**
     * Denotes an entity that should no longer be used and can be cleaned up
     * according to storage requirements.
     */
    EXPIRED(DataType.NONE),

    /**
     * A rudimentary component for entities whose primary role is as a link to other
     * entities.
     */
    LINK(DataType.REFERENCE),

    /**
     * A rudimentary component for entities whose primary role is as a container for
     * other entities.
     */
    CONTAINER(DataType.MULTIREF),

    /**
     * The entity is a game player.
     */
    PLAYER(DataType.NONE),

    /**
     * The entity references a game player.
     */
    PLAYER_REF(DataType.REFERENCE),

    /**
     * The entity is a game command.
     */
    COMMAND(DataType.STRING),

    /**
     * The entity has arguments.
     */
    ARGUMENTS(DataType.STRING),

    /**
     * The entity has a common name.
     */
    NAME(DataType.STRING),

    /**
     * The entity contains "heads up data", i.e. data to be displayed.
     */
    HUD(DataType.STRING);

    private final DataType type;

    private CoreComponent(final DataType type) {
        this.type = type;
    }

    @Override
    public DataType type() {
        return type;
    }

}