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
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class PircBotXService implements Listener<IrcBot> {

    private static final Logger log = LoggerFactory.getLogger(PircBotXService.class);

    private final Map<String, Builder<IrcBot>> builders = new HashMap<>();
    private final MultiBotManager<IrcBot> manager = new MultiBotManager<>();
    @Inject private EventBus eventBus;

    @Inject
    public PircBotXService(Config config, EventBus eventBus) {
        eventBus.register(this);

        Config irc = config.getConfig("irc");

        Builder<IrcBot> base = new Configuration.Builder<IrcBot>()
                .setVersion(irc.getString("version"))
                .setAutoSplitMessage(true)
                .setIdentServerEnabled(false)
                .setAutoNickChange(true);

        for (Config server : irc.getConfigList("servers")) {
            server = server.withFallback(irc.getConfig("default-server"));

            Builder<IrcBot> builder = new Builder<>(base)
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

            builders.put(server.getString("id"), builder);
        }
    }

    @Subscribe
    public void onStartup(StartupEvent event) {
        for (Map.Entry<String, Builder<IrcBot>> entry : builders.entrySet()) {
            Builder<IrcBot> builder = entry.getValue();
            builder.addListener(this);
            manager.addBot(new IrcBot(builder.buildConfiguration(), entry.getKey()));
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
    public void onEvent(Event<IrcBot> event) throws Exception {
        eventBus.post(event);
    }

}
