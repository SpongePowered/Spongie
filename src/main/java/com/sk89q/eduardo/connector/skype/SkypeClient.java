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

package com.sk89q.eduardo.connector.skype;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.context.Context;
import com.sk89q.eduardo.context.Mode;
import com.sk89q.eduardo.event.StartupEvent;
import com.sk89q.eduardo.event.message.MessageEvent;
import com.sk89q.eduardo.helper.AbstractDirectResponse;
import com.sk89q.eduardo.helper.AutoRegister;
import com.sk89q.eduardo.util.eventbus.EventBus;
import com.sk89q.eduardo.util.eventbus.Subscribe;
import com.sk89q.eduardo.util.formatting.Fragments;
import com.sk89q.eduardo.util.formatting.StyledFragment;
import com.skype.ChatMessage;
import com.skype.ChatMessageAdapter;
import com.skype.Skype;
import com.skype.SkypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@AutoRegister
@Singleton
public class SkypeClient {

    private static final Logger log = LoggerFactory.getLogger(SkypeClient.class);

    @Inject private EventBus eventBus;
    private final Object lock = new Object();
    private ChatMessage lastMessage;

    @Subscribe
    public void onStartup(StartupEvent event) {
        Skype.setDaemon(false);

        try {
            Skype.addChatMessageListener(new ChatMessageAdapter() {
                @Override
                public void chatMessageReceived(ChatMessage received) throws SkypeException {
                    boolean send = false;

                    synchronized (lock) {
                        if (lastMessage != received) {
                            send = true;
                            lastMessage = received; // Sometimes doubles for some reason
                        }
                    }

                    if (send) {
                        eventBus.post(received);
                    }
                }
            });
        } catch (SkypeException e) {
            log.error("Failed to hook into Skype", e);
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) throws SkypeException {
        eventBus.post(new MessageEvent(createContext(event), event.getContent(), new AbstractDirectResponse() {
            @Override
            public void broadcast(StyledFragment fragment) {
                try {
                    event.getChat().send(Fragments.renderPlain(fragment));
                } catch (SkypeException e) {
                    log.error("Failed to hook into Skype", e);
                }
            }
        }));
    }

    private static Context createContext(ChatMessage event) throws SkypeException {
        return new Context(
                SkypeNetwork.INSTANCE,
                new SkypeUser(event.getSender()),
                new SkypeRoom(event.getChat()),
                Collections.<Mode>emptySet());
    }

}
