package xyz.zlatanov.frakkintoasters.backend.user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * The {@code dev} profile's login per browser tab, so one browser plays several users side by side. Nothing goes into
 * the HTTP session, which the browser's other tabs share. Like the dev login, it can't be on with a real provider.
 */
@Profile("dev")
@Component
public class DevTabLogin extends OncePerRequestFilter {

    public static final String HEADER    = "X-Dev-User";
    public static final String PARAMETER = "devUser";

    private final Users users;

    public DevTabLogin(Users users, LoginProviders providers) {
        if (!providers.registrations().isEmpty()) {
            throw new IllegalStateException("The dev login can't be on with a real login provider");
        }
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        val header = request.getHeader(HEADER);
        val name = header != null ? header : request.getParameter(PARAMETER);
        if (name == null || name.isBlank()) {
            chain.doFilter(request, response);
            return;
        }
        if (name.strip().length() > 40) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "A name has 1 to 40 characters");
            return;
        }
        val user = users.login("dev", name.strip(), null, name.strip());
        val context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new AppUserAuthentication(user.id()));
        val before = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(context);
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.setContext(before);
        }
    }
}
