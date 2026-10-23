package com.authguard.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Turns this service into an OAuth2 Resource Server: every request must carry a JWT
 * that Spring validates against Keycloak's public keys (signature, expiry, issuer).
 * A missing/invalid token gets 401. A valid token without the right permission gets 403:
 * the service layer calls {@code AuthorizationService.requirePermission(...)}, which throws
 * {@code AccessDeniedException} — Spring Security's {@code ExceptionTranslationFilter} turns
 * that into a 403 response automatically. A valid token alone is authentication, not authorization.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                // No cookies/session involved, so CSRF protection (which guards session-based
                // browser flows) does not apply to a stateless, token-authenticated API.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
