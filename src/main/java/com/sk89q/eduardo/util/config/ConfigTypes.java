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

import java.util.function.Function;

final class ConfigTypes {

    private ConfigTypes() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T serialize(Object object) {
        if (object instanceof String
                || object instanceof Integer
                || object instanceof Long
                || object instanceof Float
                || object instanceof Double
                || object instanceof Boolean
                || object instanceof ConfigObject
                || object instanceof ConfigList) {
            return (T) object;
        } else {
            throw new IllegalArgumentException("Can't put object of type '" + object.getClass().getName() + "'");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Function<Object, T> getViewConverter(Class<T> type) {
        if (Boolean.class.isAssignableFrom(type)) {
            return (Function<Object, T>) (Function<Object, Boolean>) ConfigTypes::asBoolean;
        } else if (Integer.class.isAssignableFrom(type)) {
            return (Function<Object, T>) (Function<Object, Integer>) ConfigTypes::asInteger;
        } else if (Long.class.isAssignableFrom(type)) {
            return (Function<Object, T>) (Function<Object, Long>) ConfigTypes::asLong;
        } else if (Double.class.isAssignableFrom(type)) {
            return (Function<Object, T>) (Function<Object, Double>) ConfigTypes::asDouble;
        } else if (Number.class.isAssignableFrom(type)) {
            return (Function<Object, T>) (Function<Object, Number>) ConfigTypes::asNumber;
        } else if (String.class.isAssignableFrom(type)) {
            return (Function<Object, T>) (Function<Object, String>) ConfigTypes::asString;
        } else if (ConfigObject.class.isAssignableFrom(type)) {
            return (Function<Object, T>) (Function<Object, ConfigObject>) ConfigTypes::asObject;
        } else if (ConfigList.class.isAssignableFrom(type)) {
            return (Function<Object, T>) (Function<Object, ConfigList<?>>) ConfigTypes::asList;
        } else if (Config.class.isAssignableFrom(type)) {
            return (Function<Object, T>) (Function<Object, Config>) ConfigTypes::asConfig;
        } else {
            throw new IllegalArgumentException("Can't use type " + type.getName());
        }
    }

    public static Boolean asBoolean(Object o) {
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else {
            return null;
        }
    }

    public static Number asNumber(Object o) {
        if (o instanceof Number) {
            return (Number) o;
        } else {
            return null;
        }
    }

    public static Integer asInteger(Object o) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        } else {
            try {
                return Integer.parseInt(String.valueOf(o));
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public static Long asLong(Object o) {
        if (o instanceof Number) {
            return ((Number) o).longValue();
        } else {
            try {
                return Long.parseLong(String.valueOf(o));
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public static Double asDouble(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else {
            try {
                return Double.parseDouble(String.valueOf(o));
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public static ConfigObject asObject(Object o) {
        if (o instanceof ConfigObject) {
            return (ConfigObject) o;
        } else {
            return null;
        }
    }

    public static Config asConfig(Object o) {
        if (o instanceof ConfigObject) {
            return ((ConfigObject) o).toConfig();
        } else {
            return null;
        }
    }

    public static ConfigList<?> asList(Object o) {
        if (o instanceof ConfigList) {
            return (ConfigList<?>) o;
        } else {
            return null;
        }
    }

    public static String asString(Object o) {
        return String.valueOf(o);
    }

}
