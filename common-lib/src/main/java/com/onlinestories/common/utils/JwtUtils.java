package com.onlinestories.common.utils;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Map;

public class JwtUtils {

    public static boolean isAdmin(Jwt jwt) {
        if (jwt == null) {
            return false;
        }

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            return roles != null && roles.contains("ADMIN");
        }

        return false;
    }
}
