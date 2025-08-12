package com.notabene.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_PATHS = List.of("/api/auth/register", "/api/auth/login");

    private final TokenStore tokenStore;

    public TokenAuthenticationFilter(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Permetti richieste OPTIONS per CORS preflight
        if ("OPTIONS".equals(method)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Permetti percorsi pubblici
        if (PUBLIC_PATHS.contains(path)) { 
            chain.doFilter(request, response); 
            return; 
        }

        String token = request.getHeader("X-Auth-Token");
        if (token != null && tokenStore.isValid(token)) {
            String username = tokenStore.getUsername(token);
            var auth = new UsernamePasswordAuthenticationToken(username, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing token");
        }
    }
}
