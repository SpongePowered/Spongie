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

package com.sk89q.eduardo.helper;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.sk89q.eduardo.event.CommandEvent;
import com.sk89q.eduardo.irc.PircBotXService;
import com.typesafe.config.Config;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

public class CommandProcessor extends ListenerAdapter<PircBotX> {

    @Inject private Config config;
    @Inject private EventBus eventBus;

    @Inject
    public CommandProcessor(PircBotXService bot) {
        bot.registerListener(this);
    }

    @Override
    public void onGenericMessage(GenericMessageEvent<PircBotX> event) throws Exception {
        String message = event.getMessage();
        String prefix = config.getString("command.prefix");
        if (message.length() > prefix.length() && message.startsWith(prefix)) {
            String arguments = message.substring(prefix.length());
            eventBus.post(new CommandEvent(event, arguments));
        }
    }

}
