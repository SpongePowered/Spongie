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

package com.sk89q.eduardo.plugin.minecraft

import java.net.URLEncoder

import com.sk89q.eduardo.model.response.Response
import com.sk89q.eduardo.service.plugin.Plugin
import com.sk89q.eduardo.util.APIException
import com.sk89q.eduardo.util.http.Requests
import com.sk89q.intake.{Command, Require}
import org.apache.http.HttpStatus

@Plugin(id = "minecraft-accounts")
class ProfileLookup {

  @Command(aliases = Array("minecraft:uuid", "mcuuid"), desc = "Lookup a Minecraft user's UUID")
  @Require(Array("minecraft.uuid"))
  def uuidCommand(response: Response, name: String) = {
    getIdentity(name) match {
      case Some(identity) => response.respond(s"${identity.name} -> ${identity.idWithDashes}")
      case None => response.respond(s"Couldn't find '$name'")
    }
  }

  @Command(aliases = Array("minecraft:names", "mcnames"), desc = "Lookup a Minecraft user's names")
  @Require(Array("minecraft.names"))
  def namesCommand(response: Response, uuid: String): Unit = {
    var target = uuid.replace("-", "").toLowerCase

    if (!target.matches("^[a-f0-9]{32}$")) {
      getIdentity(target) match {
        case Some(identity) => target = identity.id
        case None =>
          response.respond(s"Couldn't find a player with name '$target'")
          return
      }
    }

    getNames(target) match {
      case Some(names) => response.respond(s"$target's name history: " + names.take(10).mkString(", "))
      case None => response.respond(s"Couldn't find a player with UUID '$target'")
    }
  }

  def getIdentity(name: String): Option[Identity] = {
    val response = Requests.request("GET", "https://api.mojang.com/users/profiles/minecraft/" +
      URLEncoder.encode(name, "UTF-8"))
    response.statusCode match {
      case HttpStatus.SC_OK => Some(response.json(classOf[Identity]))
      case HttpStatus.SC_NO_CONTENT => None
      case _ => throw new APIException("Some error occurred", response.text())
    }
  }

  def getNames(uuid: String): Option[List[String]] = {
    val response = Requests.request("GET", "https://api.mojang.com/user/profiles/" +
      URLEncoder.encode(uuid, "UTF-8") + "/names")
    response.statusCode match {
      case HttpStatus.SC_OK => Some(response.json(classOf[List[String]]))
      case HttpStatus.SC_NO_CONTENT => None
      case _ => throw new APIException("Some error occurred", response.text())
    }
  }

}
