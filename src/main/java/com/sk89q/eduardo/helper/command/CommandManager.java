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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.auth.AuthService;
import com.sk89q.eduardo.auth.Subject;
import com.sk89q.eduardo.event.CommandEvent;
import com.sk89q.eduardo.helper.throttle.RateLimiter;
import com.sk89q.eduardo.irc.IrcBot;
import com.sk89q.eduardo.irc.IrcContext;
import com.sk89q.eduardo.irc.IrcContexts;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.InvocationCommandException;
import com.sk89q.intake.context.CommandContext;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.dispatcher.SimpleDispatcher;
import com.sk89q.intake.parametric.ParametricBuilder;
import com.sk89q.intake.util.auth.AuthorizationException;
import com.typesafe.config.Config;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CommandManager {

    private static final Logger log = LoggerFactory.getLogger(CommandManager.class);

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

    @Subscribe
    public void onGenericMessage(GenericMessageEvent<IrcBot> event) {
        String message = event.getMessage();
        String prefix = config.getString("command.prefix");
        if (message.length() > prefix.length() && message.startsWith(prefix)) {
            String arguments = message.substring(prefix.length());

            CommandEvent commandEvent = new CommandEvent(event, arguments);
            eventBus.post(commandEvent);

            if (!commandEvent.isCancelled()) {
                String[] split = CommandContext.split(arguments);

                if (split.length > 0 && dispatcher.contains(split[0])) {
                    CommandLocals locals = new CommandLocals();
                    IrcContext context = IrcContexts.create(event);
                    locals.put(GenericMessageEvent.class, event);
                    locals.put(IrcContext.class, context);
                    locals.put(Subject.class, authService.login(context));

                    try {
                        dispatcher.call(arguments, locals, new String[0]);
                    } catch (InvocationCommandException e) {
                        log.warn("Failed to execute a command", e);
                        event.respond("An unexpected error occurred while executing the command");
                    } catch (CommandException e) {
                        event.respond("error: " + e.getMessage());
                    } catch (AuthorizationException ignored) {
                        log.info("User was not permitted to run !" + arguments);
                    }
                }
            }
        }
    }

}
