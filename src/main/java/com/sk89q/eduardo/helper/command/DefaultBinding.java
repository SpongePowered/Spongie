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

import com.sk89q.eduardo.Context;
import com.sk89q.eduardo.auth.Subject;
import com.sk89q.eduardo.event.CommandEvent;
import com.sk89q.eduardo.helper.Response;
import com.sk89q.intake.parametric.ParameterException;
import com.sk89q.intake.parametric.argument.ArgumentStack;
import com.sk89q.intake.parametric.binding.BindingBehavior;
import com.sk89q.intake.parametric.binding.BindingHelper;
import com.sk89q.intake.parametric.binding.BindingMatch;

public class DefaultBinding extends BindingHelper {

    @BindingMatch(type = CommandEvent.class, behavior = BindingBehavior.PROVIDES, consumedCount = -1)
    public CommandEvent getCommandEvent(ArgumentStack context) throws ParameterException {
        CommandEvent event = context.getContext().getLocals().get(CommandEvent.class);
        context.markConsumed();
        if (event == null) {
            throw new ParameterException("Uh oh! The CommandEvent is not known.");
        } else {
            return event;
        }
    }

    @BindingMatch(type = Context.class, behavior = BindingBehavior.PROVIDES, consumedCount = -1)
    public Context getContext(ArgumentStack context) throws ParameterException {
        Context c = context.getContext().getLocals().get(Context.class);
        if (c == null) {
            throw new ParameterException("Uh oh! The Context is not known.");
        } else {
            return c;
        }
    }

    @BindingMatch(type = Subject.class, behavior = BindingBehavior.PROVIDES, consumedCount = -1)
    public Subject getSubject(ArgumentStack context) throws ParameterException {
        Subject subject = context.getContext().getLocals().get(Subject.class);
        if (subject == null) {
            throw new ParameterException("Uh oh! The subject of the command is not known.");
        } else {
            return subject;
        }
    }

    @BindingMatch(type = Response.class, behavior = BindingBehavior.PROVIDES, consumedCount = -1)
    public Response getResponse(ArgumentStack context) throws ParameterException {
        Response response = context.getContext().getLocals().get(Response.class);
        if (response == null) {
            throw new ParameterException("Uh oh! The Response is not known.");
        } else {
            return response;
        }
    }

}
