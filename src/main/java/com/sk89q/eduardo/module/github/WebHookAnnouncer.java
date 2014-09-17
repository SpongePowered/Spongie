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

package com.sk89q.eduardo.module.github;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.event.http.ConfigureRouteEvent;
import com.sk89q.eduardo.helper.AutoRegister;
import com.sk89q.eduardo.helper.GenericBroadcast;
import com.sk89q.eduardo.helper.shortener.URLShortener;
import com.sk89q.eduardo.http.status.BadRequestError;
import com.sk89q.eduardo.http.status.InternalServerError;
import com.sk89q.eduardo.util.eventbus.Subscribe;
import com.sk89q.eduardo.util.irc.Users;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import org.apache.commons.codec.binary.Hex;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sk89q.eduardo.util.formatting.IRCColorBuilder.asColorCodes;
import static com.sk89q.eduardo.util.formatting.Style.*;
import static com.sk89q.eduardo.util.formatting.StyledFragment.with;
import static spark.Spark.post;

@AutoRegister
@Singleton
public class WebHookAnnouncer extends ListenerAdapter<PircBotX> {

    private static final Logger log = LoggerFactory.getLogger(WebHookAnnouncer.class);
    private static final Pattern REF_CLEANUP = Pattern.compile("^refs/heads/");
    private static final Pattern COMMIT_CLEANUP = Pattern.compile("[\r\n].*$", Pattern.DOTALL);
    private static final Pattern SIGNATURE_PATTERN = Pattern.compile("^([^=]+)=(.+)$");
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final int COMMIT_MAX_LEN = 80;

    private final ObjectMapper mapper = new ObjectMapper();
    private Config thisConfig;
    private final Multimap<String, String> targets = HashMultimap.create();
    @Inject private URLShortener shortener;
    @Inject private GenericBroadcast broadcast;

    @Inject
    public WebHookAnnouncer(Config config) {
        thisConfig = config.getConfig("github-webhook");

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Get project -> channel mapping
        Config targets = thisConfig.getConfig("targets");
        for (Entry<String, ConfigValue> entry : targets.root().entrySet()) {
            String projectName = entry.getKey();
            // TODO: Does this actually work properly?
            this.targets.putAll(projectName.toLowerCase(), targets.getStringList(projectName));
        }
    }

    @Subscribe
    public void onConfigureRoute(ConfigureRouteEvent event) {
        post("/github/webhook/", (request, response) -> {
            try {
                String signature = request.headers("X-Hub-Signature");
                String type = request.headers("X-GitHub-Event");
                byte[] data = ByteStreams.toByteArray(request.raw().getInputStream());

                if (verifySignature(signature, data)) {
                    try {
                        handlePayload(type, data);
                        return "OK";
                    } catch (IOException e) {
                        log.warn("Failed to process GitHub webhook", e);
                        throw new InternalServerError("Encountered an error processing the payload", e);
                    }
                } else {
                    throw new BadRequestError("Invalid signature!");
                }
            } catch (IOException e) {
                throw new InternalServerError("Failed to read input stream", e);
            }
        });
    }

    private boolean verifySignature(@Nullable String signature, byte[] content) {
        if (signature == null) {
            return false;
        }

        Matcher matcher = SIGNATURE_PATTERN.matcher(signature);

        if (matcher.matches()) {
            String type = matcher.group(1);

            if (type.equalsIgnoreCase("sha1")) {
                String key = thisConfig.getString("secret-key");

                try {
                    SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
                    Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
                    mac.init(signingKey);
                    return Hex.encodeHexString(mac.doFinal(content)).equalsIgnoreCase(matcher.group(2));
                } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                    log.warn("Failed to create a signature of the input data", e);
                    return false;
                }
            } else {
                log.warn("Received GitHub web hook with signature algorithm of '{}'", type);
                return false;
            }
        } else {
            log.warn("Received GitHub web hook hit with invalid X-Hub-Signature header format");
            return false;
        }
    }

    private void handlePayload(String event, byte[] data) throws IOException {
        switch (event) {
            case "push":
                handlePush(data);
                break;
            default:
        }
    }

    private void handlePush(byte[] data) throws IOException {
        PushEvent payload = mapper.readValue(data, PushEvent.class);

        log.info("Got GitHub push event for {}", payload.repository.fullName);

        broadcast(payload.repository.fullName, String.format(
                asColorCodes(with()
                        .append(with(BOLD, DARK_GREEN).append("[%s]"))
                        .append(" %s pushed ")
                        .append(with(BOLD).append("%d commit%s"))
                        .append(" to ")
                        .append(with(BOLD).append("%s"))
                        .append(" (%s)")),
                payload.repository.name, Users.preventMention(payload.pusher.name), payload.commits.size(),
                payload.commits.size() == 1 ? "" : "s",
                simplifyGitRef(payload.ref), shortener.shorten(payload.compare)));

        for (int i = 0; i < payload.commits.size(); i++) {
            Commit commit = payload.commits.get(i);

            if (i >= 5) {
                broadcast(payload.repository.fullName, "... etc.");
                break;
            } else {
                broadcast(payload.repository.fullName, String.format(
                        asColorCodes(with()
                                .append(with(BOLD).append("%s/%s"))
                                .append(" %s: %s (by %s)")),
                        payload.repository.name, simplifyGitRef(payload.ref),
                        simplifyGitId(commit.id), simplfiyCommitMessage(commit.message),
                        Users.preventMention(commit.author.name)));
            }
        }
    }

    private void broadcast(String project, String message) {
        Collection<String> t = targets.get(project.toLowerCase());
        for (String target : t) {
            broadcast.broadcast(target, message);
        }
    }

    private static String simplifyGitId(String s) {
        return s.substring(0, 8);
    }

    private static String simplifyGitRef(String s) {
        return REF_CLEANUP.matcher(s).replaceAll("");
    }

    private static String simplfiyCommitMessage(String s) {
        s = COMMIT_CLEANUP.matcher(s).replaceAll("test");
        if (s.length() > COMMIT_MAX_LEN) {
            s = s.substring(0, COMMIT_MAX_LEN) + "...";
        }
        return s;
    }

}
