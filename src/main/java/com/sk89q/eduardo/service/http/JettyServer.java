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

package com.sk89q.eduardo.service.http;

import com.google.inject.Inject;
import com.sk89q.eduardo.event.StartupEvent;
import com.sk89q.eduardo.event.http.ConfigureRouteEvent;
import com.sk89q.eduardo.service.event.EventBus;
import com.sk89q.eduardo.service.event.Subscribe;
import com.sk89q.eduardo.service.http.status.HttpStatusException;
import com.sk89q.eduardo.service.plugin.Plugin;
import com.sk89q.eduardo.util.config.Config;

import java.net.UnknownHostException;

import static com.google.common.collect.ImmutableMap.of;
import static com.sk89q.eduardo.service.http.SparkUtils.render;
import static spark.Spark.*;

@Plugin(id = "http-server")
public class JettyServer {

    @Inject private Config config;
    @Inject private EventBus eventBus;

    @Subscribe
    public void onStartup(StartupEvent event) throws UnknownHostException {
        setIpAddress(config.getString("http.bind-address", "0.0.0.0"));
        setPort(config.getInt("http.port", 8005));

        staticFileLocation("/public");

        eventBus.post(new ConfigureRouteEvent());

        exception(HttpStatusException.class, (e, request, response) -> {
            response.status(((HttpStatusException) e).getCode());
            response.body(render(of("error", e.getMessage()), "error.mustache"));
        });

    }

}
