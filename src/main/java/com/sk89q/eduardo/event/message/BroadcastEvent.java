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

import com.sk89q.eduardo.util.formatting.StyledFragment;

import static com.google.common.base.Preconditions.checkNotNull;

public class BroadcastEvent {

    private final String target;
    private final StyledFragment message;

    public BroadcastEvent(String target, StyledFragment message) {
        checkNotNull(target);
        checkNotNull(message);
        this.target = target;
        this.message = message;
    }

    public String getTarget() {
        return target;
    }

    public StyledFragment getMessage() {
        return message;
    }
}
