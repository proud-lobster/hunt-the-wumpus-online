package com.proudlobster.wumpus.server;

import com.proudlobster.wumpus.core.entity.Component;

public enum ServerComponent implements Component {

    // Session Components
    SESSION(DataType.NONE),
    SESSION_REF(DataType.REFERENCE),
    DISCONNECT_TIMESTAMP(DataType.NUMBER),

    // Account Components
    ACCOUNT(DataType.NONE),
    ACCOUNT_REF(DataType.REFERENCE),
    EMAIL_ADDRESS(DataType.STRING),
    SALT(DataType.STRING),
    CLIENT_TOKEN(DataType.STRING),
    TEMP_CODE(DataType.STRING),
    TEMP_CODE_TIMESTAMP(DataType.NUMBER),
    TEMP_CODE_ATTEMPTS(DataType.NUMBER),
    LOCK_UNTIL_TIMESTAMP(DataType.NUMBER);

    private final DataType type;

    private ServerComponent(final DataType type) {
        this.type = type;
    }

    @Override
    public DataType type() {
        return type;
    }
}