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

import com.google.common.base.Joiner
import com.google.inject.{Inject, Singleton}
import com.sk89q.eduardo.model.response.Response
import com.sk89q.eduardo.service.command.CommandManager
import com.sk89q.eduardo.service.plugin.Plugin
import com.sk89q.eduardo.util.Paginator
import com.sk89q.eduardo.util.formatting.Style
import com.sk89q.eduardo.util.formatting.StyledFragment.style
import com.sk89q.intake.context.CommandLocals
import com.sk89q.intake.parametric.annotation.Optional
import com.sk89q.intake.{Command, CommandMapping, Require}

import scala.collection.JavaConversions._

@Plugin(id = "help")
@Singleton
class CommandHelp @Inject() (commandManager: CommandManager) {

  def getBestAlias(aliases: Array[String]): String = {
    for (alias <- aliases) {
      if (!alias.contains(":")) {
        return alias
      }
    }

    aliases(0)
  }

  @Command(aliases = Array("help:help", "help"), desc = "View a list of commands")
  @Require(Array("help"))
  def helpCommand(response: Response, @Optional(Array("1")) page: Integer, locals: CommandLocals) = {
    val set = commandManager.getDispatcher.getCommands
    val commands = set.toArray(new Array[CommandMapping](set.size))
    val results = commands
      .filter(m => m.getCallable.testPermission(locals))
      .map(m => getBestAlias(m.getAllAliases))
      .sorted
    val paginator = new Paginator(results.toList, 15)
    val pageResult = paginator.getPage(page)
    val output = Joiner.on(", ").join(pageResult.results)
    response.respond(style().append(style(Style.BOLD).append(s"[pg. ${pageResult.page}/${paginator.getPageCount}] ")).append(output))
  }

}
