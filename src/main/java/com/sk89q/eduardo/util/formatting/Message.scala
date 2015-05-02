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

package com.sk89q.eduardo.util.formatting

object Message {

  def styled(): Message = {
    new Message()
  }

  def style(style: Style, content: Any): Message = {
    new Message(style).append(content)
  }
}

class Message(styles: Style*) extends StyledFragment(styles.toArray:_*) {

  def +(message: Any): Message = {
    if (getStyle.isUnstyled) {
      append(message)
    } else {
      new Message().append(this).append(message)
    }
  }

  override def append(fragment: StyledFragment): Message = {
    super.append(fragment)
    this
  }

  override def append(fragment: Any): Message = {
    fragment match {
      case f: StyledFragment => super.append(f)
      case _ => super.append(fragment)
    }
    this
  }

}
