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
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.sk89q.eduardo.event.CommandEvent;
import com.sk89q.eduardo.helper.command.CommandProcessor;
import com.sk89q.eduardo.http.JettyService;
import com.sk89q.eduardo.irc.PircBotXService;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HelloWorld extends ListenerAdapter<PircBotX> {

    @Inject private CommandProcessor processor;

    @Inject
    public HelloWorld(PircBotXService bot, JettyService jetty, EventBus bus) {
        bot.registerListener(this);
        bus.register(this);

        ContextHandler testHandler = new ContextHandler("/hello");
        testHandler.setHandler(new HelloWorldHandler());
        jetty.registerHandler(testHandler);
    }

    @Subscribe
    public void onCommand(CommandEvent event) throws Exception {
        if (event.getArguments().startsWith("helloworld")) {
            event.respond("Hello world! ");
        }
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
