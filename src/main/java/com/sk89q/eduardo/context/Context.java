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

package com.sk89q.eduardo.context;

import javax.annotation.Nullable;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class Context {

    private final Network network;
    private final User user;
    @Nullable private final Room room;
    private final Set<Mode> modes;

    public Context(Network network, User user, @Nullable Room room, Set<Mode> modes) {
        checkNotNull(network);
        checkNotNull(user);
        checkNotNull(modes);
        this.network = network;
        this.user = user;
        this.room = room;
        this.modes = modes;
    }

    public Network getNetwork() {
        return network;
    }

    public User getUser() {
        return user;
    }

    @Nullable
    public Room getRoom() {
        return room;
    }

    public Set<Mode> getModes() {
        return modes;
    }

}
