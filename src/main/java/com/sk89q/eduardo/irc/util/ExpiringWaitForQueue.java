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

package com.sk89q.eduardo.irc.util;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.WaitForQueue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExpiringWaitForQueue extends WaitForQueue {

    public ExpiringWaitForQueue(PircBotX bot) {
        super(bot);
    }

    @SuppressWarnings("unchecked")
    public <E extends Event> E waitFor(Class<E> eventClass, long timeout, TimeUnit unit) throws InterruptedException {
        return (E) waitFor(Arrays.asList(eventClass), timeout, unit);
    }

    @Override
    public Event waitFor(List<Class<? extends Event>> eventClasses, long timeout, TimeUnit unit) throws InterruptedException {
        long start = System.nanoTime();
        do {
            Event curEvent = eventQueue.poll(timeout, unit);
            for (Class<? extends Event> curEventClass : eventClasses)
                if (curEventClass.isInstance(curEvent))
                    return curEvent;
        } while (System.nanoTime() - start <= unit.toNanos(timeout));
        throw new InterruptedException("Timed out");
    }

}
