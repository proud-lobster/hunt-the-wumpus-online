package com.proudlobster.wumpus.script;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.graalvm.polyglot.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proudlobster.wumpus.core.Engine;
import com.proudlobster.wumpus.core.error.CriticalError;
import com.proudlobster.wumpus.core.service.CommandService;
import com.proudlobster.wumpus.core.service.ComponentService;
import com.proudlobster.wumpus.core.service.EntityService;
import com.proudlobster.wumpus.core.service.LifecycleService;
import com.proudlobster.wumpus.core.service.ProcessorService;
import com.proudlobster.wumpus.core.service.Repository;
import com.proudlobster.wumpus.core.service.SettingService;
import com.proudlobster.wumpus.script.js.JsBootstrap;
import com.proudlobster.wumpus.script.js.JsCommand;
import com.proudlobster.wumpus.script.js.JsComponent;
import com.proudlobster.wumpus.script.js.JsComponentProcessor;
import com.proudlobster.wumpus.script.js.JsModule;
import com.proudlobster.wumpus.script.js.ScriptInterface;
import com.proudlobster.wumpus.server.WebSocketService;

public class ScriptService implements Repository<Source>, LifecycleService {

    public static final String LANG_ID = "js";
    private static final String LANG_EXT = ".js";

    private static Stream<URL> sourceURLsFromPath(final String path) {
        try {
            return Collections.list(ScriptService.class.getClassLoader().getResources(path)).stream();
        } catch (IOException e) {
            throw new CriticalError("Could not get URLs.", e);
        }
    }

    private static URL uriToUrl(final URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new CriticalError("Could not walk URL.", e);
        }
    }

    private static URI urlToUri(final URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new CriticalError("Could not convert to URI.", e);
        }
    }

    private static Stream<URL> walkFileUrl(final URL base, final String path) {
        try {
            return Files.walk(Paths.get(base.toURI()))
                    .filter(Files::isRegularFile)
                    .map(Path::toUri)
                    .map(ScriptService::uriToUrl);
        } catch (IOException | URISyntaxException e) {
            throw new CriticalError("Could not walk URL.", e);
        }
    }

    private static Source build(final Source.Builder builder) {
        try {
            return builder.build();
        } catch (IOException e) {
            throw new CriticalError("Could not build source.", e);
        }
    }

    private final Logger log;
    private final Map<String, Source> processorCache;
    private final Map<String, Source> commandCache;
    private final Map<String, Source> bootstrapCache;
    private final Map<String, Source> componentCache;
    private final org.graalvm.polyglot.Engine scriptEngine;

    private SettingService settings;
    private ComponentService components;
    private CommandService commands;
    private ProcessorService processors;
    private EntityService entities;
    private WebSocketService sockets;
    private String processorPath;
    private String commandPath;
    private String bootstrapPath;
    private String componentPath;

    public ScriptService() {
        this.log = LoggerFactory.getLogger("SCRIPT");
        this.processorCache = new ConcurrentHashMap<>();
        this.commandCache = new ConcurrentHashMap<>();
        this.bootstrapCache = new ConcurrentHashMap<>();
        this.componentCache = new ConcurrentHashMap<>();
        this.scriptEngine = org.graalvm.polyglot.Engine.newBuilder(LANG_ID).build();
    }

    private Stream<Source> sources(final String path) {
        return sourceURLsFromPath(path)
                .filter(url -> url.getProtocol().equals("file"))
                .flatMap(url -> walkFileUrl(url, path))
                .map(ScriptService::urlToUri)
                .map(uri -> Paths.get(uri).toFile())
                .filter(file -> file.toString().endsWith(LANG_EXT))
                .map(url -> Source.newBuilder(LANG_ID, url).mimeType("application/javascript+module"))
                .map(ScriptService::build);
    }

    @Override
    public void handleInitialized(final Engine eng) {
        settings = eng.service(SettingService.class);
        components = eng.service(ComponentService.class);
        commands = eng.service(CommandService.class);
        processors = eng.service(ProcessorService.class);
        entities = eng.service(EntityService.class);
        sockets = eng.service(WebSocketService.class);
    }

    @Override
    public void handleRunning() {
        final String basePath = settings.getCritical("script.js.paths.base");
        componentPath = basePath + "/" + settings.getCritical("script.js.paths.component");
        processorPath = basePath + "/" + settings.getCritical("script.js.paths.processor");
        commandPath = basePath + "/" + settings.getCritical("script.js.paths.command");
        bootstrapPath = basePath + "/" + settings.getCritical("script.js.paths.bootstrap");

        log.info("Caching Components...");
        sources(componentPath)
                .peek(s -> log.info(s.getName()))
                .forEach(s -> componentCache.put(s.getName(), s));

        log.info("Caching Processors...");
        sources(processorPath)
                .peek(s -> log.info(s.getName()))
                .forEach(s -> processorCache.put(s.getName(), s));

        log.info("Caching Commands...");
        sources(commandPath)
                .peek(s -> log.info(s.getName()))
                .forEach(s -> commandCache.put(s.getName(), s));

        log.info("Caching Boostrap Scripts...");
        sources(bootstrapPath)
                .peek(s -> log.info(s.getName()))
                .forEach(s -> bootstrapCache.put(s.getName(), s));

        log.info("Loading Components...");
        componentCache.values().stream()
                .map(c -> JsModule.load(c, scriptEngine, this))
                .map(m -> new JsComponent(m))
                .forEach(components::register);

        log.info("Loading Processors...");
        processorCache.values().stream()
                .map(p -> JsModule.load(p, scriptEngine, this))
                .map(m -> new JsComponentProcessor(m, components))
                .forEach(processors::register);

        log.info("Loading commands...");
        commandCache.values().stream()
                .map(c -> JsModule.load(c, scriptEngine, this))
                .map(JsCommand::load)
                .forEach(commands::register);

        log.info("Running bootstraps...");
        bootstrapCache.values().stream()
                .map(c -> new JsBootstrap(this, c))
                .forEach(bs -> bs.get());

        log.info("Script loading complete.");
    }

    @Override
    public Source apply(final String t) {
        final String[] parts = t.split("\\/", 2);
        if (parts.length == 2) {
            switch (parts[0]) {
                case "processor":
                    return processorCache.get(parts[1]);
                case "command":
                    return commandCache.get(parts[1]);
                case "bootstrap":
                    return bootstrapCache.get(parts[1]);
            }
        }

        return null;
    }

    public Optional<JsModule> getModule(final String name, final Engine eng) {
        return this.get(name).map(s -> JsModule.load(s, scriptEngine, this));
    }

    public ScriptInterface createScriptInterface() {
        return new ScriptInterface(entities, components, sockets);
    }

}
