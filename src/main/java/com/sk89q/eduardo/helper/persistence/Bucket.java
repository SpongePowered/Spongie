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

package com.sk89q.eduardo.helper.persistence;

import org.flywaydb.core.Flyway;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Bucket {

    private final Persistence persistence;
    private final String id;

    public Bucket(Persistence persistence, String id) {
        this.persistence = persistence;
        this.id = id;
        migrate();
    }

    public void migrate() {
        Map<String, String> placeHolders = new HashMap<String, String>();
        placeHolders.put("tablePrefix", id + "_");

        Flyway flyway = new Flyway();
        flyway.setLocations("migrations/" + id);
        flyway.setClassLoader(getClass().getClassLoader());
        flyway.setDataSource(persistence.getDataSource());
        flyway.setTable(id + "_migrations");
        flyway.setPlaceholders(placeHolders);
        flyway.setValidateOnMigrate(false);
        flyway.migrate();
    }

    public Connection createConnection() throws SQLException {
        return persistence.getDataSource().getConnection();
    }

    public <V> Table<Record> table(String name) {
        return DSL.table(id + "_" + name);
    }

}
