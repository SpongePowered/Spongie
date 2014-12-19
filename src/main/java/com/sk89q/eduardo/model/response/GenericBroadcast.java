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

package com.sk89q.eduardo.model.response;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.event.message.BroadcastEvent;
import com.sk89q.eduardo.service.event.EventBus;
import com.sk89q.eduardo.util.formatting.StyledFragment;

@Singleton
public class GenericBroadcast {

    @Inject private EventBus eventBus;

    public void broadcast(String target, StyledFragment message) {
        eventBus.post(new BroadcastEvent(target, message));
    }

}
