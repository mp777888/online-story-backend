package com.onlinestories.api_gateway.Config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // Lấy claim realm_access
        Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");

        // Nếu không có realm_access hoặc không có roles, trả về list rỗng
        if (realmAccess == null || realmAccess.isEmpty()) {
            return List.of();
        }

        // Lấy danh sách roles
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");

        // Map roles thành GrantedAuthority với tiền tố ROLE_
        return roles.stream()
                .map(roleName -> "ROLE_" + roleName) // Ví dụ: admin -> ROLE_admin
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
