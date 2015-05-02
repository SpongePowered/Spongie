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

package com.sk89q.eduardo.plugin.bing

import java.util.Base64

import com.google.inject.{Inject, Singleton}
import com.sk89q.eduardo.model.response.Response
import com.sk89q.eduardo.service.plugin.Plugin
import com.sk89q.eduardo.service.throttle.RateLimit
import com.sk89q.eduardo.util.APIException
import com.sk89q.eduardo.util.config.Config
import com.sk89q.eduardo.util.http.Requests
import com.sk89q.intake.parametric.annotation.Text
import com.sk89q.intake.{Command, Require}

@Plugin(id = "bing-search")
class Search @Inject() (config: Config) {

  val appId = config.stringAt("bing-search.app-id", "")

  @Command(aliases = Array("bing:search", "bing", "b"), desc = "Search Bing for a query")
  @Require(Array("bing.search"))
  @RateLimit(weight = 2)
  def searchCommand(r: Response, @Text query: String) {
    search(query) match {
      case Some(item) => r.broadcast(item.Url + " -- " + item.Title + " -- " + item.Description)
      case None => r.respond(s"No results found for '$query'")
    }
  }

  def search(query: String): Option[Result] = {
    val response = Requests.request("GET", "https://api.datamarket.azure.com/Bing/Search/Web",
      params = List(
        ("Query", "\'" + query + "\'"),
        ("$format", "json"),
        ("$top", "1")),
      headers = Map(
        ("Authorization", "Basic " + Base64.getUrlEncoder.encodeToString((appId.get() + ":" + appId.get()).getBytes)))
    )

    response.statusCode match {
      case 200 => response.json(classOf[Data]).d.results.headOption;
      case _ => throw new APIException("Failed to search Bing", response.text())
    }
  }

}
