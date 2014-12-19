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

package com.sk89q.eduardo.service.plugin

import com.google.common.reflect.ClassPath
import com.google.common.reflect.ClassPath.ClassInfo
import com.sk89q.eduardo.util.config.Config
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

@throws[LoaderException]
class PluginLoader(config: Config) {

  private val log = LoggerFactory.getLogger(classOf[PluginLoader])

  val exclusions = List("com.oracle.", "com.sun.", "java.", "javax.", "jdk.", "sun.", ".scala")
  val mapping = new mutable.HashMap[String, String]()

  def getLoadable: List[LoadablePlugin] = {
    val results = ListBuffer[LoadablePlugin]()
    val provided = new mutable.HashSet[Class[_]]()

    for (id <- config.getList("plugins.enabled", classOf[String])) {
      mapping.lift(id) match {
        case Some(className) =>
          try {
            val pluginClass = Class.forName(className)

            // Identify services
            val services = ListBuffer[Class[_]]()
            val plugin = pluginClass.getAnnotation(classOf[Plugin])
            if (plugin != null) {
              for (serviceClass <- plugin.provides()) {
                if (provided.contains(serviceClass)) {
                  throw new LoaderException(s"Cannot register the plugin '$id' for " +
                    s"service '${serviceClass.getName}' because it has already been registered")
                } else {
                  services += serviceClass
                  provided += serviceClass
                }
              }
            }

            results += new LoadablePlugin(pluginClass.getAnnotation(classOf[Plugin]), pluginClass, services.toList)
          } catch {
            case t: Throwable =>
              throw new LoaderException(s"Failed to load the plugin '$id'", t)
          }

        case None =>
          throw new LoaderException(s"Can't find the plugin '$id'\n\n" +
            s"These are the known plugins:\n" + mapping.map(e => s"\t${e._1} (${e._2})").mkString(",\n") + "\n")
      }
    }

    results.toList
  }

  def scanClassPath() = {
    val classPath = ClassPath.from(getClass.getClassLoader)
    for (info: ClassInfo <- classPath.getTopLevelClasses) {
      if (shouldScan(info.getName)) {
        try {
          val loadedClass = info.load()

          val plugin = loadedClass.getAnnotation(classOf[Plugin])
          if (plugin != null) {
            mapping.put(plugin.id(), loadedClass.getCanonicalName)
          }
        } catch {
          case _: Throwable =>
        }
      }
    }
  }

  private def shouldScan(name: String): Boolean = {
    for (prefix <- exclusions) {
      if (name.startsWith(prefix)) {
        return false
      }
    }
    true
  }

}

case class LoadablePlugin(plugin: Plugin, pluginClass: Class[_], serviceClasses: List[Class[_]])