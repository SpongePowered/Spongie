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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.sk89q.eduardo.util.eventbus.EventBus;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(DefaultModule.class);

    private final Config config;

    public DefaultModule(Config config) {
        checkNotNull(config);
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        bindListener(Matchers.any(), new AutoRegisterListener());

        try {
            Config mapping = config.getConfig("services.mapping");
            for (Entry<String, ConfigValue> entry : mapping.entrySet()) {
                String serviceName = entry.getKey();
                String implName = entry.getValue().unwrapped().toString();
                log.info("Binding {} -> {}", new Object[] { serviceName, implName });
                Class<Object> service = (Class<Object>) Class.forName(serviceName);
                Class<Object> impl = (Class<Object>) Class.forName(implName);
                bind(service).to(impl).in(Singleton.class);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Invalid service definition", e);
        }
    }

    @Provides
    @Singleton
    EventBus provideEventBus() {
        return new EventBus();
    }

    @Provides
    @Singleton
    ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }

    @Provides
    @Singleton
    Config provideConfig() {
        return config;
    }

}
