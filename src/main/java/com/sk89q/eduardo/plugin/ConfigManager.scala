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
import com.sk89q.eduardo.event.ConfigureEvent
import com.sk89q.eduardo.model.response.Response
import com.sk89q.eduardo.service.event.EventBus
import com.sk89q.eduardo.service.plugin.Plugin
import com.sk89q.intake.{Command, Require}

@Plugin(id = "config")
@Singleton
class ConfigManager @Inject() (eventBus: EventBus) {

  @Command(aliases = Array("config:reload", "reload"), desc = "Reload configuration")
  @Require(Array("config.reload"))
  def reloadCommand(response: Response) = {
    eventBus.post(new ConfigureEvent)
    response.respond("Configuration reloaded!")
  }

}
