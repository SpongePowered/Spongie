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

import java.util.AbstractList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ConfigList<T> extends AbstractList<T> implements List<T> {

    private final List<T> list = new CopyOnWriteArrayList<>();

    @Override
    public boolean add(T o) {
        return o != null && list.add(ConfigTypes.serialize(o));
    }

    @Override
    public T set(int index, T element) {
        if (element != null) {
            return list.set(index, ConfigTypes.serialize(element));
        } else {
            return null;
        }
    }

    @Override
    public void add(int index, Object element) {
        if (element != null) {
            list.add(index, ConfigTypes.serialize(element));
        }
    }

    @Override
    public T remove(int index) {
        return list.remove(index);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public String toString() {
        return list.toString();
    }

}
