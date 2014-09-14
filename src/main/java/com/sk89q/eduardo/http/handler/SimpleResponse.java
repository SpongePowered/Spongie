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

package com.sk89q.eduardo.http.handler;

import javax.servlet.http.HttpServletResponse;

public class SimpleResponse {

    private int response = HttpServletResponse.SC_OK;
    private String contentType = "text/html; charset=utf-8";
    private byte[] body = new byte[0];

    private SimpleResponse() {
    }

    public SimpleResponse response(int response) {
        this.response = response;
        return this;
    }

    public SimpleResponse contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public SimpleResponse body(byte[] body) {
        this.body = body;
        return this;
    }

    public SimpleResponse body(String body) {
        return body(body.getBytes());
    }

    public int getResponse() {
        return response;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getBody() {
        return body;
    }

    public static SimpleResponse create() {
        return new SimpleResponse();
    }

}
