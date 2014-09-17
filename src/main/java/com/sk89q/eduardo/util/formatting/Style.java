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
 * ANY WARRANTY), without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.eduardo.util.formatting;

import org.pircbotx.Colors;

/**
 * All supported color values for chat.
 */
public enum Style {

    RESET(Colors.NORMAL, false),
    BOLD(Colors.BOLD, false),
    UNDERLINE(Colors.UNDERLINE, false),
    REVERSE(Colors.REVERSE, false),

    WHITE(Colors.WHITE),
    BLACK(Colors.BLACK),
    DARK_BLUE(Colors.DARK_BLUE),
    DARK_GREEN(Colors.DARK_GREEN),
    RED(Colors.RED),
    BROWN(Colors.BROWN),
    PURPLE(Colors.PURPLE),
    OLIVE(Colors.OLIVE),
    YELLOW(Colors.YELLOW),
    GREEN(Colors.GREEN),
    TEAL(Colors.TEAL),
    CYAN(Colors.CYAN),
    BLUE(Colors.BLUE),
    MAGENTA(Colors.MAGENTA),
    DARK_GRAY(Colors.DARK_GRAY),
    LIGHT_GRAY(Colors.LIGHT_GRAY);

    private final String code;
    private final boolean color;

    private Style(String code) {
        this.code = code;
        this.color = true;
    }

    private Style(String code, boolean color) {
        this.code = code;
        this.color = color;
    }

    public String getCode() {
        return code;
    }

    public boolean isColor() {
        return color;
    }

    public static String stripColor(String str) {
        return Colors.removeFormattingAndColors(str);
    }

    @Override
    public String toString() {
        return code;
    }

}
