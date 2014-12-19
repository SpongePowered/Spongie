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

import java.net.{MalformedURLException, URL}

import com.google.inject.Inject
import com.sk89q.eduardo.TestSingleton
import com.sk89q.eduardo.model.response.Response
import com.sk89q.eduardo.service.auth.Subject
import com.sk89q.eduardo.service.plugin.Plugin
import com.sk89q.eduardo.service.shortener.URLShortener
import com.sk89q.eduardo.service.throttle.RateLimit
import com.sk89q.intake.parametric.annotation.Text
import com.sk89q.intake.{Command, CommandException, Require}

@Plugin(id = "shortener")
class Shortener @Inject() (shortener: URLShortener) {

  @Command(aliases = Array("shorten:shorten", "shorten", "short"), desc = "Shorten a URL")
  @Require(Array("shortener.shorten"))
  @RateLimit(weight = 2)
  def shorten(subject: Subject, response: Response, @Text url: String) {
    try {
      response.respond("Shortened: " + shortener.shorten(new URL(url)))
    } catch {
      case e: MalformedURLException => throw new CommandException("Invalid URL!")
    }
  }

}