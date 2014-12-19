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
 * ANY WARRANTY without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.eduardo.plugin.github

import java.io.IOException
import java.util.function.Supplier
import java.util.regex.{Matcher, Pattern}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.{HashMultimap, Multimap}
import com.google.common.io.ByteStreams
import com.google.inject.{Inject, Singleton}
import com.sk89q.eduardo.event.ConfigureEvent
import com.sk89q.eduardo.event.http.ConfigureRouteEvent
import com.sk89q.eduardo.model.context.Users.mangleName
import com.sk89q.eduardo.model.response.GenericBroadcast
import com.sk89q.eduardo.service.event.Subscribe
import com.sk89q.eduardo.service.http.status.{BadRequestError, InternalServerError}
import com.sk89q.eduardo.service.plugin.Plugin
import com.sk89q.eduardo.service.shortener.URLShortener
import com.sk89q.eduardo.util.GitUtils.{shortenHash, shortenMessage, shortenRef}
import com.sk89q.eduardo.util.config.{Config, ConfigObject}
import com.sk89q.eduardo.util.formatting.Message.{style, styled}
import com.sk89q.eduardo.util.formatting.{Message, Style}
import com.sk89q.eduardo.util.text.English.plural
import org.apache.commons.codec.binary.Hex
import org.slf4j.{Logger, LoggerFactory}
import spark.{Request, Response, Route, Spark}

import scala.collection.JavaConversions._
import scala.util.control.Breaks._

@Plugin(id = "github-webhooks")
class WebHooks @Inject() (config: Config, mapper: ObjectMapper,
                                   shortener: URLShortener, broadcast: GenericBroadcast) {

  val maxCommitsToAnnounce: Supplier[Integer] = config.intAt("github-webhook.max-commits-to-announce", 5)
  val sigPattern: Pattern = Pattern.compile("^([^=]+)=(.+)$")
  val hmacSha1: String = "HmacSHA1"

  val log: Logger = LoggerFactory.getLogger(classOf[WebHooks])
  val targets: Multimap[String, String] = HashMultimap.create[String, String]
  val c = config.getConfig("github-webhook")

  @Subscribe
  def onConfigure(event: ConfigureEvent): Unit = {
    targets.clear()

    for ((project: String, c) <- c.getMap("repositories", classOf[ConfigObject])) {
      targets.putAll(project.toLowerCase, c.getList("targets", classOf[String]))
    }
  }

  @Subscribe
  def onConfigureRoute(event: ConfigureRouteEvent) {
    Spark.post("/github/webhook/", new Route {
      override def handle(request: Request, response: Response) = {
        val signature = request.headers("X-Hub-Signature")
        val eventType = request.headers("X-GitHub-Event")
        val data = ByteStreams.toByteArray(request.raw().getInputStream)

        if (signature != null && isSignatureCorrect(signature, data)) {
          try {
            handlePayload(eventType, data)
            "OK"
          } catch {
            case e: IOException =>
              log.warn("Failed to process GitHub webhook", e)
              throw new InternalServerError("Encountered an error processing the payload", e)
          }
        } else {
          throw new BadRequestError("Invalid signature!")
        }
      }
    })
  }

  def isSignatureCorrect(signature: String, content: Array[Byte]): Boolean = {
    val matcher: Matcher = sigPattern.matcher(signature)

    if (matcher.matches) {
      matcher.group(1) match {
        case "sha1" =>
          val expected = c.getString("secret-key", "")
          val signingKey = new SecretKeySpec(expected.getBytes, hmacSha1)
          val mac = Mac.getInstance(hmacSha1)
          mac.init(signingKey)
          Hex.encodeHexString(mac.doFinal(content)).equalsIgnoreCase(matcher.group(2))
        case algorithm =>
          log.warn(s"Received GitHub web hook with signature algorithm of '$algorithm'")
          false
      }
    } else {
      log.warn("Received GitHub web hook hit with invalid X-Hub-Signature header format")
      false
    }
  }

  def handlePayload(event: String, data: Array[Byte]) {
    event match {
      case "push" => handlePush(mapper.readValue(data, classOf[PushEvent]))
      case "pull_request" => handlePullRequest(mapper.readValue(data, classOf[PullRequestEvent]))
      case _ =>
    }
  }

  def handlePush(event: PushEvent) {
    log.info(s"Got GitHub push event for ${event.repository.fullName}")

    val pusher = mangleName(event.pusher.name)
    val url = shortener.shorten(event.compare)

    val repoName = styled() + "[" + style(Style.BOLD, style(Style.DARK_GREEN, s"${event.repository.name}")) + "]"

    broadcast(
      event.repository.fullName,
      styled() +
        repoName + " " + s"$pusher pushed " + style(Style.BOLD, event.commits.size) + s" commit${plural(event.commits.size)} to ${shortenRef(event.ref)}: $url")


    for ((commit, i) <- event.commits.zipWithIndex) {
      if (i >= maxCommitsToAnnounce.get()) {
        broadcast(event.repository.fullName, styled() + s"(for more, visit $url)")
        break()
      } else {
        broadcast(event.repository.fullName,
          style(Style.BOLD, s"${event.repository.name}/${shortenRef(event.ref)}") +
            s" ${shortenHash(commit.id)}: ${shortenMessage(commit.message)} (by ${mangleName(commit.author.name)})")
      }
    }
  }

  def handlePullRequest(event: PullRequestEvent) = {
    log.info(s"Got GitHub pull request event for ${event.repository.fullName}")

    val sender = mangleName(event.sender.login)
    val url = shortener.shorten(event.pullRequest.html_url)
    val repoName = styled() + "[" + style(Style.BOLD, style(Style.DARK_GREEN, s"${event.repository.name}")) + "]"

    broadcast(
      event.repository.fullName,
      styled() +
        repoName + " " + s"$sender ${event.action} PR #" +
        style(Style.BOLD, event.number) +
        s": ${event.pullRequest.title} ($url)")
  }

  private def broadcast(project: String, message: Message) {
    targets.get(project.toLowerCase).map(t => {
      broadcast.broadcast(t, message)
    })
  }
}