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

import java.util.function.Supplier;

abstract class AbstractConfig extends AbstractConfigObject implements Config {

    @Override
    protected Object get(Object key) {
        return get(String.valueOf(key));
    }

    @Override
    protected Object put(Object key, Object value) {
        return put(String.valueOf(key), value);
    }

    private <T> Supplier<T> call(Supplier<T> supplier) {
        supplier.get();
        return supplier;
    }

    @Override
    public Supplier<Boolean> booleanAt(String path, boolean fallback) {
        return call(() -> getBoolean(path, fallback));
    }

    @Override
    public Supplier<Number> numberAt(String path, Number fallback) {
        return call(() -> getNumber(path, fallback));
    }

    @Override
    public Supplier<Integer> intAt(String path, Integer fallback) {
        return call(() -> getInt(path, fallback));
    }

    @Override
    public Supplier<Long> longAt(String path, Long fallback) {
        return call(() -> getLong(path, fallback));
    }

    @Override
    public Supplier<Double> doubleAt(String path, Double fallback) {
        return call(() -> getDouble(path, fallback));
    }

    @Override
    public Supplier<String> stringAt(String path, String fallback) {
        return call(() -> getString(path, fallback));
    }

    @Override
    public Supplier<Config> configAt(String path) {
        return call(() -> getConfig(path));
    }

    @Override
    public Supplier<ConfigObject> objectAt(String path) {
        return call(() -> getObject(path));
    }

    @Override
    public <T> Supplier<ImmutableList<T>> listAt(String path, Class<T> type) {
        return call(() -> getList(path, type));
    }

    @Override
    public <T> Supplier<ImmutableMap<String, T>> mapAt(String path, Class<T> type) {
        return call(() -> getMap(path, type));
    }

}
