package com.groupsoft.piedrazul.infrastructure.security;

import com.groupsoft.piedrazul.infrastructure.web.GlobalExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    Converter<Jwt, AbstractAuthenticationToken> keycloakRealmRoleConverter() {
        return new KeycloakRealmRoleConverter();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            GlobalExceptionHandler errors,
            CorsConfigurationSource corsConfigurationSource,
            Converter<Jwt, AbstractAuthenticationToken> keycloakRealmRoleConverter
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/patients/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/appointments/doctor/**")
                            .hasAnyRole("SCHEDULER", "DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/appointments/patient")
                            .hasAnyRole("SCHEDULER", "DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/patients/suggest")
                            .hasAnyRole("SCHEDULER", "DOCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/appointments").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/doctors/*/slots").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/doctors")
                            .hasAnyRole("SCHEDULER", "DOCTOR", "PATIENT", "ADMINISTRATOR")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/**").denyAll()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakRealmRoleConverter)));
        return http.build();
    }
}
