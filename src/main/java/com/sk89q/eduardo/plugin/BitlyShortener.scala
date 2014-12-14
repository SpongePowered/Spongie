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

import com.google.inject.{Inject, Singleton}
import com.rosaloves.bitlyj.Bitly.Provider
import com.rosaloves.bitlyj.{Bitly, BitlyException, ShortenedUrl}
import com.sk89q.eduardo.event.ConfigureEvent
import com.sk89q.eduardo.service.event.Subscribe
import com.sk89q.eduardo.service.plugin.{Plugin, Provides}
import com.sk89q.eduardo.service.shortener.URLShortener
import com.sk89q.eduardo.util.config.Config
import org.slf4j.LoggerFactory

@Plugin(id = "bitly")
@Provides(Array(classOf[URLShortener]))
@Singleton
class BitlyShortener @Inject() (config: Config) extends URLShortener {

  val log = LoggerFactory.getLogger(classOf[BitlyShortener])
  val user = config.stringAt("bitly.user", "")
  val apiKey = config.stringAt("bitly.api-key", "")
  var client: Provider = null

  @Subscribe
  def onConfigure(event: ConfigureEvent) {
    client = Bitly.as(user.get(), apiKey.get())
  }

  def shorten(url: URL): URL = {
    try {
      val shortened: ShortenedUrl = client.call(Bitly.shorten(url.toExternalForm))
      new URL(shortened.getShortUrl)
    }
    catch {
      case e: BitlyException =>
        log.warn("Bitly shortening has failed", e)
        url
      case e: MalformedURLException =>
        log.warn("Bitly service returned an invalid shortened URL", e)
        url
    }
  }
}