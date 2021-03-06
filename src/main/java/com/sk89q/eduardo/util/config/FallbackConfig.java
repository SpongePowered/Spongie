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

import javax.annotation.Nullable;

public class FallbackConfig extends AbstractConfig {

    private final Config config;
    private final Config fallback;

    public FallbackConfig(Config config, Config fallback) {
        this.config = config;
        this.fallback = fallback;
    }

    @Nullable
    @Override
    public Object get(String path) {
        Object object = config.get(path);
        if (object != null) {
            return object;
        } else {
            return fallback.get(path);
        }
    }

    @Nullable
    @Override
    public Object put(String path, Object value) {
        return config.put(path, value);
    }

    @Override
    public ConfigObject toObject() {
        return config.toObject();
    }

}
