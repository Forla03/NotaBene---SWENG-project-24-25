// src/main/java/com/notabene/service/support/JdbcCurrentUserResolver.java
package com.notabene.service.support;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class JdbcCurrentUserResolver implements CurrentUserResolver {

    private final JdbcTemplate jdbc;

    public JdbcCurrentUserResolver(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public Long currentUserId() {
        String p = principal();
        // 1) try by email (if principal is an email)
        Long id = jdbc.query("select id from users where email = ?",
                rs -> rs.next() ? rs.getLong(1) : null, p);
        if (id != null) return id;

        // 2) fallback per username
        id = jdbc.query("select id from users where username = ? limit 1",
                rs -> rs.next() ? rs.getLong(1) : null, p);
        if (id != null) return id;

        // Non trovato → 401, non 500
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Authenticated user not found: " + p);
    }

    @Override
    public String principal() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getName() == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authentication");
        return a.getName();
    }
}


