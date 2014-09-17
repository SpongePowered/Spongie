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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.event.CommandEvent;
import com.sk89q.eduardo.helper.AutoRegister;
import com.sk89q.eduardo.util.eventbus.EventBus;
import com.sk89q.intake.Command;
import com.sk89q.intake.context.CommandContext;

@AutoRegister
@Singleton
public class Commands {

    @Inject private EventBus eventBus;

    @Command(aliases = "&", desc = "Run several commands")
    public void create(CommandEvent event) {
        int newDepth = event.getDepth() + 1;

        String[] split = CommandContext.split(event.getArguments());
        String arguments = event.getArguments().substring(split[0].length());
        String[] commands = arguments.split("&");

        for (String command : commands) {
            CommandEvent commandEvent = new CommandEvent(event.getPrimaryContext(), command.trim(), event.getResponse(), newDepth);
            commandEvent.getContexts().addAll(event.getContexts());
            eventBus.post(commandEvent);
        }
    }

}
