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

package com.sk89q.eduardo;

import com.google.common.eventbus.EventBus;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.sk89q.eduardo.helper.AutoRegister;
import com.sk89q.eduardo.helper.command.CommandManager;

class AutoRegisterListener implements TypeListener {

    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        if (type.getRawType().getAnnotation(AutoRegister.class) != null) {
            EventBus eventBus = encounter.getProvider(EventBus.class).get();
            CommandManager commandManager = encounter.getProvider(CommandManager.class).get();

            encounter.register(new InjectionListener<I>() {
                @Override
                public void afterInjection(I injectee) {
                    eventBus.register(injectee);
                    commandManager.registerCommands(injectee);
                }
            });
        }
    }

}
