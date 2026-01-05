package com.proudlobster.wumpus.server.configuration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public interface SslConnector extends Connector {

    public static SslConnector create(final Server server, final String keyStorePath, final String keyStorePassword,
            final int port) {
        final HttpConnectionFactory httpFactory = new HttpConnectionFactory();

        final SslContextFactory.Server ctxFactory = new SslContextFactory.Server();
        ctxFactory.setKeyStorePath(keyStorePath);
        ctxFactory.setKeyStorePassword(keyStorePassword);

        final SslConnectionFactory conFactory = new SslConnectionFactory(ctxFactory, httpFactory.getProtocol());

        final ServerConnector connector = new ServerConnector(server, conFactory, httpFactory);
        connector.setPort(port);

        final InvocationHandler delegate = (d, m, a) -> m.invoke(connector, a);
        return SslConnector.class.cast(Proxy.newProxyInstance(SslConnector.class.getClassLoader(),
                new Class[] { SslConnector.class }, delegate));
    }

}
