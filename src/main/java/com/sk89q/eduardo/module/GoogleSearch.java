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

package com.sk89q.eduardo.module;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.Customsearch.Builder;
import com.google.api.services.customsearch.CustomsearchRequest;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.helper.AutoRegister;
import com.sk89q.eduardo.helper.Response;
import com.sk89q.eduardo.helper.throttle.RateLimit;
import com.sk89q.intake.Command;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Text;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import static com.sk89q.eduardo.util.formatting.StyledFragment.with;
import static com.sk89q.eduardo.util.formatting.component.Separator.separator;
import static com.sk89q.eduardo.util.formatting.component.URLFragment.link;

@AutoRegister
@Singleton
public class GoogleSearch {

    private static final Logger log = LoggerFactory.getLogger(GoogleSearch.class);

    private final String apiKey;
    private final String searchId;

    @Inject
    public GoogleSearch(Config config) {
        Config thisConfig = config.getConfig("google-search");
        apiKey = thisConfig.getString("api-key");
        searchId = thisConfig.getString("search-id");
    }

    @Command(aliases = {"g", "search", "gsearch", "googlesearch"}, desc = "Search Google for a query")
    @Require("googlesearch")
    @RateLimit(weight = 2)
    public void searchGoogle(Response response, @Text String query) throws CommandException {
        try {
            Builder builder = new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(), new JacksonFactory(), null);
            builder.setApplicationName("IRC");
            builder.setCustomsearchRequestInitializer(new CustomsearchRequestInitializer() {
                @Override
                protected void initializeCustomsearchRequest(CustomsearchRequest<?> request) throws IOException {
                    request.setKey(apiKey);
                    request.set("cx", searchId);
                }
            });
            Customsearch customsearch = builder.build();
            Search searchResult = customsearch.cse().list(query).execute();

            @Nullable List<Result> items = searchResult.getItems();

            if (items != null && !items.isEmpty()) {
                Result item = items.get(0);
                response.broadcast(with()
                        .append(link(item.getLink()))
                        .append(separator())
                        .append(item.getTitle())
                        .append(separator())
                        .append(item.getSnippet()));
            } else {
                response.respond("No results found for '" + query + "'");
            }
        } catch (GeneralSecurityException | IOException e) {
            log.warn("Failed to issue Google Search", e);
            throw new CommandException("Failed to search Google!");
        }
    }

}
