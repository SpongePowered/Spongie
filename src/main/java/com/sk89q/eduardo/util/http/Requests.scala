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

package com.sk89q.eduardo.util.http

import java.io.{ByteArrayInputStream, InputStream}
import java.net.URI

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.google.common.io.ByteStreams
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods._
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.{BasicCookieStore, DefaultRedirectStrategy, HttpClientBuilder}
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.message.BasicNameValuePair
import org.apache.http.{HttpEntity, NameValuePair}

import scala.collection.JavaConverters._
import scala.collection.Map

/**
 * A simple API to access HTTP resources, modeled after Python's Requests
 * module.
 */
object Requests {
  val defaultMapper = new ObjectMapper() with ScalaObjectMapper
  defaultMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  defaultMapper.registerModule(DefaultScalaModule)

  val MAX_REDIRECTS = 5

  /**
   * Make an HTTP request.
   *
   * @param method The method
   * @param url The URL of the resource
   * @param params The query parameters
   * @param data The body data
   * @param headers HTTP request headers
   * @param cookies Cookies
   * @param timeout The connection timeout, in seconds
   * @param allowRedirects Whether redirects should be followed automatically
   * @param stream Whether streaming mode should be used
   * @return The response
   */
  def request(method: String,
              url: String,
              params: List[(String, String)] = Nil,
              data: HttpEntity = null,
              headers: Map[String, String] = Map.empty,
              cookies: Map[String, String] = Map.empty,
              timeout: Double = 5.0,
              allowRedirects: Boolean = true,
              stream: Boolean = false): Response = {

    val builder = HttpClientBuilder.create()

    val reqConf = RequestConfig.custom()
      .setSocketTimeout((timeout * 1000).asInstanceOf[Int])
      .setConnectTimeout((timeout * 1000).asInstanceOf[Int])
      .setRedirectsEnabled(allowRedirects)
      .setMaxRedirects(MAX_REDIRECTS)
      .build()
    builder.setDefaultRequestConfig(reqConf)

    val uriBuilder = new URIBuilder(url)
    uriBuilder.addParameters(
      params.map(t => new BasicNameValuePair(t._1, t._2))
        .asInstanceOf[List[NameValuePair]].asJava)
    val uri = uriBuilder.build

    val request = method match {
      case "GET" => new HttpGet(uri)
      case "POST" => new HttpPost(uri)
      case "HEAD" => new HttpHead(uri)
      case "PUT" => new HttpPut(uri)
      case "DELETE" => new HttpDelete(uri)
      case "PATCH" => new HttpPatch(uri)
      case "OPTIONS" => new HttpOptions(uri)
      case "TRACE" => new HttpTrace(uri)
      case _ => new HttpRequest(method, uri)
    }

    request match {
      case base: HttpEntityEnclosingRequestBase =>
        base.setEntity(data)
      case _ =>
    }

    headers.map(t => { request.addHeader(t._1, t._2) })

    if (cookies.nonEmpty) {
      val cookieStore = new BasicCookieStore()
      cookies.map(t => { cookieStore.addCookie(new BasicClientCookie(t._1, t._2))})
      builder.setDefaultCookieStore(cookieStore)
    }

    if (allowRedirects) {
      builder.setRedirectStrategy(AutoRedirectStrategy)
    }

    val response = new Response(builder.build().execute(request))
    if (!stream) {
      response.bytes() // Read from stream and close
    }

    response
  }

  private object AutoRedirectStrategy extends DefaultRedirectStrategy {
    override def isRedirectable(method: String): Boolean = {
      true
    }
  }
}

/**
 * The response.
 *
 * @param response The HTTP client response object
 */
class Response(response: CloseableHttpResponse) {
  var content: Array[Byte] = null

  /**
   * Get the input stream for reading.
   *
   * <p>This is only available if {@code stream} was set to {@code true}.
   * Once the input stream or respone is closed, this method can no longer
   * be used.</p>
   *
   * @return The input stream
   */
  def inputStream: InputStream = {
    if (content != null) {
      throw new IllegalArgumentException("The input stream has been read")
    }
    new InputStreamWrapper(response, response.getEntity match {
      case null => new ByteArrayInputStream(new Array[Byte](0))
      case ent => ent.getContent
    })
  }

  def statusCode: Int = response.getStatusLine.getStatusCode

  def reason: String = response.getStatusLine.getReasonPhrase

  def headers: Map[String, String] = response.getAllHeaders.map(t => t.getName -> t.getValue).toMap

  def bytes(): Array[Byte] = {
    if (content == null) {
      try {
        content = ByteStreams.toByteArray(inputStream)
      } finally {
        if (content == null) content = new Array(0)
        response.close()
      }
    }

    content
  }

  def text(): String = new String(bytes(), "UTF-8")

  def json[T](classType: Class[T]): T = Requests.defaultMapper.readValue(text(), classType)

  def close(): Unit = response.close()
}

private class InputStreamWrapper(response: CloseableHttpResponse, in: InputStream) extends InputStream {
  override def read(): Int = in.read()

  override def close() = {
    try {
      in.close()
    } finally {
      response.close()
    }
  }
}

private class HttpRequest(method: String, uri: URI) extends HttpRequestBase {
  setURI(uri)

  override def getMethod: String = method
}