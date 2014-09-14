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

package com.sk89q.eduardo.auth.policy;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

public class MultiMapPolicy<T> implements Policy<T> {

    private final Multimap<String, Predicate<T>> grant = HashMultimap.create();
    private final Multimap<String, Predicate<T>> deny = HashMultimap.create();

    @Override
    public void grant(String permission, Predicate<T> predicate) {
        checkNotNull(predicate);
        grant.put(permission.toLowerCase(), predicate);
    }

    @Override
    public void deny(String permission, Predicate<T> predicate) {
        checkNotNull(predicate);
        deny.put(permission.toLowerCase(), predicate);
    }

    @Override
    public boolean testPermission(String permission, T context) {
        String current = permission.replaceAll("\\.+", ".");
        int lastIndex = current.length();
        Result result = Result.NONE;

        Result wildcard = getResult("*", context);
        switch (wildcard) {
            case ALLOW: result = Result.ALLOW; break;
            case DENY: return false;
            default:
        }

        do {
            current = current.substring(0, lastIndex);

            Result r = getResult(current, context);
            switch (r) {
                case ALLOW: result = Result.ALLOW; break;
                case DENY: return false;
                default:
            }
        } while ((lastIndex = current.lastIndexOf('.')) != -1);

        return result == Result.ALLOW;
    }

    public Result getResult(String permission, T context) {
        for (Predicate<T> predicate : deny.get(permission)) {
            if (predicate.test(context)) {
                return Result.DENY;
            }
        }

        for (Predicate<T> predicate : grant.get(permission)) {
            if (predicate.test(context)) {
                return Result.ALLOW;
            }
        }

        return Result.NONE;
    }

    public static enum Result {
        ALLOW,
        DENY,
        NONE
    }

}
