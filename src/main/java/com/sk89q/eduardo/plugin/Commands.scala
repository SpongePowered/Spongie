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

package com.sk89q.eduardo.plugin

import com.google.inject.{Inject, Singleton}
import com.sk89q.eduardo.event.CommandEvent
import com.sk89q.eduardo.service.event.EventBus
import com.sk89q.eduardo.service.plugin.Plugin
import com.sk89q.intake.Command
import com.sk89q.intake.context.CommandContext

@Plugin(id = "commands")
class Commands @Inject() (eventBus: EventBus) {

  @Command(aliases = Array("commands:join", "&"), desc = "Run several commands")
  def joinCommand(event: CommandEvent) {
    val newDepth: Int = event.getDepth + 1
    val split = CommandContext.split(event.getArguments)
    val arguments = event.getArguments.substring(split(0).length)
    val commands = arguments.split("&")
    for (command <- commands) {
      eventBus.post(new CommandEvent(event.getContext, command.trim, event.getResponse, newDepth))
    }
  }

}