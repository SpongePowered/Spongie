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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.Contexts;
import com.sk89q.eduardo.event.StartupEvent;
import com.sk89q.eduardo.event.message.BroadcastEvent;
import com.sk89q.eduardo.event.message.MessageEvent;
import com.sk89q.eduardo.helper.Response;
import com.sk89q.eduardo.util.eventbus.EventBus;
import com.sk89q.eduardo.util.eventbus.Subscribe;
import com.typesafe.config.Config;
import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class PircBotXClient implements Listener<IRCBot> {

    private static final Logger log = LoggerFactory.getLogger(PircBotXClient.class);

    private final Map<String, Builder<IRCBot>> builders = new HashMap<>();
    private final MultiBotManager<IRCBot> manager = new MultiBotManager<>();
    @Inject private EventBus eventBus;

    @Inject
    public PircBotXClient(Config config, EventBus eventBus) {
        eventBus.register(this);

        Config irc = config.getConfig("irc");

        Builder<IRCBot> base = new Configuration.Builder<IRCBot>()
                .setVersion(irc.getString("version"))
                .setAutoSplitMessage(true)
                .setIdentServerEnabled(false)
                .setAutoNickChange(true);

        for (Config server : irc.getConfigList("servers")) {
            server = server.withFallback(irc.getConfig("default-server"));

            Builder<IRCBot> builder = new Builder<>(base)
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
        for (Map.Entry<String, Builder<IRCBot>> entry : builders.entrySet()) {
            Builder<IRCBot> builder = entry.getValue();
            builder.addListener(this);
            manager.addBot(new IRCBot(builder.buildConfiguration(), entry.getKey()));
        }

        Thread thread = new Thread(manager::start, "PircBotX");
        thread.start();
    }

    @Subscribe
    public void onGenericMessage(GenericMessageEvent<IRCBot> event) {
        eventBus.post(new MessageEvent(Contexts.create(event), event.getMessage(), new ResponseImpl(event)));
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
    public void onEvent(Event<IRCBot> event) throws Exception {
        eventBus.post(event);
    }

    private static class ResponseImpl implements Response {
        private final GenericMessageEvent<?> event;

        private ResponseImpl(GenericMessageEvent<?> event) {
            this.event = event;
        }

        @Override
        public void respond(String message) {
            event.respond(message);
        }

        @Override
        public void broadcast(String message) {
            if (event instanceof org.pircbotx.hooks.events.MessageEvent) {
                ((org.pircbotx.hooks.events.MessageEvent) event).getChannel().send().message(message);
            } else {
                event.respond(message);
            }
        }
    }

}
