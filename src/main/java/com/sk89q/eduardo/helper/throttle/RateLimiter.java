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

package com.sk89q.eduardo.helper.throttle;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.irc.IrcContext;
import com.typesafe.config.Config;
import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

@Singleton
public class RateLimiter {

    private static final double FACTOR = 100;
    private static final String GLOBAL_BUCKET = "global";
    private static final String CHANNEL_BUCKET = "per-channel";
    private static final String HOST_BUCKET = "per-host";

    private final Ticker ticker = Ticker.systemTicker();
    private final Config thisConfig;
    private final TokenBucket global;
    private final LoadingCache<String, TokenBucket> perChannel;
    private final LoadingCache<String, TokenBucket> perHost;

    @Inject
    public RateLimiter(Config config) {
        this.thisConfig = config.getConfig("rate-limiter");
        global = createTokenBucket(GLOBAL_BUCKET);
        perChannel = createTokenBucketCache(CHANNEL_BUCKET);
        perHost = createTokenBucketCache(HOST_BUCKET);
    }

    public synchronized boolean tryConsume(IrcContext context, double tokens) {
        long tokensLong = applyFactor(tokens);

        if (context.getUser() != null) {
            TokenBucket bucket = perHost.getUnchecked(context.getUser().getHostmask());
            if (!bucket.tryConsume(tokensLong)) {
                return false;
            }
        }

        if (context.getChannel() != null) {
            TokenBucket bucket = perChannel.getUnchecked(context.getChannel().toLowerCase());
            if (!bucket.tryConsume(tokensLong)) {
                return false;
            }
        }

        return global.tryConsume(tokensLong);

    }

    private long applyFactor(double tokens) {
        return (long) (tokens * FACTOR);
    }

    private TokenBucket createTokenBucket(String key) {
        Config bucket = thisConfig.getConfig(key);
        return TokenBuckets.builder()
                .withCapacity(applyFactor(bucket.getDouble("capacity")))
                .withRefillStrategy(new FixedTimeRefillStrategy(
                        ticker,
                        applyFactor(bucket.getDouble("refill")),
                        (long) (bucket.getDouble("wait") * 1000), TimeUnit.MILLISECONDS))
                .build();
    }

    private <V> LoadingCache<V, TokenBucket> createTokenBucketCache(String configKey) {
        return CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<V, TokenBucket>() {
                            @Override
                            public TokenBucket load(@Nullable V key) {
                                return createTokenBucket(configKey);
                            }
                        });
    }

}
