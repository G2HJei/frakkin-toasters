package xyz.zlatanov.frakkintoasters.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginProviders providers;
    private final EmailLogin     emailLogin;
    private final Environment    environment;

    public record Logins(List<String> providers, boolean devLogin, boolean email) {
    }

    @GetMapping("/api/login/providers")
    public Logins logins() {
        return new Logins(providers.registrations().stream().map(ClientRegistration::getRegistrationId).toList(),
                environment.acceptsProfiles(Profiles.of("dev")), emailLogin.enabled());
    }
}
