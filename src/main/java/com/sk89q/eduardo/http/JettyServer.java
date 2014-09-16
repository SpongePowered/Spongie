/*
 * Eduardo, an IRC bot framework
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) Eduardo team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.eduardo.http;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.event.StartupEvent;
import com.typesafe.config.Config;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class JettyServer {

    private static final Logger log = LoggerFactory.getLogger(JettyServer.class);

    private final Config config;
    private final List<Handler> handlers = new ArrayList<Handler>();

    @Inject
    public JettyServer(Config config, EventBus eventBus) {
        this.config = config;
        eventBus.register(this);
    }

    @Subscribe
    public void onStartup(StartupEvent event) {
        Server server = new Server();

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setHost(config.getString("http.bind_address"));
        connector.setPort(config.getInt("http.port"));
        server.setConnectors(new Connector[] { connector });

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        Handler[] handlersArr = new Handler[handlers.size()];
        handlers.toArray(handlersArr);
        contexts.setHandlers(handlersArr);
        server.setHandler(contexts);

        try {
            server.start();
        } catch (Exception e) {
            log.info("Failed to start server", e);
        }
    }

    public void registerHandler(Handler handler) {
        handlers.add(handler);
    }


}
