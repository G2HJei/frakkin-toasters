package xyz.zlatanov.frakkintoasters.backend.user;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class Users {

    private final JdbcClient          jdbc;
    private final TransactionTemplate transaction;

    /**
     * Finds the user of the identity, or creates him on his first login.
     *
     * @param provider    e.g. {@code google}, {@code discord} or {@code dev}
     * @param subject     the provider's id of the user
     * @param defaultName the name of a new user
     */
    public AppUser login(String provider, String subject, String defaultName) {
        val existing = findByIdentity(provider, subject);
        if (existing.isPresent()) {
            return existing.get();
        }
        try {
            return transaction.execute(ignored -> {
                val id = UUID.randomUUID();
                jdbc.sql("INSERT INTO app_user (id, name) VALUES (:id, :name)")
                        .param("id", id)
                        .param("name", defaultName)
                        .update();
                jdbc.sql("INSERT INTO user_identity (provider, subject, user_id) VALUES (:provider, :subject, :id)")
                        .param("provider", provider)
                        .param("subject", subject)
                        .param("id", id)
                        .update();
                return find(id).orElseThrow();
            });
        } catch (DuplicateKeyException e) {
            // the same identity logged in twice at once
            return findByIdentity(provider, subject).orElseThrow();
        }
    }

    public Optional<AppUser> find(UUID id) {
        return jdbc.sql("SELECT id, name, settings::text AS settings FROM app_user WHERE id = :id")
                .param("id", id)
                .query((row, ignored) -> new AppUser(
                        row.getObject("id", UUID.class),
                        row.getString("name"),
                        JournalCodec.parse(row.getString("settings"))))
                .optional();
    }

    public AppUser update(UUID id, String name, JsonNode settings) {
        jdbc.sql("UPDATE app_user SET name = :name, settings = :settings::jsonb WHERE id = :id")
                .param("id", id)
                .param("name", name)
                .param("settings", JournalCodec.stringify(settings))
                .update();
        return find(id).orElseThrow();
    }

    private Optional<AppUser> findByIdentity(String provider, String subject) {
        return jdbc.sql("SELECT user_id FROM user_identity WHERE provider = :provider AND subject = :subject")
                .param("provider", provider)
                .param("subject", subject)
                .query(UUID.class)
                .optional()
                .flatMap(this::find);
    }
}
