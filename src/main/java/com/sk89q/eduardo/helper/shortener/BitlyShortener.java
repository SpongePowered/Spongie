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

package com.sk89q.eduardo.helper.shortener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.rosaloves.bitlyj.Bitly;
import com.rosaloves.bitlyj.Bitly.Provider;
import com.rosaloves.bitlyj.BitlyException;
import com.rosaloves.bitlyj.ShortenedUrl;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

@Singleton
public class BitlyShortener implements URLShortener {

    private static final Logger log = LoggerFactory.getLogger(BitlyShortener.class);

    private final Provider client;

    @Inject
    public BitlyShortener(Config config) {
        Config thisConfig = config.getConfig("bitly");
        client = Bitly.as(thisConfig.getString("user"), thisConfig.getString("api-key"));
    }

    @Override
    public URL shorten(URL url) {
        try {
            ShortenedUrl shortened = client.call(Bitly.shorten(url.toExternalForm()));
            return new URL(shortened.getShortUrl());
        } catch (BitlyException e) {
            log.warn("Bitly shortening has failed", e);
            return url;
        } catch (MalformedURLException e) {
            log.warn("Bitly service returned an invalid shortened URL", e);
            return url;
        }
    }

}
