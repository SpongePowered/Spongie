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

package com.sk89q.eduardo.connector.irc;

import com.google.common.base.Joiner;
import com.sk89q.eduardo.util.formatting.Fragment;
import com.sk89q.eduardo.util.formatting.Style;
import com.sk89q.eduardo.util.formatting.StyleSet;
import com.sk89q.eduardo.util.formatting.StyledFragment;

public class IRCColorBuilder {

    private static final IRCColorBuilder instance = new IRCColorBuilder();
    private static final Joiner newLineJoiner = Joiner.on("\n");
    
    /**
     * Convert a message into color-coded text.
     * 
     * @param message the message
     * @return a list of lines
     */
    public String[] build(StyledFragment message) {
        StringBuilder builder = new StringBuilder();
        buildFragment(builder, message, message.getStyle(), new StyleSet());
        return builder.toString().split("\r?\n");
    }
    
    /**
     * Build a fragment.
     * 
     * @param builder the string builder
     * @param message the message
     * @param parentStyle the parent style
     * @param lastStyle the last style
     * @return the last style used
     */
    private StyleSet buildFragment(StringBuilder builder, StyledFragment message, StyleSet parentStyle, StyleSet lastStyle) {
        for (Fragment node : message.getChildren()) {
            if (node instanceof StyledFragment) {
                StyledFragment fragment = (StyledFragment) node;
                lastStyle = buildFragment(
                        builder, fragment, 
                        parentStyle.extend(message.getStyle()), lastStyle);
            } else {
                StyleSet style = parentStyle.extend(message.getStyle());
                builder.append(getAdditive(style, lastStyle));
                builder.append(node);
                lastStyle = style;
            }
        }
        
        return lastStyle;
    }
    
    /**
     * Get the formatting codes.
     * 
     * @param style the style
     * @return the color codes
     */
    public static String getFormattingCode(StyleSet style) {
        StringBuilder builder = new StringBuilder();
        if (style.isBold()) {
            builder.append(Style.BOLD);
        }
        if (style.isUnderline()) {
            builder.append(Style.UNDERLINE);
        }
        if (style.isReverse()) {
            builder.append(Style.REVERSE);
        }
        return builder.toString();
    }
    
    /**
     * Get the formatting and color codes.
     * 
     * @param style the style
     * @return the color codes
     */
    public static String getCode(StyleSet style) {
        StringBuilder builder = new StringBuilder();
        builder.append(getFormattingCode(style));
        if (style.getColor() != null) {
            builder.append(style.getColor());
        }
        return builder.toString();
    }

    /**
     * Get the additional color codes needed to set the given style when the current
     * style is the other given one.
     * 
     * @param resetTo the style to reset to
     * @param resetFrom the style to reset from
     * @return the color codes
     */
    public static String getAdditive(StyleSet resetTo, StyleSet resetFrom) {
        if (!resetFrom.hasFormatting() && resetTo.hasFormatting()) {
            StringBuilder builder = new StringBuilder();
            builder.append(getFormattingCode(resetTo));
            if (resetFrom.getColor() != resetTo.getColor()) {
                builder.append(resetTo.getColor());
            }
            return builder.toString();
        } else if (!resetFrom.hasEqualFormatting(resetTo) || 
                (resetFrom.getColor() != null && resetTo.getColor() == null)) {
            // Have to set reset code and add back all the formatting codes
            return Style.RESET + getCode(resetTo);
        } else {
            if (resetFrom.getColor() != resetTo.getColor()) {
                return String.valueOf(resetTo.getColor());
            }
        }
        
        return "";
    }
    
    /**
     * Callback for transforming a word, such as a URL.
     * 
     * @param word the word
     * @return the transformed value, or null to do nothing
     */
    protected String transform(String word) {
        return null;
    }

    /**
     * Convert the given styled fragment into color codes.
     *
     * @param fragment the fragment
     * @return color codes
     */
    public static String asColorCodes(StyledFragment fragment) {
        return newLineJoiner.join(instance.build(fragment));
    }

}
