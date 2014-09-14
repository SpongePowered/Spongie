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

package com.sk89q.eduardo.auth;

import com.sk89q.eduardo.irc.IrcContext;
import org.junit.Test;

import static com.sk89q.eduardo.irc.ChannelUserMode.OPERATOR;
import static com.sk89q.eduardo.irc.ChannelUserMode.VOICED;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ContextMatchTest {

    @Test
    public void testTest_User() throws Exception {
        ContextMatch match = new ContextMatch();
        match.matchUser("test!*@*");
        assertThat(match.test(new IrcContext("test!example@example.com", "chan1")), is(true));
        assertThat(match.test(new IrcContext("test!example@example.com", "chan2")), is(true));
        assertThat(match.test(new IrcContext("someone!example@example.com", "chan1")), is(false));
        assertThat(match.test(new IrcContext("someone!example@example.com", "chan2")), is(false));
        assertThat(match.test(new IrcContext("someone!test@example.com", "chan1")), is(false));
        assertThat(match.test(new IrcContext("someone!test@example.com", "chan2")), is(false));
    }

    @Test
    public void testTest_Channel() throws Exception {
        ContextMatch match = new ContextMatch();
        match.matchChannel("chan1");
        assertThat(match.test(new IrcContext("test!example@example.com", "chan1")), is(true));
        assertThat(match.test(new IrcContext("test!example@example.com", "chan2")), is(false));
        assertThat(match.test(new IrcContext("someone!example@example.com", "chan1")), is(true));
        assertThat(match.test(new IrcContext("someone!example@example.com", "chan2")), is(false));
        assertThat(match.test(new IrcContext("someone!test@example.com", "chan1")), is(true));
        assertThat(match.test(new IrcContext("someone!test@example.com", "chan2")), is(false));
    }

    @Test
    public void testTest_Mode() throws Exception {
        ContextMatch match = new ContextMatch();
        match.matchMode(VOICED);
        assertThat(match.test(new IrcContext("test!example@example.com", "chan1")), is(false));
        assertThat(match.test(new IrcContext("test!example@example.com", "chan2")), is(false));
        assertThat(match.test(new IrcContext("test!example@example.com", "chan2", VOICED)), is(true));
        assertThat(match.test(new IrcContext("test!example@example.com", "chan2", OPERATOR)), is(false));
        assertThat(match.test(new IrcContext("someone!example@example.com", "chan1")), is(false));
        assertThat(match.test(new IrcContext("someone!example@example.com", "chan2")), is(false));
        assertThat(match.test(new IrcContext("someone!example@example.com", "chan2", OPERATOR)), is(false));
        assertThat(match.test(new IrcContext("someone!test@example.com", "chan1")), is(false));
        assertThat(match.test(new IrcContext("someone!test@example.com", "chan2")), is(false));
        assertThat(match.test(new IrcContext("someone!test@example.com", "chan2", OPERATOR)), is(false));
    }

    @Test
    public void testTest_UserChannel() throws Exception {
        ContextMatch match = new ContextMatch();
        match.matchUser("test!*@*");
        match.matchChannel("chan1");
        assertThat(match.test(new IrcContext("test!example@example.com", "chan1")), is(true));
        assertThat(match.test(new IrcContext("test!example@example.com", "chan2")), is(false));
        assertThat(match.test(new IrcContext("someone!example@example.com", "chan1")), is(false));
        assertThat(match.test(new IrcContext("someone!example@example.com", "chan2")), is(false));
        assertThat(match.test(new IrcContext("someone!test@example.com", "chan1")), is(false));
        assertThat(match.test(new IrcContext("someone!test@example.com", "chan2")), is(false));
    }

}