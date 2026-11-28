package xyz.zlatanov.frakkintoasters.backend.user;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * An email is stored lowercased, so each address is one user whichever way it logs in.
 */
@Repository
@RequiredArgsConstructor
public class Users {

    public record Listed(UUID id, String name) {
    }

    private final JdbcClient          jdbc;
    private final TransactionTemplate transaction;

    /**
     * The user of a provider's identity; a first login joins the user with the same verified email, or creates one.
     * A later change of the email at the provider leaves the account as it is.
     */
    public AppUser login(String provider, String subject, String email, String defaultName) {
        val existing = findByIdentity(provider, subject);
        if (existing.isPresent()) {
            return existing.get();
        }
        try {
            return linkOrCreate(provider, subject, email, defaultName);
        } catch (DuplicateKeyException e) {
            // the same identity or email logged in twice at once: now the other login's rows are there
            return findByIdentity(provider, subject).orElseGet(() -> linkOrCreate(provider, subject, email, defaultName));
        }
    }

    public AppUser byEmail(String email, String defaultName) {
        jdbc.sql("INSERT INTO app_user (id, name, email) VALUES (:id, :name, :email) ON CONFLICT (email) DO NOTHING")
                .param("id", UUID.randomUUID())
                .param("name", defaultName)
                .param("email", lowercase(email))
                .update();
        return findByEmail(email).orElseThrow();
    }

    public Optional<AppUser> findByEmail(String email) {
        return jdbc.sql("SELECT id FROM app_user WHERE email = :email")
                .param("email", lowercase(email))
                .query(UUID.class)
                .optional()
                .flatMap(this::find);
    }

    public Optional<AppUser> find(UUID id) {
        return jdbc.sql("SELECT id, name, email, settings::text AS settings FROM app_user WHERE id = :id")
                .param("id", id)
                .query((row, ignored) -> new AppUser(
                        row.getObject("id", UUID.class),
                        row.getString("name"),
                        row.getString("email"),
                        JournalCodec.parse(row.getString("settings"))))
                .optional();
    }

    public List<Listed> list() {
        return jdbc.sql("SELECT id, name FROM app_user ORDER BY lower(name), id")
                .query((row, ignored) -> new Listed(row.getObject("id", UUID.class), row.getString("name")))
                .list();
    }

    public AppUser update(UUID id, String name, JsonNode settings) {
        jdbc.sql("UPDATE app_user SET name = :name, settings = :settings::jsonb WHERE id = :id")
                .param("id", id)
                .param("name", name)
                .param("settings", JournalCodec.stringify(settings))
                .update();
        return find(id).orElseThrow();
    }

    private AppUser linkOrCreate(String provider, String subject, String email, String defaultName) {
        return transaction.execute(ignored -> {
            val id = Optional.ofNullable(email)
                    .flatMap(this::findByEmail)
                    .map(AppUser::id)
                    .orElseGet(() -> insert(defaultName, email));
            jdbc.sql("INSERT INTO user_identity (provider, subject, user_id) VALUES (:provider, :subject, :id)")
                    .param("provider", provider)
                    .param("subject", subject)
                    .param("id", id)
                    .update();
            return find(id).orElseThrow();
        });
    }

    private UUID insert(String name, String email) {
        val id = UUID.randomUUID();
        jdbc.sql("INSERT INTO app_user (id, name, email) VALUES (:id, :name, :email)")
                .param("id", id)
                .param("name", name)
                .param("email", email == null ? null : lowercase(email))
                .update();
        return id;
    }

    private Optional<AppUser> findByIdentity(String provider, String subject) {
        return jdbc.sql("SELECT user_id FROM user_identity WHERE provider = :provider AND subject = :subject")
                .param("provider", provider)
                .param("subject", subject)
                .query(UUID.class)
                .optional()
                .flatMap(this::find);
    }

    private static String lowercase(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
