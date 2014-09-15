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

package com.sk89q.eduardo.module;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.helper.Response;
import com.sk89q.eduardo.helper.command.CommandProcessor;
import com.sk89q.eduardo.helper.command.RateLimit;
import com.sk89q.eduardo.http.JettyService;
import com.sk89q.intake.Command;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class HelloWorld {

    @Inject
    public HelloWorld(CommandProcessor commands, JettyService jetty, EventBus bus) {
        commands.registerCommands(this);
        bus.register(this);

        ContextHandler testHandler = new ContextHandler("/hello");
        testHandler.setHandler(new HelloWorldHandler());
        jetty.registerHandler(testHandler);
    }

    @Command(aliases = "hello", desc = "Hello world!")
    @RateLimit
    public void helloWorld(Response response) {
        response.respond("Hello world!");
    }

    private class HelloWorldHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            response.setContentType("text/html; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            response.getWriter().println("<h1>Hello World</h1>");
        }
    }

}
