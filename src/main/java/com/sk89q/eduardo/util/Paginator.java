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

import java.util.Collections;
import java.util.List;

public class Paginator<T> {

    private final List<T> list;
    private final int perPage;

    public Paginator(List<T> list, int perPage) {
        this.list = list;
        this.perPage = perPage;
    }

    public int getPageCount() {
        return (int) Math.ceil(list.size() / (double) perPage);
    }

    public Page<T> getPage(int page) {
        page = Math.max(1, page) - 1;
        int offset = page * perPage;
        int end = Math.min(offset + perPage, list.size());
        if (offset >= end) {
            return new Page<T>(page + 1, Collections.emptyList());
        } else {
            return new Page<T>(page + 1, list.subList(offset, end));
        }
    }

    public static class Page<T> {
        public final int page;
        public final List<T> results;

        private Page(int page, List<T> results) {
            this.page = page;
            this.results = results;
        }
    }

}
