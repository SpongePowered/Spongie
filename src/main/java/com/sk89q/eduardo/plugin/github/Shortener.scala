package com.sk89q.eduardo.plugin.github

import java.net.{MalformedURLException, URL}
import java.util

import com.github.kevinsawicki.http.HttpRequest
import com.sk89q.eduardo.service.plugin.Plugin
import com.sk89q.eduardo.service.shortener.URLShortener
import org.slf4j.LoggerFactory

@Plugin(id = "github-shortener")
class Shortener extends URLShortener {

  final val targetUrl = "http://git.io"
  val log = LoggerFactory.getLogger(classOf[Shortener])

  def shorten(url: URL) : URL = {
    try {
      val data = new util.HashMap[String, String]
      data.put("url", url.toExternalForm)
      new URL(HttpRequest.post(targetUrl).form(data).header("Location"))
    } catch {
      case e: MalformedURLException =>
        log.warn("git.io service returned an invalid shortened URL")
        url
    }
  }
}
