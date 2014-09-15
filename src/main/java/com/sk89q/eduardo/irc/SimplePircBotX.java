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
import org.pircbotx.Configuration.Builder;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.hooks.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class SimplePircBotX implements PircBotXService {

    private static final Logger log = LoggerFactory.getLogger(SimplePircBotX.class);

    private final List<Listener<PircBotX>> listeners = new ArrayList<>();
    private final List<Builder<PircBotX>> builders = new ArrayList<>();
    private final MultiBotManager<PircBotX> manager = new MultiBotManager<>();

    @Inject
    public SimplePircBotX(Config config, EventBus eventBus) {
        eventBus.register(this);

        Config irc = config.getConfig("irc");

        Builder<PircBotX> base = new Configuration.Builder<>()
                .setVersion(irc.getString("version"))
                .setAutoSplitMessage(true)
                .setIdentServerEnabled(false)
                .setAutoNickChange(true);

        for (Config server : irc.getConfigList("servers")) {
            server = server.withFallback(irc.getConfig("default-server"));

            Builder<PircBotX> builder = new Builder<>(base)
                    .setServer(server.getString("host"), server.getInt("port"), server.getString("password"));

            builder
                    .setName(server.getString("name"))
                    .setLogin(server.getString("name"))
                    .setFinger(server.getString("name"))
                    .setRealName(server.getString("name"));

            if (server.getBoolean("ssl")) {
                builder.setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates());
            }

            for (String channel : server.getStringList("auto-join")) {
                log.info("Auto-joining channel {}", channel);
                builder.addAutoJoinChannel(channel);
            }

            builders.add(builder);
        }
    }

    @Subscribe
    public void onStartup(StartupEvent event) {
        for (Builder<PircBotX> builder : builders) {
            listeners.forEach(builder::addListener);
            manager.addBot(builder.buildConfiguration());
        }

        Thread thread = new Thread(manager::start, "PircBotX");
        thread.start();
    }

    @Subscribe
    public void onBroadcast(BroadcastEvent event) {
        String target = event.getTarget();
        if (target.startsWith("#")) {
            for (PircBotX bot : manager.getBots()) {
                bot.sendIRC().message(target, event.getMessage());
            }
        }
    }

    @Override
    public void registerListener(Listener<PircBotX> listener) {
        listeners.add(listener);
    }

}
