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

package com.sk89q.eduardo.event;

import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;

public class CommandEvent implements Cancellable {

    private final GenericMessageEvent<PircBotX> messageEvent;
    private final String arguments;
    private boolean cancelled;

    public CommandEvent(GenericMessageEvent<PircBotX> messageEvent, String arguments) {
        this.messageEvent = messageEvent;
        this.arguments = arguments;
    }

    public GenericMessageEvent<PircBotX> getMessageEvent() {
        return messageEvent;
    }

    public void respond(String response) {
        messageEvent.respond(response);
    }

    public User getUser() {
        return messageEvent.getUser();
    }

    public String getMessage() {
        return messageEvent.getMessage();
    }

    public String getArguments() {
        return arguments;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
