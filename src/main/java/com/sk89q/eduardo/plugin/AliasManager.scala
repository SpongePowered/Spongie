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

import java.util

import com.google.inject.{Inject, Singleton}
import com.sk89q.eduardo.event.{CommandEvent, CommandQueryEvent}
import com.sk89q.eduardo.model.context.Context
import com.sk89q.eduardo.model.response.Response
import com.sk89q.eduardo.service.event.EventHandler.Priority
import com.sk89q.eduardo.service.event.{EventBus, Subscribe}
import com.sk89q.eduardo.service.plugin.Plugin
import com.sk89q.eduardo.util.config.ConfigFile
import com.sk89q.intake.context.CommandContext
import com.sk89q.intake.parametric.annotation.Text
import com.sk89q.intake.{Command, Require}
import org.slf4j.{Logger, LoggerFactory}

@Plugin(id = "alias")
@Singleton
class AliasManager @Inject() (config: ConfigFile, eventBus: EventBus) {

  val log: Logger = LoggerFactory.getLogger(classOf[AliasManager])
  val aliases = config.configAt("alias.aliases")

  /**
   * Look up an alias.
   *
   * @param alias The alias
   * @param context The content
   * @return The command
   */
  def resolveAlias(alias: String, context: Context): Option[String] = {
    aliases.get.toObject.get(alias) match {
      case null => None
      case c => Some(String.valueOf(c))
    }
  }


  @Command(aliases = Array("alias:create", "alias"), desc = "Create an alias")
  @Require(Array("alias.create"))
  def createCommand(context: Context, response: Response, alias: String, @Text command: String) = {
    aliases.get.toObject.put(alias, command)
    response.respond(s"Created the alias $alias => $command")
    config.save()
  }

  @Command(aliases = Array("alias:remove", "removealias"), desc = "Remove an alias")
  @Require(Array("alias.remove"))
  def removeCommand(context: Context, response: Response, alias: String, @Text command: String) = {
    val prev = aliases.get.toObject.remove(alias)
    if (prev == null) {
      response.respond(s"There was no alias defined for '$alias'")
    } else {
      response.respond(s"Removed the alias $alias => $prev")
    }
    config.save()
  }

  @Subscribe(priority = Priority.LATE, ignoreCancelled = true)
  def onCommandEvent(event: CommandEvent) {
    val newDepth = event.getDepth + 1
    val split = CommandContext.split(event.getArguments)
    var alias = split(0)
    var query = false
    if (alias.length > 1 && alias.endsWith("?")) {
      alias = alias.substring(0, alias.length - 1)
      query = true
    }

    resolveAlias(alias, event.getContext) match {
      case Some(command) =>
        event.setCancelled(true)
        if (query) {
          event.getResponse.respond("Alias set to: " + command)
        } else {
          val arguments = util.Arrays.copyOfRange(split, 1, split.length).mkString(" ")
          val target = command.replace("$*", arguments)
          val commandEvent = new CommandEvent(event.getContext, target, event.getResponse, newDepth)
          eventBus.post(commandEvent)
        }
      case None =>
    }
  }

  @Subscribe(priority = Priority.LATE, ignoreCancelled = true)
  def onCommandQueryEvent(event: CommandQueryEvent) {
    if (event.getDescription == null) {
      resolveAlias(event.getCommand, event.getContext) match {
        case Some(command) =>
          event.setDescription(s"Is an alias to '$command'")
        case None =>
      }
    }
  }

}