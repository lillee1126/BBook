package com.enterprise.news.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class UserDefaults {

    private UserDefaults() {
    }

    public static String avatar(String seed) {
        return "https://api.dicebear.com/9.x/notionists/svg?seed="
                + URLEncoder.encode(seed, StandardCharsets.UTF_8);
    }
}