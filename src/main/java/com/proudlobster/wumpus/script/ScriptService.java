package com.proudlobster.wumpus.script;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

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
import com.proudlobster.wumpus.core.service.SettingService;
import com.proudlobster.wumpus.script.js.JsBootstrap;
import com.proudlobster.wumpus.script.js.JsCommand;
import com.proudlobster.wumpus.script.js.JsComponent;
import com.proudlobster.wumpus.script.js.JsComponentProcessor;
import com.proudlobster.wumpus.script.js.JsModule;
import com.proudlobster.wumpus.server.WebSocketService;

public class ScriptService implements LifecycleService {

    private static Source loadSource(final Path file) {
        try {
            return Source.newBuilder("js", file.toFile()).mimeType("application/javascript+module").build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final Logger log = LoggerFactory.getLogger("SCRIPT");
    private SettingService settings;
    private ComponentService components;
    private ProcessorService processors;
    private CommandService commands;
    private ScriptExecutor exec;

    @Override
    public void handleInitialized(final Engine eng) {
        settings = eng.service(SettingService.class);
        components = eng.service(ComponentService.class);
        processors = eng.service(ProcessorService.class);
        commands = eng.service(CommandService.class);

        final ScriptInterface scriptInterface = new ScriptInterface(
                eng.service(EntityService.class),
                components,
                eng.service(WebSocketService.class));
        exec = new ScriptExecutor(scriptInterface);

    }

    @Override
    public void handleRunning() {

        final String baseDir = settings.require("script.js.paths.base");
        log.info("Loading scripts from base directory: {}", baseDir);
        log.info("Loading component scripts...");
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Path.of(baseDir + "/component"))) {
            StreamSupport.stream(ds.spliterator(), false)
                    .sorted().sequential()
                    .peek(path -> log.info(path.toString()))
                    .map(ScriptService::loadSource)
                    .map(m -> JsModule.load(m, exec))
                    .map(JsComponent::new)
                    .forEach(components::register);
        } catch (IOException e) {
            throw new CriticalError("Failed to load component script.", e);
        }

        log.info("Running bootstrap scripts...");
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Path.of(baseDir + "/bootstrap"))) {
            StreamSupport.stream(ds.spliterator(), false)
                    .sorted().sequential()
                    .peek(path -> log.info(path.toString()))
                    .map(ScriptService::loadSource)
                    .map(m -> JsModule.load(m, exec))
                    .map(JsBootstrap::load)
                    .forEach(b -> b.run());
        } catch (IOException e) {
            throw new CriticalError("Failed to run bootstrap script.", e);
        }

        log.info("Loading processor scripts...");
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Path.of(baseDir + "/processor"))) {
            StreamSupport.stream(ds.spliterator(), false)
                    .sorted().sequential()
                    .peek(path -> log.info(path.toString()))
                    .map(ScriptService::loadSource)
                    .map(m -> JsModule.load(m, exec))
                    .map(m -> new JsComponentProcessor(m, components))
                    .forEach(processors::register);
        } catch (IOException e) {
            throw new CriticalError("Failed to load processor script.", e);
        }

        log.info("Loading command scripts...");
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Path.of(baseDir + "/command"))) {
            StreamSupport.stream(ds.spliterator(), false)
                    .sorted().sequential()
                    .peek(path -> log.info(path.toString()))
                    .map(ScriptService::loadSource)
                    .map(m -> JsModule.load(m, exec))
                    .map(JsCommand::load)
                    .forEach(commands::register);
        } catch (IOException e) {
            throw new CriticalError("Failed to load command script.", e);
        }

        log.info("Script loading complete.");
    }

}
