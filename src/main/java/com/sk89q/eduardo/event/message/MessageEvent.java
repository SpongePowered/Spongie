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

package com.sk89q.eduardo.event.message;

import com.sk89q.eduardo.Context;
import com.sk89q.eduardo.helper.Response;

import static com.google.common.base.Preconditions.checkNotNull;

public class MessageEvent {

    private final Context context;
    private final String message;
    private final Response response;

    public MessageEvent(Context context, String message, Response response) {
        checkNotNull(context);
        checkNotNull(message);
        checkNotNull(response);
        this.context = context;
        this.message = message;
        this.response = response;
    }

    public Context getContext() {
        return context;
    }

    public String getMessage() {
        return message;
    }

    public Response getResponse() {
        return response;
    }

}
