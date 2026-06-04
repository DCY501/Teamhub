package com.teamhub.teamhub.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    public static AuthUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthUser) {
            return (AuthUser) auth.getPrincipal();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        AuthUser user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static boolean isCaptain() {
        AuthUser user = getCurrentUser();
        return user != null && "CAPTAIN".equals(user.getRole());
    }
}
