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

package com.sk89q.eduardo.plugin.music

import com.google.inject.{Inject, Singleton}
import com.sk89q.eduardo.model.response.Response
import com.sk89q.eduardo.service.plugin.Plugin
import com.sk89q.eduardo.service.shortener.URLShortener
import com.sk89q.eduardo.service.throttle.RateLimit
import com.sk89q.eduardo.util.APIException
import com.sk89q.eduardo.util.config.Config
import com.sk89q.eduardo.util.http.Requests
import com.sk89q.intake.parametric.annotation.Text
import com.sk89q.intake.{Command, Require}
import org.apache.http.HttpStatus

@Plugin(id = "music")
class MusicLookup @Inject() (config: Config, shorterner: URLShortener) {

  val apiKey = config.stringAt("echonest.api-key", "")

  @Command(aliases = Array("music:artist", "artist"), desc = "Lookup info about a music artist")
  @Require(Array("music.artist"))
  @RateLimit(weight = 2)
  def artistCommand(response: Response, @Text query: String) = {
    findArtists(query) match {
      case Nil => response.respond(s"No artists matched '$query'")
      case artists =>
        val message = artists.map(artist => {
          val term = artist.terms match {
            case Nil => "?"
            case null => "?"
            case l => l.sortWith((a, b) => a.weight > b.weight)(0).name
          }
          val url = artist.urls.lastfm match {
            case null => ""
            case u => " " + shorterner.shorten(u)
          }
          s"${artist.name} ($term)$url"
        }).take(5).mkString(" -- ")
        response.respond(message)
    }
  }

  def findArtists(q: String): List[Artist] = {
    val response = Requests.request("GET", "http://developer.echonest.com/api/v4/artist/search",
      params = List(
        ("api_key", apiKey.get()),
        ("format", "json"),
        ("name", q),
        ("rank_type", "familiarity"),
        ("bucket", "terms"),
        ("bucket", "urls")
      ))
    response.statusCode match {
      case HttpStatus.SC_OK =>
        response.json(classOf[ArtistResponse]).response.artists
      case _ => throw new APIException("Some error occurred", response.text())
    }
  }

}
