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

package com.sk89q.eduardo.util.formatting;

import com.google.common.base.Joiner;

class PlainTextBuilder {

    private static final PlainTextBuilder instance = new PlainTextBuilder();
    private static final Joiner newLineJoiner = Joiner.on("\n");

    PlainTextBuilder() {
    }

    /**
     * Convert a message into color-coded text.
     *
     * @param message the message
     * @return a list of lines
     */
    public String[] build(StyledFragment message) {
        StringBuilder builder = new StringBuilder();
        buildFragment(builder, message);
        return builder.toString().split("\r?\n");
    }

    /**
     * Build a fragment.
     *
     * @param builder the string builder
     * @param message the message
     */
    private void buildFragment(StringBuilder builder, StyledFragment message) {
        for (Fragment node : message.getChildren()) {
            if (node instanceof StyledFragment) {
                StyledFragment fragment = (StyledFragment) node;
                buildFragment(builder, fragment);
            } else {
                builder.append(node);
            }
        }
    }

    /**
     * Convert the given styled fragment into color codes.
     *
     * @param fragment the fragment
     * @return color codes
     */
    static String asPlainText(StyledFragment fragment) {
        return newLineJoiner.join(instance.build(fragment));
    }

}
