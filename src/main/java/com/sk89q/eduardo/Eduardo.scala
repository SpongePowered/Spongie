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

import java.io.File

import com.beust.jcommander.JCommander
import com.google.inject.{Guice, Inject, Injector, Singleton}
import com.sk89q.eduardo.event.{ConfigureEvent, StartupEvent}
import com.sk89q.eduardo.service.plugin.{LoadablePlugin, LoaderException, PluginLoader}
import com.sk89q.eduardo.service.command.CommandManager
import com.sk89q.eduardo.service.event.EventBus
import com.sk89q.eduardo.util.config.{YamlConfig, ConfigFile}
import com.sk89q.eduardo.util.logging.SimpleLogFormatter
import org.slf4j.LoggerFactory

@Singleton object Eduardo {
  val log = LoggerFactory.getLogger(classOf[Eduardo])

  def main(args: Array[String]) {
    SimpleLogFormatter.configureGlobalLogger()

    log.info("Eduardo IRC bot")
    log.info("(c) sk89q <http://www.sk89q.com>")

    val opt = new CommandOptions
    new JCommander(opt, args:_*)

    if (opt.config == null) {
      log.error("No configuration file was specified")
      System.exit(1)
      return
    }

    val configFile = new File(opt.config)
    val config = YamlConfig.load(configFile)
    config.load()

    log.info(s"Searching loaded libraries for known plugins...")
    val loader = new PluginLoader(config)
    loader.scanClassPath()
    val loadable = loader.getLoadable

    val injector = Guice.createInjector(new DefaultModule(config, loadable))

    try {
      injector.getInstance(classOf[Eduardo]).load(loadable)
    } catch {
      case e: LoaderException => log.error("Failed to load the application", e)
      case e: Exception => log.error("Failed to load the application due to an unexpected error", e)
    }
  }
}

@Singleton
class Eduardo @Inject() (injector: Injector, config: ConfigFile, bus: EventBus, commands: CommandManager) {
  val log = LoggerFactory.getLogger(classOf[Eduardo])

  @throws(classOf[LoaderException])
  def load(loadable: List[LoadablePlugin]) {
    try {
      for (plugin <- loadable) {
        log.info(s"Loading plugin ${plugin.plugin.id}...")
        val instance = injector.getInstance(plugin.pluginClass)
        bus.register(instance)
        commands.register(instance)
      }
    }
    catch {
      case e: ClassNotFoundException => throw new LoaderException("Failed to load one or more plugins", e)
    }

    log.info("Plugins loaded; initializing...")

    bus.post(new ConfigureEvent)
    bus.post(new StartupEvent)

    config.save()

    log.info("Initialization complete.")
  }

}