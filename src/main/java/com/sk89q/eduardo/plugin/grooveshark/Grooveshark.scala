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

package com.sk89q.eduardo.plugin.grooveshark

import java.net.URLEncoder

import com.fasterxml.jackson.databind.JsonMappingException
import com.google.inject.{Inject, Singleton}
import com.sk89q.eduardo.model.response.Response
import com.sk89q.eduardo.service.plugin.Plugin
import com.sk89q.eduardo.service.throttle.RateLimit
import com.sk89q.eduardo.util.APIException
import com.sk89q.eduardo.util.config.Config
import com.sk89q.eduardo.util.http.Requests
import com.sk89q.intake.parametric.annotation.Text
import com.sk89q.intake.{Command, Require}
import org.apache.http.HttpStatus

@Plugin(id = "grooveshark")
@Singleton
class Grooveshark @Inject() (config: Config)  {

  val apiKey = config.stringAt("grooveshark.api-key", "")

  @Command(aliases = Array("grooveshark:search", "grooveshark", "gs"), desc = "Find a song on Grooveshark")
  @Require(Array("grooveshark.search"))
  @RateLimit(weight = 2)
  def findCommand(r: Response, @Text query: String) {
    findFirstSong(query) match {
      case Some(track) => r.broadcast(track.url + " -- " + track.artist + " - " + track.title)
      case None => r.respond(s"No results found for '$query'")
    }
  }

  def findFirstSong(q: String): Option[Track] = {
    val response = Requests.request("GET", "http://tinysong.com/b/" + URLEncoder.encode(q, "UTF-8"),
      params = List(("format", "json"), ("key", apiKey.get())))
    response.statusCode match {
      case HttpStatus.SC_OK => try {
        Some(response.json(classOf[Track]))
      } catch {
        case e: JsonMappingException => None // TinySong returns a list when there are no results
      }
      case _ => throw new APIException("Some error occurred", response.text())
    }
  }

}
