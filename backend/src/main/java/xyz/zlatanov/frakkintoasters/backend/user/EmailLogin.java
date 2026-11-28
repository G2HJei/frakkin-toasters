package xyz.zlatanov.frakkintoasters.backend.user;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The login by a code sent to an email address. Asking for a code creates the user of a new address at once; the code
 * logs in. The limits keep the site from flooding a mailbox and bound the guessing: with 5 codes an hour and 5 tries a
 * code, 25 guesses an hour at one of a million codes.
 */
@Service
public class EmailLogin {

    static final Duration CODE_VALID      = Duration.ofMinutes(10);
    static final int      ATTEMPTS        = 5;
    static final Duration COOLDOWN        = Duration.ofSeconds(60);
    static final Duration WINDOW          = Duration.ofHours(1);
    static final int      CODES_PER_EMAIL = 5;
    static final int      CODES_PER_IP    = 10;

    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    private final Users                         users;
    private final JdbcClient                    jdbc;
    private final LoginMail                     mail;
    private final SecretKeySpec                 key;
    private final Clock                         clock;
    private final SecureRandom                  random = new SecureRandom();
    // the codes sent to an address and from an IP within the window; a key nobody used for as long goes
    private final Cache<String, Deque<Instant>> sent = Caffeine.newBuilder().expireAfterAccess(WINDOW).build();

    @Autowired
    public EmailLogin(Users users, JdbcClient jdbc, ObjectProvider<LoginMail> mail,
                      @Value("${frakkin.token-secret}") String secret) {
        this(users, jdbc, mail.getIfAvailable(), secret, Clock.systemUTC());
    }

    EmailLogin(Users users, JdbcClient jdbc, LoginMail mail, String secret, Clock clock) {
        this.users = users;
        this.jdbc = jdbc;
        this.mail = mail;
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.clock = clock;
    }

    public boolean enabled() {
        return mail != null;
    }

    public void request(String email, String ip) {
        val address = address(email);
        val now = clock.instant();
        limit(now, address, ip);
        val user = users.byEmail(address, SecurityConfiguration.defaultName());
        val code = "%06d".formatted(random.nextInt(1_000_000));
        jdbc.sql("""
                        INSERT INTO login_code (user_id, code_hash, created_at, expires_at, attempts)
                        VALUES (:id, :hash, :now, :expires, 0)
                        ON CONFLICT (user_id) DO UPDATE
                            SET code_hash = :hash, created_at = :now, expires_at = :expires, attempts = 0""")
                .param("id", user.id())
                .param("hash", hash(user.id(), code))
                .param("now", Timestamp.from(now))
                .param("expires", Timestamp.from(now.plus(CODE_VALID)))
                .update();
        try {
            mail.send(address, code);
        } catch (RuntimeException e) {
            jdbc.sql("DELETE FROM login_code WHERE user_id = :id").param("id", user.id()).update();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The email couldn't be sent: try again later", e);
        }
    }

    /**
     * The user the code logs in. A try counts and the code goes once it logs in, both in one statement each, so tries at
     * once can't get past the limit or use a code twice.
     */
    public UUID verify(String email, String code) {
        val address = address(email);
        val wrong = new ResponseStatusException(HttpStatus.BAD_REQUEST, "The code is wrong or has expired");
        val digits = code == null ? "" : code.replaceAll("\\s", "");
        if (!digits.matches("\\d{6}")) {
            throw wrong;
        }
        val user = users.findByEmail(address).orElseThrow(() -> wrong);
        val stored = jdbc.sql("""
                        UPDATE login_code SET attempts = attempts + 1
                        WHERE user_id = :id AND attempts < :attempts AND expires_at > :now
                        RETURNING code_hash""")
                .param("id", user.id())
                .param("attempts", ATTEMPTS)
                .param("now", Timestamp.from(clock.instant()))
                .query(String.class)
                .optional()
                .orElseThrow(() -> wrong);
        val hash = hash(user.id(), digits);
        if (!MessageDigest.isEqual(stored.getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8))) {
            throw wrong;
        }
        val used = jdbc.sql("DELETE FROM login_code WHERE user_id = :id AND code_hash = :hash")
                .param("id", user.id())
                .param("hash", hash)
                .update();
        if (used == 0) {
            throw wrong;
        }
        return user.id();
    }

    private String address(String email) {
        if (!enabled()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The email login is off on this server");
        }
        val address = email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
        if (address.length() > 254 || !EMAIL.matcher(address).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That isn't an email address");
        }
        return address;
    }

    private void limit(Instant now, String address, String ip) {
        val toAddress = sent.get("email:" + address, ignored -> new ArrayDeque<>());
        val fromIp = sent.get("ip:" + ip, ignored -> new ArrayDeque<>());
        // always the address first, so two requests never wait for each other's lock
        synchronized (toAddress) {
            synchronized (fromIp) {
                forgetBefore(toAddress, now.minus(WINDOW));
                forgetBefore(fromIp, now.minus(WINDOW));
                if (!toAddress.isEmpty() && toAddress.peekLast().isAfter(now.minus(COOLDOWN))) {
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                            "A code is on its way: wait a minute before asking for another");
                }
                if (toAddress.size() >= CODES_PER_EMAIL || fromIp.size() >= CODES_PER_IP) {
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                            "Too many codes asked for: try again in an hour");
                }
                toAddress.addLast(now);
                fromIp.addLast(now);
            }
        }
    }

    private static void forgetBefore(Deque<Instant> times, Instant start) {
        while (!times.isEmpty() && !times.peekFirst().isAfter(start)) {
            times.pollFirst();
        }
    }

    @SneakyThrows
    private String hash(UUID userId, String code) {
        val mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        return Base64.getEncoder().encodeToString(mac.doFinal((userId + ":" + code).getBytes(StandardCharsets.UTF_8)));
    }
}
