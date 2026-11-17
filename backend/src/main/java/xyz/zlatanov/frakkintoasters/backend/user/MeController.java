package xyz.zlatanov.frakkintoasters.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import xyz.zlatanov.frakkintoasters.backend.user.AppUserAuthentication.AppUserPrincipal;

/**
 * "Register / update settings": the logged in user's name and settings.
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final Users users;

    public record MeUpdate(String name, JsonNode settings) {
    }

    @GetMapping
    public AppUser me(@AuthenticationPrincipal AppUserPrincipal principal) {
        return users.find(principal.id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @PutMapping
    public AppUser update(@AuthenticationPrincipal AppUserPrincipal principal, @RequestBody MeUpdate update) {
        if (update.name() == null || update.name().isBlank() || update.name().length() > 40) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A name has 1 to 40 characters");
        }
        var settings = update.settings() == null || !update.settings().isObject()
                ? JsonNodeFactory.instance.objectNode()
                : update.settings();
        return users.update(principal.id(), update.name().strip(), settings);
    }
}
