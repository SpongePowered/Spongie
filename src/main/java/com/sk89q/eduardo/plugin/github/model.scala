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

package com.sk89q.eduardo.plugin.github

import java.net.URL

import com.fasterxml.jackson.annotation.JsonProperty

case class Identity(name: String, email: String, username: String)
case class User(login: String, url: URL)
case class Commit(id: String, message: String, url: String, author: Identity, committer: Identity,
                  added: List[String], removed: List[String], modified: List[String])
case class Repository(name: String, @JsonProperty("full_name") fullName: String, html_url: URL, owner: Identity)
case class PullRequest(html_url: URL, id: Int, title: String, body: String, user: User)

case class PushEvent(ref: String, compare: URL, commits: List[Commit], repository: Repository, pusher: Identity)
case class PullRequestEvent(action: String, number: Int,
                            @JsonProperty("pull_request") pullRequest: PullRequest,
                            repository: Repository,
                            sender: User)
case class CreateEvent(ref: String, ref_type: String, repository: Repository, sender: User)
case class DeleteEvent(ref: String, ref_type: String, repository: Repository, sender: User)