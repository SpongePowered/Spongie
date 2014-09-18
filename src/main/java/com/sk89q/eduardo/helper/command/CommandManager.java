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

package com.sk89q.eduardo.helper.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.Context;
import com.sk89q.eduardo.auth.AuthService;
import com.sk89q.eduardo.auth.Subject;
import com.sk89q.eduardo.event.CommandEvent;
import com.sk89q.eduardo.event.message.MessageEvent;
import com.sk89q.eduardo.helper.Response;
import com.sk89q.eduardo.helper.throttle.RateLimiter;
import com.sk89q.eduardo.util.eventbus.EventBus;
import com.sk89q.eduardo.util.eventbus.EventHandler.Priority;
import com.sk89q.eduardo.util.eventbus.Subscribe;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.InvocationCommandException;
import com.sk89q.intake.context.CommandContext;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.dispatcher.SimpleDispatcher;
import com.sk89q.intake.parametric.ParametricBuilder;
import com.sk89q.intake.util.auth.AuthorizationException;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CommandManager {

    private static final Logger log = LoggerFactory.getLogger(CommandManager.class);
    private static final int MAX_DEPTH = 10;

    private final EventBus eventBus;
    @Inject private Config config;
    @Inject private AuthService authService;
    private final Dispatcher dispatcher;
    private final ParametricBuilder builder;

    @Inject
    public CommandManager(EventBus eventBus, AuthService authService, RateLimiter limiter) {
        this.eventBus = eventBus;
        eventBus.register(this);
        dispatcher = new SimpleDispatcher();
        builder = new ParametricBuilder();
        builder.setAuthorizer(new ServiceAuthorizer(authService));
        builder.addBinding(new DefaultBinding());
        builder.addExceptionConverter(new DefaultExceptionConverter());
        builder.addInvokeListener(new RateLimitListener(limiter));
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public void registerCommands(Object object) {
        builder.registerMethodsAsCommands(dispatcher, object);
    }

    public String removePrefix(String arguments) {
        String prefix = config.getString("command.prefix");

        if (arguments.startsWith(prefix)) {
            return arguments.substring(prefix.length());
        } else {
            return arguments;
        }
    }

    @Subscribe(priority = Priority.VERY_EARLY, ignoreCancelled = true)
    public void handleCommandRecursion(CommandEvent event) {
        if (event.getDepth() >= MAX_DEPTH) {
            event.getResponse().respond("error: Commands have reached the maximum recursion limit");
            event.setCancelled(true);
        }
    }

    @Subscribe(ignoreCancelled = true)
    public void onCommand(CommandEvent event) {
        String[] split = CommandContext.split(event.getArguments());

        if (split.length > 0 && dispatcher.contains(split[0])) {
            CommandLocals locals = new CommandLocals();
            Context context = event.getPrimaryContext();
            locals.put(CommandEvent.class, event);
            locals.put(Response.class, event.getResponse());
            locals.put(Context.class, context);
            locals.put(Subject.class, authService.login(event.getContexts()));

            event.setCancelled(true);

            try {
                dispatcher.call(event.getArguments(), locals, new String[0]);
            } catch (InvocationCommandException e) {
                log.warn("Failed to execute a command", e);
                event.getResponse().respond("An unexpected error occurred while executing the command");
            } catch (CommandException e) {
                event.getResponse().respond("error: " + e.getMessage());
            } catch (AuthorizationException ignored) {
                log.info("User was not permitted to run !" + event.getArguments());
            }
        }
    }

    @Subscribe
    public void onMessage(MessageEvent event) {
        String message = event.getMessage();
        String prefix = config.getString("command.prefix");

        if (message.length() > prefix.length() && message.startsWith(prefix)) {
            String arguments = message.substring(prefix.length());
            eventBus.post(new CommandEvent(event.getContext(), arguments, event.getResponse()));
        }
    }

}
