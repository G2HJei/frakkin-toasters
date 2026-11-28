package xyz.zlatanov.frakkintoasters.backend.user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.UUID;

/**
 * A server-side HTTP session, whose cookie the web socket handshake uses too. CSRF protection stays on for the REST
 * calls: the client sends the {@code XSRF-TOKEN} cookie back in the {@code X-XSRF-TOKEN} header.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LoginProviders.class)
public class SecurityConfiguration {

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    // The dev login per tab belongs in the security chain alone, after the context is loaded from the session: Boot would
    // put a filter bean in the servlet chain too, where the session's context would replace its user.
    @Bean
    @Profile("dev")
    FilterRegistrationBean<DevTabLogin> devTabLoginRegistration(DevTabLogin filter) {
        val registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, LoginProviders providers, Users users,
                                            SecurityContextRepository contexts,
                                            ObjectProvider<DevTabLogin> devTabLogin) throws Exception {
        val unauthorized = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
        http
                .securityContext(context -> context.securityContextRepository(contexts))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/dev/login").permitAll()
                        .requestMatchers("/api/login/**").permitAll()
                        .requestMatchers("/api/schema").permitAll()
                        .requestMatchers("/api/**", "/ws/**").authenticated()
                        .anyRequest().permitAll())
                .csrf(csrf -> csrf.spa())
                // the token is deferred: loading it on every request puts the cookie in the browser before its first POST
                .addFilterAfter(new OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                                    FilterChain chain) throws ServletException, IOException {
                        if (request.getAttribute(CsrfToken.class.getName()) instanceof CsrfToken token) {
                            token.getToken();
                        }
                        chain.doFilter(request, response);
                    }
                }, CsrfFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(unauthorized, PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                        .defaultAuthenticationEntryPointFor(unauthorized, PathPatternRequestMatcher.withDefaults().matcher("/ws/**")))
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()));
        devTabLogin.ifAvailable(filter -> http.addFilterAfter(filter, SecurityContextHolderFilter.class));
        val registrations = providers.registrations();
        if (!registrations.isEmpty()) {
            http.oauth2Login(login -> login
                    .loginPage("/login") // the app's page, not one Spring makes
                    .clientRegistrationRepository(new InMemoryClientRegistrationRepository(registrations))
                    .successHandler((request, response, authentication) -> {
                        val token = (OAuth2AuthenticationToken) authentication;
                        val provider = token.getAuthorizedClientRegistrationId();
                        val email = LoginProviders.verifiedEmail(provider, token.getPrincipal().getAttributes());
                        if (email.isEmpty()) {
                            // the filter kept the provider's login in the session: nobody is logged in after all
                            val empty = SecurityContextHolder.createEmptyContext();
                            SecurityContextHolder.setContext(empty);
                            contexts.saveContext(empty, request, response);
                            response.sendRedirect("/login?error=unverified-email");
                            return;
                        }
                        val user = users.login(provider, token.getName(), email.get(), defaultName());
                        logIn(user.id(), request, response, contexts);
                        // to the games, as the dev login goes
                        response.sendRedirect("/play");
                    }));
        }
        return http.build();
    }

    static void logIn(UUID userId, HttpServletRequest request, HttpServletResponse response, SecurityContextRepository contexts) {
        // a session from before the login gets a new id, against session fixation; saving the context creates one otherwise
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        val context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new AppUserAuthentication(userId));
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
    }

    // A user's email is shown to nobody else, so a new user gets a name to change in his settings.
    static String defaultName() {
        return "Pilot " + (1000 + (int) (Math.random() * 9000));
    }
}
