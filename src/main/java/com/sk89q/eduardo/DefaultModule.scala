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

package com.sk89q.eduardo

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.google.inject.{Scopes, AbstractModule, Provides, Singleton}
import com.sk89q.eduardo.service.event.EventBus
import com.sk89q.eduardo.service.plugin.{Plugin, LoadablePlugin}
import com.sk89q.eduardo.util.config.{Config, ConfigFile}
import org.slf4j.LoggerFactory

class DefaultModule(config: ConfigFile, loadable: List[LoadablePlugin]) extends AbstractModule {

  val log = LoggerFactory.getLogger(classOf[DefaultModule])

  protected def configure() {
    bindScope(classOf[Plugin], Scopes.SINGLETON)
    bind(classOf[EventBus]).toInstance(new EventBus)
    bind(classOf[Config]).toInstance(config)
    bind(classOf[ConfigFile]).toInstance(config)

    try {
      for (plugin <- loadable) {
        val id = plugin.plugin.id()
        for (service <- plugin.serviceClasses) {
          log.info(s"Binding ${service.getName} -> $id")
          bind(service.asInstanceOf[Class[AnyRef]])
            .to(plugin.pluginClass.asInstanceOf[Class[AnyRef]])
            .in(classOf[Singleton])
        }
      }
    } catch {
      case e: ClassNotFoundException => throw new RuntimeException("Invalid service definition", e)
    }
  }

  @Provides
  @Singleton
  def provideObjectMapper: ObjectMapper = {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

}