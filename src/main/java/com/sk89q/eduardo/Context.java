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

package com.sk89q.eduardo;

import com.sk89q.eduardo.irc.ChannelUserMode;
import org.pircbotx.User;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class Context {

    private final User user;
    @Nullable private final String channel;
    private final EnumSet<ChannelUserMode> modes;

    public Context(User user, @Nullable String channel, List<ChannelUserMode> modes) {
        checkNotNull(user);
        this.user = user;
        this.channel = channel;
        if (modes.size() > 0) {
            this.modes = EnumSet.copyOf(modes);
        } else {
            this.modes = EnumSet.noneOf(ChannelUserMode.class);
        }
    }

    public Context(User user, @Nullable String channel, ChannelUserMode... modes) {
        this(user, channel, Arrays.asList(modes));
    }

    public User getUser() {
        return user;
    }

    @Nullable
    public String getChannel() {
        return channel;
    }

    public EnumSet<ChannelUserMode> getModes() {
        return modes;
    }

    @Override
    public String toString() {
        return "IrcContext{" +
                "user=" + user +
                ", channel='" + channel + '\'' +
                ", modes=" + modes +
                '}';
    }
}
