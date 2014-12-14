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

package com.sk89q.eduardo.plugin.irc;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.event.StartupEvent;
import com.sk89q.eduardo.event.message.BroadcastEvent;
import com.sk89q.eduardo.event.message.MessageEvent;
import com.sk89q.eduardo.model.context.Context;
import com.sk89q.eduardo.model.context.Mode;
import com.sk89q.eduardo.model.response.Response;
import com.sk89q.eduardo.service.plugin.Plugin;
import com.sk89q.eduardo.service.event.EventBus;
import com.sk89q.eduardo.service.event.Subscribe;
import com.sk89q.eduardo.util.config.Config;
import com.sk89q.eduardo.util.formatting.StyledFragment;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Plugin(id = "irc")
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
                .setVersion(irc.getString("version", "Eduardo"))
                .setAutoSplitMessage(true)
                .setIdentServerEnabled(false)
                .setAutoNickChange(true);

        for (Config server : irc.getList("servers", Config.class)) {
            Builder<IRCBot> builder = new Builder<>(base)
                    .setServer(
                            server.getString("host", "localhost"),
                            server.getInt("port", 6667),
                            server.getString("password", ""));

            builder
                    .setName(server.getString("name", "MyNewBot"))
                    .setLogin(server.getString("name", "MyNewBot"))
                    .setFinger(server.getString("name", "MyNewBot"))
                    .setRealName(server.getString("name", "MyNewBot"));

            if (server.getBoolean("ssl", false)) {
                builder.setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates());
            }

            for (String channel : server.getList("auto-join", String.class)) {
                log.info("Auto-joining channel {}", channel);
                builder.addAutoJoinChannel(channel);
            }

            builders.put(server.getString("id", "noname-" + System.currentTimeMillis()), builder);
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
        eventBus.post(new MessageEvent(createContext(event), event.getMessage(), new ResponseImpl(event)));
    }

    @Subscribe
    public void onBroadcast(BroadcastEvent event) {
        String target = event.getTarget();
        if (target.startsWith("#")) {
            for (PircBotX bot : manager.getBots()) {
                bot.sendIRC().message(target, IRCColorBuilder.asColorCodes(event.getMessage()));
            }
        }
    }

    @Override
    public void onEvent(Event<IRCBot> event) throws Exception {
        eventBus.post(event);
    }

    private static Context createContext(GenericMessageEvent<? extends IRCBot> event) {
        User user = event.getUser();
        @Nullable Channel channel;
        List<Mode> modes = new ArrayList<>();

        if (event instanceof org.pircbotx.hooks.events.MessageEvent) {
            channel = ((org.pircbotx.hooks.events.MessageEvent) event).getChannel();
        } else if (event instanceof NoticeEvent) {
            channel = ((NoticeEvent) event).getChannel();
        } else {
            channel = null;
        }

        if (channel != null) {
            if (user.getChannelsOpIn().contains(channel)) {
                modes.add(Mode.OPERATOR);
            }

            if (user.getChannelsHalfOpIn().contains(channel)) {
                modes.add(Mode.HALF_OP);
            }

            if (user.getChannelsVoiceIn().contains(channel)) {
                modes.add(Mode.VOICED);
            }
        }

        return new Context(
                new IRCNetwork(event.getBot()),
                new IRCUser(event.getUser()),
                channel != null ? new IRCRoom(channel) : null,
                modes.isEmpty() ? Collections.emptySet() : EnumSet.copyOf(modes));
    }

    private static class ResponseImpl implements Response {
        private final GenericMessageEvent<?> event;

        private ResponseImpl(GenericMessageEvent<?> event) {
            this.event = event;
        }

        @Override
        public void respond(StyledFragment fragment) {
            event.respond(IRCColorBuilder.asColorCodes(fragment));
        }

        @Override
        public void broadcast(StyledFragment fragment) {
            String message = IRCColorBuilder.asColorCodes(fragment);

            if (event instanceof org.pircbotx.hooks.events.MessageEvent) {
                ((org.pircbotx.hooks.events.MessageEvent) event).getChannel().send().message(message);
            } else {
                event.respond(message);
            }
        }
    }

}
