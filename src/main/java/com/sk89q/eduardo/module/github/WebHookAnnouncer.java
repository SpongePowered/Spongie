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
import com.google.common.eventbus.EventBus;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.helper.GenericBroadcast;
import com.sk89q.eduardo.helper.shortener.URLShortener;
import com.sk89q.eduardo.http.JettyService;
import com.sk89q.eduardo.http.handler.SimpleHandler;
import com.sk89q.eduardo.http.handler.SimpleResponse;
import com.sk89q.eduardo.irc.Users;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import org.apache.commons.codec.binary.Hex;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sk89q.eduardo.http.handler.SimpleResponse.create;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

@Singleton
public class WebHookAnnouncer extends ListenerAdapter<PircBotX> {

    private static final Logger log = LoggerFactory.getLogger(WebHookAnnouncer.class);
    private static final Pattern REF_CLEANUP = Pattern.compile("^refs/heads/");
    private static final Pattern COMMIT_CLEANUP = Pattern.compile("[\r\n].*$", Pattern.DOTALL);
    private static final Pattern SIGNATURE_PATTERN = Pattern.compile("^([^=]+)=(.+)$");
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final int COMMIT_MAX_LEN = 50;

    private final ObjectMapper mapper = new ObjectMapper();
    private Config thisConfig;
    private final Multimap<String, String> targets = HashMultimap.create();
    @Inject private URLShortener shortener;
    @Inject private GenericBroadcast broadcast;

    @Inject
    public WebHookAnnouncer(JettyService jetty, EventBus bus, Config config) {
        thisConfig = config.getConfig("github-webhook");

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Get project -> channel mapping
        Config targets = thisConfig.getConfig("targets");
        for (Entry<String, ConfigValue> entry : targets.root().entrySet()) {
            String projectName = entry.getKey();
            // TODO: Does this actually work properly?
            this.targets.putAll(projectName.toLowerCase(), targets.getStringList(projectName));
        }

        //bot.registerListener(this);
        bus.register(this);

        ContextHandler hookHandler = new ContextHandler("/github-webhook");
        hookHandler.setHandler(new WebHookHandler());
        jetty.registerHandler(hookHandler);
    }

    private boolean verifySignature(String signature, byte[] content) {
        Matcher matcher = SIGNATURE_PATTERN.matcher(signature);

        if (matcher.matches()) {
            String type = matcher.group(1);

            if (type.equalsIgnoreCase("sha1")) {
                String key = thisConfig.getString("secret_key");

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
                "[%s] %s pushed %d commit%s to %s (%s)",
                payload.repository.name, Users.preventMention(payload.pusher.name), payload.commits.size(),
                payload.commits.size() == 1 ? "" : "s",
                simplifyGitRef(payload.ref), shortener.shorten(payload.compare)));

        for (int i = 0; i < payload.commits.size(); i++) {
            Commit commit = payload.commits.get(i);

            if (i >= 3) {
                broadcast(payload.repository.fullName, "... etc.");
                break;
            } else {
                broadcast(payload.repository.fullName, String.format(
                        "%s/%s %s: %s (by %s)",
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

    private class WebHookHandler extends SimpleHandler {
        @Override
        public SimpleResponse respond(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            String signature = request.getHeader("X-Hub-Signature");
            String event = request.getHeader("X-GitHub-Event");
            byte[] data = ByteStreams.toByteArray(request.getInputStream());

            if (verifySignature(signature, data)) {
                try {
                    handlePayload(event, data);
                    return create().body("OK");
                } catch (IOException e) {
                    log.warn("Failed to process GitHub webhook", e);
                    return create()
                            .response(SC_INTERNAL_SERVER_ERROR)
                            .body("Encountered an error processing the payload");
                }
            } else {
                return create()
                        .response(SC_BAD_REQUEST)
                        .body("Invalid signature");
            }
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
