package com.proudlobster.wumpus.server.configuration;

import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer.Configurator;

import jakarta.servlet.ServletContext;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpointConfig;

public class WumpusConfigurator implements Configurator {

    private final ServerEndpointConfig.Configurator endpointConfig;
    private final String endpointPath;

    public WumpusConfigurator(final SessionHandler sessions, final String endpointPath) {
        this.endpointPath = endpointPath;

        final Endpoint endpoint = new WumpusEndpoint(sessions);
        endpointConfig = new ServerEndpointConfig.Configurator() {
            @Override
            public <T> T getEndpointInstance(final Class<T> endpointClass) throws InstantiationException {
                if (endpointClass.isInstance(endpoint)) {
                    return endpointClass.cast(endpoint);
                } else {
                    throw new InstantiationException("No endpoint for type " + endpointClass.getName());
                }
            }
        };
    }

    @Override
    public void accept(ServletContext servletContext, ServerContainer serverContainer) throws DeploymentException {
        serverContainer.addEndpoint(ServerEndpointConfig.Builder
                .create(WumpusEndpoint.class, endpointPath)
                .configurator(endpointConfig)
                .build());
    }

}
