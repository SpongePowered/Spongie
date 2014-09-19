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

import com.sk89q.eduardo.context.Context;
import com.sk89q.eduardo.helper.Response;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class CommandEvent implements Cancellable {

    private final Response response;
    private final Context primaryContext;
    private final Set<Context> contexts = new HashSet<>();
    private final String arguments;
    private final int depth;
    private boolean cancelled;

    public CommandEvent(Context primaryContext, String arguments, Response response) {
        this(primaryContext, arguments, response, 0);
    }

    public CommandEvent(Context primaryContext, String arguments, Response response, int depth) {
        checkNotNull(primaryContext);
        checkNotNull(arguments);
        checkNotNull(response);
        checkArgument(depth >= 0, "depth must be >= 0");
        this.primaryContext = primaryContext;
        this.response = response;
        this.arguments = arguments;
        this.depth = depth;
        this.contexts.add(primaryContext);
    }

    public Context getPrimaryContext() {
        return primaryContext;
    }

    public Set<Context> getContexts() {
        return contexts;
    }

    public String getArguments() {
        return arguments;
    }

    public Response getResponse() {
        return response;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void respond(String message) {
        response.respond(message);
    }

}
