package com.campusexchange.config;

import com.campusexchange.controller.web.AuthController;
import com.campusexchange.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.campusexchange.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ObjectMapper errorMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * API chain, Order(1), for /api/**: HTTP Basic, no CSRF (non-browser clients),
     * JSON 401/403 bodies instead of redirects to the login page.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // /api/devices/mine must be matched BEFORE the public /api/devices/* rule.
                .requestMatchers(HttpMethod.GET, "/api/devices/mine").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/devices", "/api/devices/*").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> basic.authenticationEntryPoint(apiAuthenticationEntryPoint()))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(apiAuthenticationEntryPoint())
                .accessDeniedHandler(apiAccessDeniedHandler())
            );

        return http.build();
    }

    /**
     * Web chain, Order(2): form login with session, CSRF on all forms,
     * public browsing routes, everything else requires sign-in.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http,
                                                      CustomUserDetailsService userDetailsService,
                                                      @Value("${app.remember-me.key:${REMEMBER_ME_KEY:campus-exchange-dev-key}}") String rememberMeKey,
                                                      @Value("${spring.h2.console.enabled:false}") boolean h2Console)
            throws Exception {
        if (h2Console) {
            // Dev-only database console: needs frames and has its own forms.
            http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        }
        http
            .authorizeHttpRequests(auth -> {
                // Creation forms need sign-in; any other /books/* or /devices/* GET is public
                // (unknown ids then 404 instead of bouncing to the login page).
                auth.requestMatchers(HttpMethod.GET, "/books/new", "/devices/new").authenticated();
                auth.requestMatchers(HttpMethod.GET, "/books/*", "/devices/*").permitAll();
                auth.requestMatchers(
                    "/",
                    "/books",
                    "/books/{id:[0-9]+}",
                    "/devices",
                    "/devices/{id:[0-9]+}",
                    "/register",
                    "/login",
                    "/error",
                    "/css/**",
                    "/js/**",
                    "/fonts/**",
                    "/images/**",
                    "/favicon.ico",
                    "/favicon.svg"
                ).permitAll();
                if (h2Console) {
                    auth.requestMatchers("/h2-console/**").permitAll();
                }
                auth.anyRequest().authenticated();
            })
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", false)
                .failureHandler(rememberEmailOnFailure())
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key(rememberMeKey)
                .userDetailsService(userDetailsService)
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds(14 * 24 * 60 * 60)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
            )
            .headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .referrerPolicy(ref -> ref.policy(
                    org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
            );

        return http.build();
    }

    /** Remembers the typed email in the session so the login form can keep it after a failed attempt. */
    private AuthenticationFailureHandler rememberEmailOnFailure() {
        return (request, response, exception) -> {
            String email = request.getParameter("username");
            if (email != null && email.length() <= 150) {
                request.getSession().setAttribute(AuthController.LAST_LOGIN_EMAIL, email.trim());
            }
            response.sendRedirect(request.getContextPath() + "/login?error");
        };
    }

    private AuthenticationEntryPoint apiAuthenticationEntryPoint() {
        return (request, response, authException) -> writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized", "Sign in with HTTP Basic (email and password) to use this endpoint",
                request.getRequestURI());
    }

    private AccessDeniedHandler apiAccessDeniedHandler() {
        return (request, response, ex) -> writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                "Forbidden", "You do not have permission to perform this action", request.getRequestURI());
    }

    private void writeJson(HttpServletResponse response, int status, String error, String message, String path)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        errorMapper.writeValue(response.getWriter(), new ApiErrorResponse(status, error, message, path));
    }
}
