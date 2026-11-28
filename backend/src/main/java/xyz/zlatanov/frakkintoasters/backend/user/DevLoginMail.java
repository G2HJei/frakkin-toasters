package xyz.zlatanov.frakkintoasters.backend.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code dev} profile's login codes: written to the log, and kept for the tests. Like the dev login, it can't be on
 * with a real login: SMTP makes the email login one.
 */
@Slf4j
@Profile("dev")
@Component
public class DevLoginMail implements LoginMail {

    private final Map<String, String> sent = new ConcurrentHashMap<>();

    public DevLoginMail(@Value("${spring.mail.host:}") String mailHost) {
        if (!mailHost.isBlank()) {
            throw new IllegalStateException("The dev login can't be on with a real login provider");
        }
    }

    @Override
    public void send(String email, String code) {
        sent.put(email, code);
        log.info("Login code for {}: {}", email, code);
    }

    public Optional<String> lastCode(String email) {
        return Optional.ofNullable(sent.get(email));
    }
}
