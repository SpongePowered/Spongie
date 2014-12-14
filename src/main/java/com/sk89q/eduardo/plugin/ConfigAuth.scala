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

import com.google.inject.{Inject, Singleton}
import com.sk89q.eduardo.event.ConfigureEvent
import com.sk89q.eduardo.model.context.{Context, Mode}
import com.sk89q.eduardo.service.auth.{AuthService, ContextMatch, Subject}
import com.sk89q.eduardo.service.auth.policy.{MultiMapPolicy, Policy}
import com.sk89q.eduardo.service.event.Subscribe
import com.sk89q.eduardo.service.plugin.{Plugin, Provides}
import com.sk89q.eduardo.util.config.Config

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

@Plugin(id = "config-auth")
@Provides(Array(classOf[AuthService]))
@Singleton
class ConfigAuth @Inject() (config: Config) extends AuthService {

  var policy: Policy[Context] = new MultiMapPolicy[Context]

  @Subscribe
  def onConfigure(event: ConfigureEvent) {
    val p = new MultiMapPolicy[Context]

    for (policy <- config.getList("config-perms.policy", classOf[Config])) {
      val m: ContextMatch = new ContextMatch
      m.matchAllUsers(policy.getList("users", classOf[String]))
      m.matchAllChannels(policy.getList("rooms", classOf[String]))
      m.matchAllModes(policy.getList("modes", classOf[String]).asScala.map(mode => Mode.valueOf(mode)))

      for (permission <- policy.getList("grant", classOf[String])) {
        p.grant(permission, m)
      }

      for (permission <- policy.getList("deny", classOf[String])) {
        p.deny(permission, m)
      }
    }

    this.policy = p
  }

  def login(context: Context): Subject = {
    new ConfigAuth#ConfigSubject(context)
  }

  private class ConfigSubject(context: Context) extends Subject {
    def testPermission(permission: String): Boolean = {
      policy.testPermission(permission, context)
    }
  }

}