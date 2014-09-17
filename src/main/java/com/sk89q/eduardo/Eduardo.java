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

package com.sk89q.eduardo;

import com.beust.jcommander.JCommander;
import com.sk89q.eduardo.util.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.sk89q.eduardo.event.ConfigureEvent;
import com.sk89q.eduardo.event.StartupEvent;
import com.sk89q.eduardo.util.logging.SimpleLogFormatter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@Singleton
public class Eduardo {

    private static final Logger log = LoggerFactory.getLogger(Eduardo.class);

    private final Injector injector;
    private final Config config;
    private final EventBus bus;

    @Inject
    public Eduardo(Injector injector, Config config, EventBus bus) {
        this.injector = injector;
        this.config = config;
        this.bus = bus;
    }

    public void load() throws LoaderException {
        try {
            for (String name : config.getStringList("modules.enabled")) {
                log.info("Loading module {}...", name);
                injector.getInstance(Class.forName(name));
            }
        } catch (ClassNotFoundException e) {
            throw new LoaderException("Failed to load modules", e);
        }

        log.info("Modules loaded; initializing...");

        bus.post(new ConfigureEvent());
        bus.post(new StartupEvent());

        log.info("Initialization complete.");
    }

    public static void main(String[] args) throws IOException, IrcException {
        SimpleLogFormatter.configureGlobalLogger();

        log.info("Eduardo IRC bot");
        log.info("(c) sk89q <http://www.sk89q.com>");

        CommandOptions opt = new CommandOptions();
        new JCommander(opt, args);

        if (opt.config == null) {
            log.error("No configuration file was specified");
            System.exit(1);
            return;
        }

        File configFile = new File(opt.config);
        Config config = ConfigFactory.parseFileAnySyntax(configFile).withFallback(ConfigFactory.load());
        Injector injector = Guice.createInjector(new DefaultModule(config));

        try {
            injector.getInstance(Eduardo.class).load();
        } catch (LoaderException e) {
            log.error("Failed to load the application", e);
        } catch (Exception e) {
            log.error("Failed to load the application due to an unexpected error", e);
        }
    }

}
