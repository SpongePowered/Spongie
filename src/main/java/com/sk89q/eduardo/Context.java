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

import javax.annotation.Nullable;
import java.util.EnumSet;

public class Context {

    private String network;
    private String user;
    @Nullable private String channel;
    private EnumSet<ChannelUserMode> modes = EnumSet.noneOf(ChannelUserMode.class);

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    @Nullable
    public String getChannel() {
        return channel;
    }

    public void setChannel(@Nullable String channel) {
        this.channel = channel;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public EnumSet<ChannelUserMode> getModes() {
        return modes;
    }

    public void setModes(EnumSet<ChannelUserMode> modes) {
        this.modes = modes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Context context = (Context) o;

        if (channel != null ? !channel.equals(context.channel) : context.channel != null)
            return false;
        if (!modes.equals(context.modes)) return false;
        if (!network.equals(context.network)) return false;
        if (!user.equals(context.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = network.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + (channel != null ? channel.hashCode() : 0);
        result = 31 * result + modes.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Context{" +
                "network='" + network + '\'' +
                ", user=" + user +
                ", channel='" + channel + '\'' +
                ", modes=" + modes +
                '}';
    }

}
