package com.proudlobster.wumpus;

import com.proudlobster.wumpus.core.Engine;
import com.proudlobster.wumpus.core.service.CommandService;
import com.proudlobster.wumpus.core.service.ComponentService;
import com.proudlobster.wumpus.core.service.EntityService;
import com.proudlobster.wumpus.core.service.ProcessorService;
import com.proudlobster.wumpus.core.service.Service;
import com.proudlobster.wumpus.core.service.SettingService;
import com.proudlobster.wumpus.script.ScriptService;
import com.proudlobster.wumpus.server.WebSocketService;
import com.proudlobster.wumpus.server.service.AccountService;
import com.proudlobster.wumpus.server.service.EmailService;

public interface App {

    Service[] services = new Service[] {
            new SettingService(),
            new ComponentService(),
            new EntityService(),
            new EmailService(),
            new ProcessorService(),
            new CommandService(),
            new WebSocketService(),
            new AccountService(),
            new ScriptService(),
    };

    static void main(String... args) {
        final Engine engine = new Engine(services);
        engine.start();
    }
}
