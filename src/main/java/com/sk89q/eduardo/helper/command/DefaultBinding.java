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

import com.sk89q.eduardo.auth.Subject;
import com.sk89q.eduardo.helper.Response;
import com.sk89q.intake.parametric.ParameterException;
import com.sk89q.intake.parametric.argument.ArgumentStack;
import com.sk89q.intake.parametric.binding.BindingBehavior;
import com.sk89q.intake.parametric.binding.BindingHelper;
import com.sk89q.intake.parametric.binding.BindingMatch;
import org.pircbotx.hooks.types.GenericMessageEvent;

public class DefaultBinding extends BindingHelper {

    @BindingMatch(type = Subject.class,
                  behavior = BindingBehavior.PROVIDES,
                  consumedCount = -1)
    public Subject getSubject(ArgumentStack context) throws ParameterException {
        Subject subject = context.getContext().getLocals().get(Subject.class);
        if (subject == null) {
            throw new ParameterException("Uh oh! The subject of the command is not known.");
        } else {
            return subject;
        }
    }

    @BindingMatch(type = GenericMessageEvent.class,
            behavior = BindingBehavior.PROVIDES,
            consumedCount = -1)
    public GenericMessageEvent getMessageEvent(ArgumentStack context) throws ParameterException {
        GenericMessageEvent event = context.getContext().getLocals().get(GenericMessageEvent.class);
        if (event == null) {
            throw new ParameterException("Uh oh! The GenericMessageEvent is not known.");
        } else {
            return event;
        }
    }

    @BindingMatch(type = Response.class,
            behavior = BindingBehavior.PROVIDES,
            consumedCount = -1)
    public Response getResponse(ArgumentStack context) throws ParameterException {
        GenericMessageEvent event = context.getContext().getLocals().get(GenericMessageEvent.class);
        if (event == null) {
            throw new ParameterException("Uh oh! The GenericMessageEvent is not known.");
        } else {
            return event::respond;
        }
    }

}
