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

package com.sk89q.eduardo.auth;

import com.google.inject.Inject;
import com.sk89q.eduardo.auth.policy.MultiMapPolicy;
import com.sk89q.eduardo.auth.policy.Policy;
import com.sk89q.eduardo.irc.ChannelUserMode;
import com.sk89q.eduardo.irc.IrcContext;
import com.typesafe.config.Config;

import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConfigAuthProvider implements AuthService {

    private final Policy<IrcContext> policy = new MultiMapPolicy<>();

    @Inject
    public ConfigAuthProvider(Config config) {
        for (Config policy : config.getConfigList("config-perms.policy")) {
            policy = policy.withFallback(config.getConfig("config-perms.default-policy"));

            ContextMatch match = new ContextMatch();
            match.matchAllUsers(policy.getStringList("users"));
            match.matchAllChannels(policy.getStringList("channels"));
            match.matchAllModes(
                    policy.getStringList("modes")
                            .stream()
                            .map(ChannelUserMode::valueOf)
                            .collect(Collectors.toList()));

            for (String permission : policy.getStringList("grant")) {
                this.policy.grant(permission, match);
            }

            for (String permission : policy.getStringList("deny")) {
                this.policy.deny(permission, match);
            }
        }

    }

    @Override
    public Subject login(IrcContext context) {
        return new ConfigSubject(context);
    }

    private class ConfigSubject implements Subject {
        private final IrcContext context;

        private ConfigSubject(IrcContext context) {
            checkNotNull(context);
            this.context = context;
        }

        @Override
        public boolean testPermission(String permission) {
            return policy.testPermission(permission, context);
        }
    }

}
