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

package com.sk89q.eduardo.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class SimpleLogFormatter extends Formatter {

    private static final Logger log = Logger.getLogger(SimpleLogFormatter.class.getCanonicalName());
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        sb.append("[")
                .append(record.getLevel().getLocalizedName().toLowerCase())
                .append("] ")
                .append(record.getLoggerName())
                .append(": ")
                .append(formatMessage(record))
                .append(LINE_SEPARATOR);

        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception e) {
                System.err.println(
                        "!!!! UH OH! An exception occurred while trying to print the exception! " +
                                "The following is NOT the actual error; rather, it is the exception " +
                                "that prevents us from printing the real error");
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public static void configureGlobalLogger() {
        Logger globalLogger = Logger.getLogger("");

        // Set formatter
        for (Handler handler : globalLogger.getHandlers()) {
            handler.setFormatter(new SimpleLogFormatter());
        }

        // Set level
        String logLevel = System.getProperty(
                SimpleLogFormatter.class.getCanonicalName() + ".logLevel", "INFO");
        try {
            Level level = Level.parse(logLevel);
            globalLogger.setLevel(level);
        } catch (IllegalArgumentException e) {
            log.log(Level.WARNING, "Invalid log level of " + logLevel, e);
        }
    }

}