package com.enterprise.news.security;

import com.enterprise.news.model.UserProfile;

public final class AuthContext {

    private static final ThreadLocal<UserProfile> CURRENT_USER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void setUser(UserProfile user) {
        CURRENT_USER.set(user);
    }

    public static UserProfile getUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}