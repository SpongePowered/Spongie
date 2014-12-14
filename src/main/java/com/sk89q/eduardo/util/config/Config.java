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

package com.sk89q.eduardo.util.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public interface Config {

    @Nullable
    Object get(String path);

    @Nullable
    Object put(String path, Object value);

    boolean getBoolean(String path, boolean fallback);

    Number getNumber(String path, Number fallback);

    int getInt(String path, int fallback);

    long getLong(String path, long fallback);

    double getDouble(String path, double fallback);

    String getString(String path, String fallback);

    ConfigObject getObject(String path);

    Config getConfig(String path);

    ConfigList<?> getList(String path);

    <T> ImmutableList<T> getList(String path, Class<T> type);

    <T> ImmutableMap<String, T> getMap(String path, Class<T> type);

    Supplier<Boolean> booleanAt(String path, boolean fallback);

    Supplier<Number> numberAt(String path, Number fallback);

    Supplier<Integer> intAt(String path, Integer fallback);

    Supplier<Long> longAt(String path, Long fallback);

    Supplier<Double> doubleAt(String path, Double fallback);

    Supplier<String> stringAt(String path, String fallback);

    Supplier<ConfigObject> objectAt(String path);

    Supplier<Config> configAt(String path);

    <T> Supplier<ImmutableList<T>> listAt(String path, Class<T> type);

    <T> Supplier<ImmutableMap<String, T>> mapAt(String path, Class<T> type);

    ConfigObject toObject();

}
