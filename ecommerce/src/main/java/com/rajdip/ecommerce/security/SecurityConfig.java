package com.rajdip.ecommerce.security;

import com.rajdip.ecommerce.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/users/register", "/users/login", "/users/validate-token").permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs").permitAll()
                    .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                    .requestMatchers("/users/refresh-token", "/users/logout", "/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/products/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN")
                        .requestMatchers("/orders/**").authenticated()
                        .requestMatchers("/cart/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/payments").hasRole("ADMIN")
                        .requestMatchers("/payments/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/categories/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/categories").authenticated()
                        .requestMatchers(HttpMethod.POST, "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/categories/**").hasRole("ADMIN")
                        .requestMatchers("/reviews/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmail(username)
                .map(user -> User.withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles(normalizeRole(user.getRole()))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "CUSTOMER";
        }

        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return normalized;
    }
}