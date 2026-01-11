package com.proudlobster.wumpus.core.service;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.proudlobster.wumpus.core.Engine;
import com.proudlobster.wumpus.core.entity.Component;
import com.proudlobster.wumpus.core.error.CriticalError;
import com.proudlobster.wumpus.core.error.OperatingError;

public class StorageService implements LifecycleService {

    private static final String DS_CONF_PREFIX = "storage.datasource.";
    private static final String DS_CLASS_NAME = DS_CONF_PREFIX + "className";
    private static final String DS_PROPS_LIST = DS_CONF_PREFIX + "properties";
    private static final String DS_PROP_PREFIX = DS_CONF_PREFIX + "prop.";
    private static final String TABLE_NAME = "ENTITY_COMPONENT";
    private static final String TABLE_DEFINITION = """
            CREATE TABLE ENTITY_COMPONENT (
                ID BIGINT NOT NULL,
                COMPONENT VARCHAR(500) NOT NULL,
                VALUE VARCHAR(10000),
                PRIMARY KEY (ID, COMPONENT)
            )
                """;

    private static final String SELECT_ALL = """
            SELECT ID, COMPONENT, VALUE
            FROM ENTITY_COMPONENT
            """;
    private static final String SELECT_BY_ID = SELECT_ALL + " WHERE ID = ?";

    private static final String SELECT_BY_COMPONENT = SELECT_ALL
            + " WHERE ID IN (SELECT ID FROM ENTITY_COMPONENT WHERE COMPONENT = ?)";

    private static final String SELECT_BY_COMPONENT_AND_VALUE = SELECT_ALL
            + " WHERE ID IN (SELECT ID FROM ENTITY_COMPONENT WHERE COMPONENT = ? AND VALUE = ?)";

    private static final String INSERT = """
            INSERT INTO ENTITY_COMPONENT
            (ID, COMPONENT, VALUE)
            VALUES (?,?,?)
            """;

    private static final String UPDATE = """
            UPDATE ENTITY_COMPONENT
            SET VALUE = ?
            WHERE ID = ?
            AND COMPONENT = ?
            """;

    private final AtomicBoolean batchCommit = new AtomicBoolean(false);
    private SettingService settings;
    private DataSource ds;

    private DataSource createDataSource() {
        final String className = settings.require(DS_CLASS_NAME);
        try {
            return DataSource.class.cast(Class.forName(className).getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw new CriticalError("Failed to create DataSource of class: " + className, e);
        }
    }

    private void initializeDataSource() {
        Pattern.compile(",")
                .splitAsStream(settings.getCritical(DS_PROPS_LIST))
                .filter(s -> !s.isBlank())
                .forEach(this::setDataSourceProperty);
    }

    private void setDataSourceProperty(final String prop) {
        try {
            new PropertyDescriptor(prop, ds.getClass())
                    .getWriteMethod()
                    .invoke(ds, settings.require(DS_PROP_PREFIX + prop));
        } catch (IllegalAccessException | InvocationTargetException | IntrospectionException e) {
            throw new CriticalError("Could not initialize property " + prop, e);
        }
    }

    private void validateDatabase() {
        try (final Connection c = ds.getConnection()) {
            if (!c.getMetaData().getTables(null, null, TABLE_NAME, null).next()) {
                c.prepareStatement(TABLE_DEFINITION).execute();
            }
        } catch (SQLException e) {
            throw new CriticalError("Could not validate database schema.", e);
        }
    }

    private void startBatchCommit() {
        batchCommit.set(true);
        try {
            ds.getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            throw new OperatingError("Unable to start batch commit.", e);
        }
    }

    private void endBatchCommit() {
        batchCommit.set(false);
        try {
            ds.getConnection().commit();
            ds.getConnection().setAutoCommit(true);
        } catch (SQLException e) {
            throw new OperatingError("Unable to end batch commit.", e);
        }
    }

    private Map<Long, Map<String, String>> extract(final ResultSet rs) throws SQLException {
        final Map<Long, Map<String, String>> result = new HashMap<>();
        while (rs.next()) {
            final Long id = rs.getLong("ID");
            final String component = rs.getString("COMPONENT");
            final String value = rs.getString("VALUE");
            final Map<String, String> comps = result.computeIfAbsent(id, k -> new HashMap<>());
            comps.put(component, value);
        }
        return result;
    }

    public void write(final Long id, final String name, final String value) {
        if (readById(id).containsKey(name)) {
            try (final Connection c = ds.getConnection()) {
                final PreparedStatement ps = c.prepareStatement(UPDATE);
                ps.setString(1, value);
                ps.setLong(2, id);
                ps.setString(3, name);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new OperatingError("Could not update value for ID " + id + " and component " + name, e);
            }
        } else {
            try (final Connection c = ds.getConnection()) {
                final PreparedStatement ps = c.prepareStatement(INSERT);
                ps.setLong(1, id);
                ps.setString(2, name);
                ps.setString(3, value);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new OperatingError("Could not insert value for ID " + id + " and component " + name, e);
            }
        }
    }

    public void write(final Long id, final Component name, final String value) {
        write(id, name.name(), value);
    }

    public void write(final Long id, final Map<Component, String> values) {
        final Map<Component, String> copy = Map.copyOf(values);
        startBatchCommit();
        copy.forEach((c, v) -> write(id, c, v));
        endBatchCommit();
    }

    public void write(final Map<Long, Map<Component, String>> values) {
        final Map<Long, Map<Component, String>> copy = Map.copyOf(values);
        startBatchCommit();
        copy.forEach((id, comps) -> write(id, comps));
        endBatchCommit();
    }

    public Map<String, String> readById(final Long id) {
        try (final Connection c = ds.getConnection()) {
            final PreparedStatement ps = c.prepareStatement(SELECT_BY_ID);
            ps.setLong(1, id);
            return extract(ps.executeQuery()).get(id);
        } catch (SQLException e) {
            throw new OperatingError("Could not read results for ID " + id, e);
        }
    }

    public Map<Long, Map<String, String>> readByComponent(final String name) {
        try (final Connection c = ds.getConnection()) {
            final PreparedStatement ps = c.prepareStatement(SELECT_BY_COMPONENT);
            ps.setString(1, name);
            return extract(ps.executeQuery());
        } catch (SQLException e) {
            throw new OperatingError("Could not read results for component " + name, e);
        }
    }

    public Map<Long, Map<String, String>> readByComponentWithValue(final String name, final String value) {
        try (final Connection c = ds.getConnection()) {
            final PreparedStatement ps = c.prepareStatement(SELECT_BY_COMPONENT_AND_VALUE);
            ps.setString(1, name);
            ps.setString(2, value);
            return extract(ps.executeQuery());
        } catch (SQLException e) {
            throw new OperatingError("Could not read results for component " + name + " with value " + value, e);
        }
    }

    @Override
    public void handleInitialized(final Engine eng) {
        settings = eng.service(SettingService.class);
        ds = createDataSource();
        initializeDataSource();
        validateDatabase();
    }
}
