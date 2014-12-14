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

package com.sk89q.eduardo.util;

import java.util.regex.Pattern;

public final class GitUtils {

    private static final Pattern REF_CLEANUP = Pattern.compile("^refs/heads/");
    private static final Pattern COMMIT_CLEANUP = Pattern.compile("[\r\n].*$", Pattern.DOTALL);
    private static final int COMMIT_MAX_LEN = 80;

    private GitUtils() {
    }

    public static String shortenHash(String s) {
        return s.substring(0, 8);
    }

    public static String shortenRef(String s) {
        return REF_CLEANUP.matcher(s).replaceAll("");
    }

    public static String shortenMessage(String s) {
        s = COMMIT_CLEANUP.matcher(s).replaceAll("");
        if (s.length() > COMMIT_MAX_LEN) {
            s = s.substring(0, COMMIT_MAX_LEN) + "...";
        }
        return s;
    }

}
