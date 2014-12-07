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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sk89q.eduardo.context.Context;
import com.sk89q.eduardo.context.Room;
import com.sk89q.eduardo.event.CommandEvent;
import com.sk89q.eduardo.helper.AutoRegister;
import com.sk89q.eduardo.helper.Response;
import com.sk89q.eduardo.helper.command.CommandManager;
import com.sk89q.eduardo.helper.persistence.Bucket;
import com.sk89q.eduardo.helper.persistence.Persistence;
import com.sk89q.eduardo.helper.throttle.RateLimit;
import com.sk89q.eduardo.util.eventbus.EventBus;
import com.sk89q.eduardo.util.eventbus.EventHandler.Priority;
import com.sk89q.eduardo.util.eventbus.Subscribe;
import com.sk89q.eduardo.util.formatting.Style;
import com.sk89q.intake.Command;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.Require;
import com.sk89q.intake.context.CommandContext;
import com.sk89q.intake.parametric.annotation.Text;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import static com.sk89q.eduardo.util.formatting.StyledFragment.with;
import static org.jooq.impl.DSL.field;

@AutoRegister
@Singleton
public class AliasManager {

    private static final Logger log = LoggerFactory.getLogger(AliasManager.class);
    private static final String ID = "alias";
    private static final int MAX_DEPTH = 5;

    private final Bucket bucket;
    private final Table<?> aliasTable;
    @Inject private EventBus eventBus;
    @Inject private CommandManager commandManager;

    @Inject
    public AliasManager(Persistence persistence) {
        bucket = persistence.connect(ID);
        aliasTable = bucket.table("aliases");
    }

    @Nullable
    public ResolvedAlias resolveAlias(String alias, Context inheritFrom) {
        try {
            try (Connection conn = bucket.createConnection()) {
                DSLContext create = DSL.using(conn);

                Result<Record> record = create.select()
                        .from(aliasTable)
                        .where(field("alias").eq(alias.toLowerCase()))
                        .fetch();

                if (record.isEmpty()) {
                    return null;
                } else {
                    String command = (String) record.get(0).getValue("command");
                    return new ResolvedAlias(command, inheritFrom);
                }
            }
        } catch (SQLException e) {
            log.warn("Failed to get alias due to an error", e);
            return null;
        }
    }

    @Subscribe(priority = Priority.LATE, ignoreCancelled = true)
    public void onCommandEvent(CommandEvent event) {
        int newDepth = event.getDepth() + 1;

        String[] split = CommandContext.split(event.getArguments());
        String command = split[0];
        boolean query = false;

        if (command.length() > 1 && command.endsWith("?")) {
            command = command.substring(0, command.length() - 1);
            query = true;
        }

        ResolvedAlias resolved = resolveAlias(command, event.getPrimaryContext());
        if (resolved != null) {
            event.setCancelled(true);

            if (query) {
                event.getResponse().respond("Alias set to: " + resolved);
            } else {
                String arguments = Joiner.on(" ").join(Arrays.copyOfRange(split, 1, split.length));
                String target = resolved.getCommand().replace("$*", arguments);
                CommandEvent commandEvent = new CommandEvent(event.getPrimaryContext(), target, event.getResponse(), newDepth);
                commandEvent.getContexts().addAll(event.getContexts());
                eventBus.post(commandEvent);
            }
        }
    }

    @Command(aliases = "alias", desc = "Define an alias")
    @Require("alias.create")
    @RateLimit
    public void create(Context context, Response response, String alias, @Text String command) throws SQLException, CommandException, JsonProcessingException {
        alias = commandManager.removePrefix(alias);
        command = commandManager.removePrefix(command);

        Room room = context.getRoom();
        if (room == null) {
            throw new CommandException("Aliases only work on rooms");
        }

        try (Connection conn = bucket.createConnection()) {
            DSLContext create = DSL.using(conn);
            doCreate(create, context, alias, command);
        }

        response.respond(with()
                .append("Created the alias ")
                .append(with(Style.BOLD).append(alias))
                .append("."));
    }

    @Command(aliases = "unalias", desc = "Remove an alias")
    @Require("alias.remove")
    @RateLimit
    public void remove(Context context, Response response, String alias) throws SQLException, CommandException {
        alias = commandManager.removePrefix(alias);

        Room room = context.getRoom();
        if (room == null) {
            throw new CommandException("Aliases only work on rooms");
        }

        try (Connection conn = bucket.createConnection()) {
            DSLContext create = DSL.using(conn);
            doDelete(create, context, alias);
        }

        response.respond(with()
                .append("Deleted the alias ")
                .append(with(Style.BOLD).append(alias))
                .append(" if it existed."));
    }

    private void doDelete(DSLContext create, Context context, String alias) {
        create.delete(aliasTable)
                .where(field("network").eq(context.getNetwork().getId().toLowerCase()))
                .and(field("channel").eq(context.getRoom().getId().toLowerCase()))
                .and(field("alias").eq(alias.toLowerCase()))
                .execute();
    }

    private void doCreate(DSLContext create, Context context, String alias, String command) throws JsonProcessingException {
        doDelete(create, context, alias);
        create.insertInto(aliasTable,
                field("network"),
                field("channel"),
                field("alias"),
                field("command"))
                .values(context.getNetwork().getId().toLowerCase(),
                        context.getRoom().getId().toLowerCase(),
                        alias.toLowerCase(),
                        command)
                .execute();
    }

    public static class ResolvedAlias {
        private final String command;
        private final Context context;

        public ResolvedAlias(String command, Context context) {
            this.command = command;
            this.context = context;
        }

        public String getCommand() {
            return command;
        }

        public Context getContext() {
            return context;
        }
    }

}
