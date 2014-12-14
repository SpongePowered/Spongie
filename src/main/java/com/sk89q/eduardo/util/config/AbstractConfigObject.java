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

import java.util.Map;
import java.util.function.Function;

abstract class AbstractConfigObject {

    protected abstract Object get(Object key);

    protected abstract Object put(Object key, Object value);

    private <T> T getOrSet(String key, Function<Object, T> converter, T fallback) {
        Object object = get(key);
        if (object == null) {
            put(key, fallback);
            return fallback;
        } else {
            T value = converter.apply(object);
            if (value == null) {
                put(key, fallback);
                return fallback;
            } else {
                return value;
            }
        }
    }

    private ConfigList<?> getOrSetList(String key) {
        Object object = get(key);
        if (object == null) {
            ConfigList<Object> fallback = new ConfigList<>();
            put(key, fallback);
            return fallback;
        } else if (object instanceof ConfigList) {
            return (ConfigList<?>) object;
        } else {
            ConfigList<Object> fallback = new ConfigList<>();
            fallback.add(object);
            put(key, fallback);
            return fallback;
        }
    }

    public boolean getBoolean(String key, boolean fallback) {
        return getOrSet(key, ConfigTypes::asBoolean, fallback);
    }

    public Number getNumber(String key, Number fallback) {
        return getOrSet(key, ConfigTypes::asNumber, fallback);
    }

    public int getInt(String key, int fallback) {
        return getOrSet(key, ConfigTypes::asInteger, fallback);
    }

    public long getLong(String key, long fallback) {
        return getOrSet(key, ConfigTypes::asLong, fallback);
    }

    public double getDouble(String key, double fallback) {
        return getOrSet(key, ConfigTypes::asDouble, fallback);
    }

    public String getString(String key, String fallback) {
        return getOrSet(key, ConfigTypes::asString, fallback);
    }

    public ConfigObject getObject(String key) {
        return getOrSet(key, ConfigTypes::asObject, new ConfigObject());
    }

    public Config getConfig(String key) {
        return getObject(key).toConfig();
    }

    public ConfigList<?> getList(String path) {
        return getOrSetList(path);
    }

    public <T> ImmutableList<T> getList(String path, Class<T> type) {
        ConfigList<?> list = getOrSetList(path);
        Function<Object, T> converter = ConfigTypes.getViewConverter(type);
        ImmutableList.Builder<T> builder = new ImmutableList.Builder<>();
        for (Object object : list) {
            T value = converter.apply(object);
            if (value != null) {
                builder.add(value);
            }
        }
        return builder.build();
    }

    public <T> ImmutableMap<String, T> getMap(String path, Class<T> type) {
        ConfigObject object = getObject(path);
        Function<Object, T> converter = ConfigTypes.getViewConverter(type);
        ImmutableMap.Builder<String, T> builder = new ImmutableMap.Builder<>();
        for (Map.Entry<Object, Object> entry : object.entrySet()) {
            T value = converter.apply(entry.getValue());
            if (entry.getKey() instanceof String && value != null) {
                builder.put((String) entry.getKey(), value);
            }
        }
        return builder.build();
    }

}
