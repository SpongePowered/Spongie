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

package com.sk89q.eduardo.helper.command;

import com.sk89q.eduardo.helper.throttle.RateLimiter;
import com.sk89q.eduardo.irc.IrcContext;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.SettableDescription;
import com.sk89q.intake.context.CommandContext;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.parametric.ParameterData;
import com.sk89q.intake.parametric.ParameterException;
import com.sk89q.intake.parametric.handler.InvokeHandler;
import com.sk89q.intake.parametric.handler.InvokeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

public class RateLimitListener implements InvokeListener, InvokeHandler {

    private static final Logger log = LoggerFactory.getLogger(RateLimitListener.class);
    private final RateLimiter limiter;

    public RateLimitListener(RateLimiter limiter) {
        checkNotNull(limiter);
        this.limiter = limiter;
    }

    @Override
    public InvokeHandler createInvokeHandler() {
        return this;
    }

    @Override
    public void updateDescription(Object object, Method method, ParameterData[] data, SettableDescription desc) {
    }

    @Override
    public boolean preProcess(Object o, Method method, ParameterData[] data, CommandContext context, CommandLocals locals) throws CommandException, ParameterException {
        RateLimit limit = method.getAnnotation(RateLimit.class);

        IrcContext callerContext = locals.get(IrcContext.class);
        if (callerContext == null) {
            log.warn("Tried to handle @RateLimit but Subject is not available while handling " + context.getCommand(),
                    new RuntimeException("Failed to get Subject from locals"));
            return false;
        }

        if (!limiter.tryConsume(callerContext, limit.weight())) {
            log.info("Command usage was rate limited ({})", callerContext);
            return false;
        }

        return true;
    }

    @Override
    public boolean preInvoke(Object o, Method method, ParameterData[] data, Object[] objects, CommandContext context, CommandLocals locals) throws CommandException, ParameterException {
        return true;
    }

    @Override
    public void postInvoke(Object o, Method method, ParameterData[] data, Object[] objects, CommandContext context, CommandLocals locals) throws CommandException, ParameterException {
    }
}
