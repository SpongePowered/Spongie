package com.sk89q.eduardo.util;

public final class IdUtil {

    public static String dashify(String id) {
        id = id.replace("-", "");

        return new StringBuilder(id)
            .insert(8, "-")
            .insert(13, "-")
            .insert(18, "-")
            .insert(23, "-")
            .toString();
    }
}
