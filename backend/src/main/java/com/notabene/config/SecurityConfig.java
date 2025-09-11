package com.notabene.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        
        cfg.setAllowedOrigins(java.util.List.of("http://localhost:3000"));
        cfg.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","OPTIONS"));
    
        cfg.setAllowedHeaders(java.util.List.of("Content-Type","X-Auth-Token","Authorization"));
        cfg.setExposedHeaders(java.util.List.of("X-Auth-Token"));
        cfg.setAllowCredentials(false);
        cfg.setMaxAge(1800L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TokenAuthenticationFilter tokenAuthenticationFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // preflight
                .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
