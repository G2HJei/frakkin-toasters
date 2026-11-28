package xyz.zlatanov.frakkintoasters.backend.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The {@code dev} profile's login by name, so the application runs without client secrets: local play and the tests. It
 * can't be on with a real provider: the application refuses to start.
 */
@Profile("dev")
@RestController
public class DevLoginController {

    private final Users                     users;
    private final SecurityContextRepository contexts;

    public DevLoginController(Users users, SecurityContextRepository contexts, LoginProviders providers) {
        if (!providers.registrations().isEmpty()) {
            throw new IllegalStateException("The dev login can't be on with a real login provider");
        }
        this.users = users;
        this.contexts = contexts;
    }

    public record DevLogin(String name) {
    }

    @PostMapping("/api/dev/login")
    public AppUser login(@RequestBody DevLogin login, HttpServletRequest request, HttpServletResponse response) {
        if (login.name() == null || login.name().isBlank() || login.name().length() > 40) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A name has 1 to 40 characters");
        }
        var name = login.name().strip();
        var user = users.login("dev", name, null, name);
        SecurityConfiguration.logIn(user.id(), request, response, contexts);
        return user;
    }
}
