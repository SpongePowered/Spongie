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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.sk89q.eduardo.LoaderException;
import com.sk89q.eduardo.helper.AutoRegister;
import com.typesafe.config.Config;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
@AutoRegister
public class Persistence {

    private static final Pattern validId = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,19}$");
    private final Config thisConfig;
    private final ComboPooledDataSource dataSource;

    @Inject
    public Persistence(Config config) throws LoaderException {
        thisConfig = config.getConfig("persistence");

        try {
            dataSource = configureDataSource();
        } catch (PropertyVetoException e) {
            throw new LoaderException("Failed to create connection pool");
        }
    }

    private ComboPooledDataSource configureDataSource() throws PropertyVetoException {
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        cpds.setDriverClass(thisConfig.getString("jdbc.driver"));
        cpds.setJdbcUrl(thisConfig.getString("jdbc.url"));
        cpds.setUser(thisConfig.getString("jdbc.username"));
        cpds.setPassword(thisConfig.getString("jdbc.password"));
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
        return cpds;
    }

    public Bucket connect(String id) {
        checkNotNull(id);
        id = id.replace("-", "_");
        checkArgument(validId.matcher(id).matches(), "id is not valid");
        return new Bucket(this, id);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

}
