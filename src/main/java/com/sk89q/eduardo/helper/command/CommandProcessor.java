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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.auth.AuthService;
import com.sk89q.eduardo.auth.Subject;
import com.sk89q.eduardo.event.CommandEvent;
import com.sk89q.eduardo.irc.IrcContext;
import com.sk89q.eduardo.irc.IrcContexts;
import com.sk89q.eduardo.irc.PircBotXService;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.dispatcher.SimpleDispatcher;
import com.sk89q.intake.parametric.ParametricBuilder;
import com.sk89q.intake.util.auth.AuthorizationException;
import com.typesafe.config.Config;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CommandProcessor extends ListenerAdapter<PircBotX> {

    private static final Logger log = LoggerFactory.getLogger(CommandProcessor.class);

    @Inject private Config config;
    @Inject private EventBus eventBus;
    @Inject private AuthService authService;
    private final Dispatcher dispatcher;
    private final ParametricBuilder builder;

    @Inject
    public CommandProcessor(PircBotXService bot, AuthService authService) {
        bot.registerListener(this);

        dispatcher = new SimpleDispatcher();
        builder = new ParametricBuilder();
        builder.setAuthorizer(new AuthServiceAuthorizer(authService));
        builder.addBinding(new DefaultBinding());
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public void registerCommands(Object object) {
        builder.registerMethodsAsCommands(dispatcher, object);
    }

    @Override
    public void onGenericMessage(GenericMessageEvent<PircBotX> event) {
        String message = event.getMessage();
        String prefix = config.getString("command.prefix");
        if (message.length() > prefix.length() && message.startsWith(prefix)) {
            String arguments = message.substring(prefix.length());

            CommandEvent commandEvent = new CommandEvent(event, arguments);
            eventBus.post(commandEvent);

            if (!commandEvent.isCancelled()) {
                CommandLocals locals = new CommandLocals();
                IrcContext context = IrcContexts.create(event);
                locals.put(GenericMessageEvent.class, event);
                locals.put(IrcContext.class, context);
                locals.put(Subject.class, authService.login(context));

                try {
                    dispatcher.call(arguments, locals, new String[0]);
                } catch (CommandException e) {
                    log.warn("Failed to execute a command", e);
                    event.respond("Failed to execute the command");
                } catch (AuthorizationException ignored) {
                    // Don't do anything
                }
            }
        }
    }

}
