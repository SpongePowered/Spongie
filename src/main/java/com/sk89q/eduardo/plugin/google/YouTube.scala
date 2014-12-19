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

package com.sk89q.eduardo.plugin.google

import com.google.inject.Inject
import com.sk89q.eduardo.model.response.Response
import com.sk89q.eduardo.service.plugin.Plugin
import com.sk89q.eduardo.util.APIException
import com.sk89q.eduardo.util.config.Config
import com.sk89q.eduardo.util.http.Requests
import com.sk89q.intake.parametric.annotation.Text
import com.sk89q.intake.{Command, Require}

@Plugin(id = "youtube")
class YouTube @Inject() (config: Config) {

  val apiKey = config.stringAt("youtube.api-key", "")

  @Command(aliases = Array("youtube:search", "yt", "youtube"), desc = "Search YouTube for a video")
  @Require(Array("youtube.search"))
  def youtubeCommand(r: Response, @Text query: String): Unit = {
    findFirstVideo(query) match {
      case Some(result) => r.broadcast(
        "https://youtu.be/" + result.id.videoId + " -- " + result.snippet.title)
      case None => r.respond(s"No video found for '$query'")
    }
  }

  /**
   * Get the first video result for a YouTube search query.
   *
   * @param query The query
   * @return The first video
   */
  def findFirstVideo(query: String): Option[Video] = {
    val response = Requests.request("GET", "https://www.googleapis.com/youtube/v3/search",
      params = List(
        ("key", apiKey.get()),
        ("part", "id,snippet"),
        ("q", query),
        ("maxResults", "1"))
      )

    response.statusCode match {
      case 200 => response.json(classOf[VideoList]).items.lift(0)
      case _ => throw new APIException("Failed to search YouTube", response.text())
    }
  }

}