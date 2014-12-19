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

package com.sk89q.eduardo.service.command;

import com.google.inject.Inject;
import com.sk89q.eduardo.event.CommandEvent;
import com.sk89q.eduardo.event.CommandQueryEvent;
import com.sk89q.eduardo.event.message.MessageEvent;
import com.sk89q.eduardo.model.context.Context;
import com.sk89q.eduardo.model.response.Response;
import com.sk89q.eduardo.service.auth.AuthService;
import com.sk89q.eduardo.service.auth.Subject;
import com.sk89q.eduardo.service.event.EventBus;
import com.sk89q.eduardo.service.event.EventHandler.Priority;
import com.sk89q.eduardo.service.event.Subscribe;
import com.sk89q.eduardo.service.plugin.Plugin;
import com.sk89q.eduardo.service.throttle.RateLimiter;
import com.sk89q.eduardo.util.config.Config;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.CommandMapping;
import com.sk89q.intake.InvocationCommandException;
import com.sk89q.intake.context.CommandContext;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.dispatcher.SimpleDispatcher;
import com.sk89q.intake.parametric.ParametricBuilder;
import com.sk89q.intake.util.auth.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@Plugin(id = "command-manager")
public class SimpleCommandManager implements CommandManager {

    private static final Logger log = LoggerFactory.getLogger(CommandManager.class);
    private static final int MAX_DEPTH = 10;

    private final EventBus eventBus;
    private final Supplier<String> prefix;
    @Inject private AuthService authService;
    private final Dispatcher dispatcher;
    private final ParametricBuilder builder;

    @Inject
    public SimpleCommandManager(Config config, EventBus eventBus, AuthService authService, RateLimiter limiter) {
        this.prefix = config.stringAt("command.prefix", ".");
        this.eventBus = eventBus;
        eventBus.register(this);
        dispatcher = new SimpleDispatcher();
        builder = new ParametricBuilder();
        builder.setAuthorizer(new ServiceAuthorizer(authService));
        builder.addBinding(new DefaultBinding());
        builder.addExceptionConverter(new DefaultExceptionConverter());
        builder.addInvokeListener(new RateLimitListener(limiter));
    }

    @Override
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    public void register(Object object) {
        builder.registerMethodsAsCommands(dispatcher, object);
    }

    @Override
    public String removePrefix(String arguments) {
        String prefix = this.prefix.get();

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
            Context context = event.getContext();
            locals.put(CommandEvent.class, event);
            locals.put(Response.class, event.getResponse());
            locals.put(Context.class, context);
            locals.put(Subject.class, authService.login(event.getContext()));

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

    @Subscribe(ignoreCancelled = true)
    public void onCommandQueryEvent(CommandQueryEvent event) {
        if (event.getDescription() == null) {
            CommandLocals locals = new CommandLocals();
            Context context = event.getContext();
            locals.put(CommandEvent.class, event);
            locals.put(Context.class, context);
            locals.put(Subject.class, authService.login(event.getContext()));

            CommandMapping mapping = dispatcher.get(event.getCommand());
            if (mapping != null && mapping.getCallable().testPermission(locals)) {
                event.setDescription(mapping.getDescription().getShortDescription());
            }
        }
    }

    @Subscribe
    public void onMessage(MessageEvent event) {
        String message = event.getMessage();
        String prefix = this.prefix.get();

        if (message.length() > prefix.length() && message.startsWith(prefix)) {
            String arguments = message.substring(prefix.length());
            String[] split = CommandContext.split(arguments);
            String command = split[0];

            if (command.length() >= 2 && command.endsWith("?")) {
                if (authService.login(event.getContext()).testPermission("help")) {
                    String actualCommand = command.substring(0, command.length() - 1);

                    CommandQueryEvent queryEvent = new CommandQueryEvent(event.getContext(), actualCommand);
                    eventBus.post(queryEvent);

                    if (!queryEvent.isCancelled()) {
                        String desc = queryEvent.getDescription();
                        if (desc != null) {
                            event.getResponse().respond(actualCommand + ": " + desc);
                        } else {
                            event.getResponse().respond("No information is available for " + actualCommand);
                        }
                    }
                }
            } else {
                eventBus.post(new CommandEvent(event.getContext(), arguments, event.getResponse()));
            }
        }
    }

}
