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

package com.sk89q.eduardo.auth.policy;

import com.sk89q.eduardo.auth.policy.MultiMapPolicy.Result;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MultiMapPolicyTest {

    @Test
    public void testTestPermission_Grant() throws Exception {
        MultiMapPolicy<Object> policy = new MultiMapPolicy<>();
        policy.grant("parent.child", o -> true);
        assertThat(policy.testPermission("parent.child", null), is(true));
        assertThat(policy.testPermission("parent.child.child", null), is(true));
        assertThat(policy.testPermission("parent.other", null), is(false));
        assertThat(policy.testPermission("parent.other.child", null), is(false));
        assertThat(policy.testPermission("parent", null), is(false));
        assertThat(policy.testPermission("other", null), is(false));
        assertThat(policy.testPermission("other.child", null), is(false));
    }

    @Test
    public void testTestPermission_Deny() throws Exception {
        MultiMapPolicy<Object> policy = new MultiMapPolicy<>();
        policy.deny("parent.child", o -> true);
        assertThat(policy.testPermission("parent.child", null), is(false));
        assertThat(policy.testPermission("parent.child.child", null), is(false));
        assertThat(policy.testPermission("parent.other", null), is(false));
        assertThat(policy.testPermission("parent.other.child", null), is(false));
        assertThat(policy.testPermission("parent", null), is(false));
        assertThat(policy.testPermission("other", null), is(false));
        assertThat(policy.testPermission("other.child", null), is(false));
    }

    @Test
    public void testTestPermission_GrantDeny() throws Exception {
        MultiMapPolicy<Object> policy = new MultiMapPolicy<>();
        policy.grant("parent.child", o -> true);
        policy.deny("parent.child", o -> true);
        assertThat(policy.testPermission("parent.child", null), is(false));
        assertThat(policy.testPermission("parent.child.child", null), is(false));
        assertThat(policy.testPermission("parent.other", null), is(false));
        assertThat(policy.testPermission("parent.other.child", null), is(false));
        assertThat(policy.testPermission("parent", null), is(false));
        assertThat(policy.testPermission("other", null), is(false));
        assertThat(policy.testPermission("other.child", null), is(false));
    }

    @Test
    public void testTestPermission_GrantChildDenyParent() throws Exception {
        MultiMapPolicy<Object> policy = new MultiMapPolicy<>();
        policy.grant("parent.child", o -> true);
        policy.deny("parent", o -> true);
        assertThat(policy.testPermission("parent.child", null), is(false));
        assertThat(policy.testPermission("parent.child.child", null), is(false));
        assertThat(policy.testPermission("parent.other", null), is(false));
        assertThat(policy.testPermission("parent.other.child", null), is(false));
        assertThat(policy.testPermission("parent", null), is(false));
        assertThat(policy.testPermission("other", null), is(false));
        assertThat(policy.testPermission("other.child", null), is(false));
    }

    @Test
    public void testTestPermission_GrantParentDenyChild() throws Exception {
        MultiMapPolicy<Object> policy = new MultiMapPolicy<>();
        policy.deny("parent.child", o -> true);
        policy.grant("parent", o -> true);
        assertThat(policy.testPermission("parent.child", null), is(false));
        assertThat(policy.testPermission("parent.child.child", null), is(false));
        assertThat(policy.testPermission("parent.other", null), is(true));
        assertThat(policy.testPermission("parent.other.child", null), is(true));
        assertThat(policy.testPermission("parent", null), is(true));
        assertThat(policy.testPermission("other", null), is(false));
        assertThat(policy.testPermission("other.child", null), is(false));
    }

    @Test
    public void testTestPermission_GrantWildcard() throws Exception {
        MultiMapPolicy<Object> policy = new MultiMapPolicy<>();
        policy.grant("*", o -> true);
        assertThat(policy.testPermission("parent.child", null), is(true));
        assertThat(policy.testPermission("parent.child.child", null), is(true));
        assertThat(policy.testPermission("parent.other", null), is(true));
        assertThat(policy.testPermission("parent.other.child", null), is(true));
        assertThat(policy.testPermission("parent", null), is(true));
        assertThat(policy.testPermission("other", null), is(true));
        assertThat(policy.testPermission("other.child", null), is(true));
    }

    @Test
    public void testTestPermission_DenyWildcard() throws Exception {
        MultiMapPolicy<Object> policy = new MultiMapPolicy<>();
        policy.deny("*", o -> true);
        assertThat(policy.testPermission("parent.child", null), is(false));
        assertThat(policy.testPermission("parent.child.child", null), is(false));
        assertThat(policy.testPermission("parent.other", null), is(false));
        assertThat(policy.testPermission("parent.other.child", null), is(false));
        assertThat(policy.testPermission("parent", null), is(false));
        assertThat(policy.testPermission("other", null), is(false));
        assertThat(policy.testPermission("other.child", null), is(false));
    }

    @Test
    public void testTestPermission_DenyWildcardGrantChild() throws Exception {
        MultiMapPolicy<Object> policy = new MultiMapPolicy<>();
        policy.deny("*", o -> true);
        policy.grant("parent", o -> true);
        assertThat(policy.testPermission("parent.child", null), is(false));
        assertThat(policy.testPermission("parent.child.child", null), is(false));
        assertThat(policy.testPermission("parent.other", null), is(false));
        assertThat(policy.testPermission("parent.other.child", null), is(false));
        assertThat(policy.testPermission("parent", null), is(false));
        assertThat(policy.testPermission("other", null), is(false));
        assertThat(policy.testPermission("other.child", null), is(false));
    }

    @Test
    public void testTestPermission_GrantWildcardDenyChild() throws Exception {
        MultiMapPolicy<Object> policy = new MultiMapPolicy<>();
        policy.deny("parent.child", o -> true);
        policy.grant("*", o -> true);
        assertThat(policy.testPermission("parent.child", null), is(false));
        assertThat(policy.testPermission("parent.child.child", null), is(false));
        assertThat(policy.testPermission("parent.other", null), is(true));
        assertThat(policy.testPermission("parent.other.child", null), is(true));
        assertThat(policy.testPermission("parent", null), is(true));
        assertThat(policy.testPermission("other", null), is(true));
        assertThat(policy.testPermission("other.child", null), is(true));
    }

    @Test
    public void testGetResult_None() throws Exception {
        MultiMapPolicy<Object> policy = new MultiMapPolicy<>();
        assertThat(policy.getResult("parent.child", null), is(Result.NONE));
        assertThat(policy.getResult("parent", null), is(Result.NONE));
        assertThat(policy.getResult("other", null), is(Result.NONE));
        assertThat(policy.getResult("other.child", null), is(Result.NONE));
    }

    @Test
    public void testGetResult_ChildGrant() throws Exception {
        MultiMapPolicy<Object> policy = new MultiMapPolicy<>();
        policy.grant("parent.child", o -> true);
        assertThat(policy.getResult("parent.child", null), is(Result.ALLOW));
        assertThat(policy.getResult("parent.other", null), is(Result.NONE));
        assertThat(policy.getResult("parent.child.child", null), is(Result.NONE));
        assertThat(policy.getResult("parent", null), is(Result.NONE));
        assertThat(policy.getResult("other", null), is(Result.NONE));
        assertThat(policy.getResult("other.child", null), is(Result.NONE));
    }

    @Test
    public void testGetResult_ChildDeny() throws Exception {
        MultiMapPolicy<Object> policy = new MultiMapPolicy<>();
        policy.deny("parent.child", o -> true);
        assertThat(policy.getResult("parent.child", null), is(Result.DENY));
        assertThat(policy.getResult("parent.other", null), is(Result.NONE));
        assertThat(policy.getResult("parent.child.child", null), is(Result.NONE));
        assertThat(policy.getResult("parent", null), is(Result.NONE));
        assertThat(policy.getResult("other", null), is(Result.NONE));
        assertThat(policy.getResult("other.child", null), is(Result.NONE));
    }

    @Test
    public void testGetResult_ChildGrantDeny() throws Exception {
        MultiMapPolicy<Object> policy = new MultiMapPolicy<>();
        policy.grant("parent.child", o -> true);
        policy.deny("parent.child", o -> true);
        assertThat(policy.getResult("parent.child", null), is(Result.DENY));
        assertThat(policy.getResult("parent.other", null), is(Result.NONE));
        assertThat(policy.getResult("parent.child.child", null), is(Result.NONE));
        assertThat(policy.getResult("parent", null), is(Result.NONE));
        assertThat(policy.getResult("other", null), is(Result.NONE));
        assertThat(policy.getResult("other.child", null), is(Result.NONE));
    }

    @Test
    public void testGetResult_MultipleChildrenGrant() throws Exception {
        MultiMapPolicy<Object> policy = new MultiMapPolicy<>();
        policy.grant("parent.child", o -> true);
        policy.grant("parent.another", o -> true);
        assertThat(policy.getResult("parent.child", null), is(Result.ALLOW));
        assertThat(policy.getResult("parent.another", null), is(Result.ALLOW));
        assertThat(policy.getResult("parent.other", null), is(Result.NONE));
        assertThat(policy.getResult("parent.child.child", null), is(Result.NONE));
        assertThat(policy.getResult("parent", null), is(Result.NONE));
        assertThat(policy.getResult("other", null), is(Result.NONE));
        assertThat(policy.getResult("other.child", null), is(Result.NONE));
    }

}