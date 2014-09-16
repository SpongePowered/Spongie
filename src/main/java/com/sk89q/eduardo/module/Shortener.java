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

package com.sk89q.eduardo.module;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.auth.Subject;
import com.sk89q.eduardo.helper.AutoRegister;
import com.sk89q.eduardo.helper.Response;
import com.sk89q.eduardo.helper.command.CommandManager;
import com.sk89q.eduardo.helper.throttle.RateLimit;
import com.sk89q.eduardo.helper.shortener.URLShortener;
import com.sk89q.intake.Command;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Text;

import java.net.MalformedURLException;
import java.net.URL;

@AutoRegister
@Singleton
public class Shortener {

    @Inject private URLShortener shortener;

    @Command(aliases = {"shorten", "short", "shrt"}, desc = "Shorten a URL")
    @Require("shortener.shorten")
    @RateLimit(weight = 2)
    public void shorten(Subject subject, Response response, @Text String url) throws CommandException {
        try {
            response.respond("Shortened: " + shortener.shorten(new URL(url)));
        } catch (MalformedURLException e) {
            throw new CommandException("Invalid URL!");
        }
    }

}
