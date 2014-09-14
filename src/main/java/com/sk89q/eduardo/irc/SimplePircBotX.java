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

package com.sk89q.eduardo.irc;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.event.BroadcastEvent;
import com.sk89q.eduardo.event.StartupEvent;
import com.typesafe.config.Config;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SimplePircBotX implements PircBotXService {

    private static final Logger log = LoggerFactory.getLogger(SimplePircBotX.class);

    private final List<Listener<PircBotX>> listeners = new ArrayList<>();
    private final Configuration.Builder<PircBotX> builder;
    private PircBotX bot;

    @Inject
    public SimplePircBotX(Config config, EventBus eventBus) {
        eventBus.register(this);

        Config irc = config.getConfig("irc");

        builder = new Configuration.Builder<>()
                .setName(irc.getString("name"))
                .setLogin(irc.getString("name"))
                .setFinger(irc.getString("name"))
                .setRealName(irc.getString("name"))
                .setVersion(irc.getString("version"))
                .setAutoSplitMessage(true)
                .setIdentServerEnabled(false)
                .setAutoNickChange(true)
                .setServer(irc.getString("server.host"), irc.getInt("server.port"), irc.getString("server.password"));

        if (irc.getBoolean("server.ssl")) {
            builder.setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates());
        }

        for (String channel : irc.getStringList("server.auto_join")) {
            log.info("Auto-joining channel {}", channel);
            builder.addAutoJoinChannel(channel);
        }
    }

    @Subscribe
    public void onStartup(StartupEvent event) {
        listeners.forEach(builder::addListener);

        Configuration<PircBotX> configuration = builder.buildConfiguration();
        bot = new PircBotX(configuration);

        Runnable runnable = () -> {
            try {
                bot.startBot();
            } catch (IOException | IrcException e) {
                log.error("Failed to start IRC client", e);
            }
        };

        Thread thread = new Thread(runnable, "PircBotX");
        thread.start();
    }

    @Subscribe
    public void onBroadcast(BroadcastEvent event) {
        String target = event.getTarget();
        if (target.startsWith("#")) {
            bot.sendIRC().message(target, event.getMessage());
        }
    }

    @Override
    public void registerListener(Listener<PircBotX> listener) {
        listeners.add(listener);
    }

}
