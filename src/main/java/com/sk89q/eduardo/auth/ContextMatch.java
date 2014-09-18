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

package com.sk89q.eduardo.auth;

import com.sk89q.eduardo.Context;
import com.sk89q.eduardo.connector.irc.ChannelUserMode;
import com.sk89q.eduardo.util.text.FnMatch;
import com.sk89q.eduardo.util.text.FnMatch.Flag;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

public class ContextMatch implements Predicate<Context> {

    private final Set<String> users = new HashSet<>();
    private final Set<String> channels = new HashSet<>();
    private final Set<ChannelUserMode> modes = new HashSet<>();

    public void matchUser(String s) {
        checkNotNull(s);
        users.add(s.toLowerCase());
    }

    public void matchAllUsers(Collection<String> c) {
        for (String s : c) {
            matchUser(s);
        }
    }

    public void matchChannel(String s) {
        checkNotNull(s);
        channels.add(s.toLowerCase());
    }

    public void matchAllChannels(Collection<String> c) {
        for (String s : c) {
            matchChannel(s);
        }
    }

    public void matchMode(ChannelUserMode mode) {
        checkNotNull(mode);
        modes.add(mode);
    }

    public void matchAllModes(Collection<ChannelUserMode> c) {
        for (ChannelUserMode mode : c) {
            matchMode(mode);
        }
    }

    @Override
    public boolean test(Context context) {
        if (!users.isEmpty()) {
            if (context.getUser() != null) {
                boolean pass = false;
                for (String pattern : users) {
                    if (FnMatch.fnmatch(pattern, context.getUser(), EnumSet.of(Flag.CASEFOLD))) {
                        pass = true;
                        break;
                    }
                }
                if (!pass) {
                    return false;
                }
            } else {
                return false;
            }
        }

        if (!channels.isEmpty()) {
            if (context.getChannel() != null) {
                boolean pass = channels.contains(context.getChannel());
                if (!pass) {
                    return false;
                }
            } else {
                return false;
            }
        }

        if (!modes.isEmpty()) {
            if (!context.getModes().isEmpty()) {
                boolean pass = false;
                for (ChannelUserMode mode : context.getModes()) {
                    if (modes.contains(mode)) {
                        pass = true;
                        break;
                    }
                }
                if (!pass) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

}
