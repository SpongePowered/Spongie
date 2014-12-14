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
import java.util.Iterator;
import java.util.List;

class SimpleConfig extends AbstractConfig {

    private final ConfigObject root;

    SimpleConfig(ConfigObject root) {
        this.root = root;
    }

    @Nullable
    @Override
    public Object get(String path) {
        List<String> parts = Configs.splitPath(path);
        Iterator<String> it = parts.iterator();
        ConfigObject parent = root;

        while (it.hasNext()) {
            String name = it.next();

            if (!it.hasNext()) {
                return parent.get(name);
            } else {
                Object child = parent.get(name);
                if (child instanceof ConfigObject) {
                    parent = (ConfigObject) child;
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    @Nullable
    @Override
    public Object put(String path, Object value) {
        List<String> parts = Configs.splitPath(path);
        Iterator<String> it = parts.iterator();
        ConfigObject parent = root;

        while (it.hasNext()) {
            String name = it.next();

            if (!it.hasNext()) {
                return parent.put(name, value);
            } else {
                parent = parent.getObject(name);
            }
        }

        return null;
    }

    @Override
    public ConfigObject toObject() {
        return root;
    }

    @Override
    public String toString() {
        return "Config(" + root + ")";
    }

}
