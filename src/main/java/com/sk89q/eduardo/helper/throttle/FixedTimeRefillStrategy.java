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

/*
 * Copyright 2012-2014 Brandon Beck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.base.Ticker;
import org.isomorphism.util.TokenBucket.RefillStrategy;

import java.util.concurrent.TimeUnit;

class FixedTimeRefillStrategy implements RefillStrategy {

    private final Ticker ticker;
    private final long numTokens;
    private final long periodInNanos;
    private long lastRefillTime;

    FixedTimeRefillStrategy(Ticker ticker, long numTokens, long period, TimeUnit unit) {
        this.ticker = ticker;
        this.numTokens = numTokens;
        this.periodInNanos = unit.toNanos(period);
    }

    @Override
    public synchronized long refill() {
        long now = ticker.read();
        long amount = (now - lastRefillTime) / periodInNanos * numTokens;
        if (amount > 0) {
            lastRefillTime = now;
        }
        return amount;
    }

}

