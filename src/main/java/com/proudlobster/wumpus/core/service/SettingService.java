package com.proudlobster.wumpus.core.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import com.proudlobster.wumpus.core.Engine;
import com.proudlobster.wumpus.core.error.CriticalError;
import com.proudlobster.wumpus.core.error.OperatingError;
import com.proudlobster.wumpus.core.utility.Template;
import com.proudlobster.wumpus.core.utility.Validator;

public class SettingService implements Repository<String>, LifecycleService {

    public static final String DEFAULT_PROP_LOC = "default.properties";
    public static final String ERROR_DEFAULT_PROP = "Could not load default properties.";
    public static final String APP_PROP_LOC = "application.properties";
    public static final String ERROR_APP_PROP = "Could not load application properties.";
    public static final Template PROP_NOT_FOUND = Template.of("Required setting ''{0}'' is missing.");

    private Properties props;
    private final Properties overrides;

    public SettingService() {
        overrides = new Properties();
        refresh();
    }

    private void loadDefaults(final Properties p) {
        final InputStream is = Engine.class.getClassLoader().getResourceAsStream(DEFAULT_PROP_LOC);
        try {
            p.load(is);
        } catch (IOException e) {
            throw new CriticalError(ERROR_DEFAULT_PROP, e);
        }
    }

    private void loadAppProperties(final Properties p) {
        final Path appPropPath = Path.of(APP_PROP_LOC);
        if (Files.exists(appPropPath)) {
            try (final InputStream is = new FileInputStream(appPropPath.toFile())) {
                p.load(is);
            } catch (IOException e) {
                throw new OperatingError(ERROR_APP_PROP, e);
            }
        }
    }

    private void loadEnvironmentProperties(final Properties p) {
        System.getenv()
                .entrySet()
                .stream()
                .forEach(e -> p.setProperty(e.getKey(), e.getValue()));
    }

    private void refresh() {
        Engine.LOG.info("Loading settings...");
        final Properties props = new Properties();

        loadDefaults(props);
        loadAppProperties(props);
        loadEnvironmentProperties(props);
        props.putAll(System.getProperties());
        props.putAll(overrides);

        this.props = props;
    }

    @Override
    public String apply(String t) {
        return props.getProperty(t);
    }

    @Override
    public void register(String p) {
        final String[] parts = p.split("=", 2);
        overrides.setProperty(parts[0], parts[1]);
        refresh();
    }

    public void register(String... ps) {
        Arrays.stream(ps).forEach(p -> {
            final String[] parts = p.split("=", 2);
            overrides.setProperty(parts[0], parts[1]);
        });
        refresh();
    }

    public Optional<String> attempt(final String key) {
        return Optional.ofNullable(apply(key));
    }

    public String attemptOrThrow(final String key) {
        return attempt(key).orElseThrow(() -> new OperatingError(PROP_NOT_FOUND, key));
    }

    public String require(final String key) {
        return attempt(key).orElseThrow(() -> new CriticalError(PROP_NOT_FOUND, key));
    }

    public Optional<Long> attemptNumber(final String key) {
        return Validator.tryToLong(attempt(key).orElse(null));
    }

    public Long attemptNumberOrThrow(final String key) {
        return Validator.tryToLong(attemptOrThrow(key))
                .orElseThrow(() -> new OperatingError(PROP_NOT_FOUND, key));
    }

    public Long requireNumber(final String key) {
        return Validator.tryToLong(require(key))
                .orElseThrow(() -> new CriticalError(PROP_NOT_FOUND, key));
    }
}
