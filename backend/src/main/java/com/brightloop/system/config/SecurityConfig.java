package com.brightloop.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Enterprise Spring Security Configuration with JWT Authentication Filter.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public health check and static assets
                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                .requestMatchers("/", "/index.html", "/pages/**", "/css/**", "/js/**", "/favicon.ico", "/error").permitAll()
                // Public auth endpoints
                .requestMatchers(HttpMethod.POST,
                        "/api/auth/login",
                        "/api/auth/signup",
                        "/api/auth/forgot-password",
                        "/api/auth/reset-password").permitAll()
                // Preflight OPTIONS requests for CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Staff creation restricted strictly to ADMIN
                .requestMatchers(HttpMethod.POST, "/api/auth/create-staff").hasRole("ADMIN")
                // Discount modification restricted to ADMIN
                .requestMatchers(HttpMethod.POST, "/api/discounts").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/discounts/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/discounts/**").hasRole("ADMIN")
                // All other API requests require valid authentication
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
