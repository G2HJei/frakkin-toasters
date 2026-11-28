package xyz.zlatanov.frakkintoasters.backend.user;

import jakarta.servlet.http.Cookie;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.server.ResponseStatusException;
import xyz.zlatanov.frakkintoasters.backend.BackendTest;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmailLoginTest extends BackendTest {

    @Autowired
    MockMvc      mvc;
    @Autowired
    Users        users;
    @Autowired
    DevLoginMail mail;

    // The limits per IP hold for the whole context, which the tests share: each test asks from an IP of its own.
    private final String ip = "10.%d.%d.%d".formatted(
            ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256),
            ThreadLocalRandom.current().nextInt(1, 255));

    @Test
    void aCodeLogsInTheUserItsRequestCreated() throws Exception {
        val email = email("starbuck");

        requestCode(email).andExpect(status().isNoContent());
        val user = users.findByEmail(email).orElseThrow();
        val login = logIn(email, code(email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id().toString()))
                .andExpect(jsonPath("$.email").value(email));

        assertTrue(user.name().startsWith("Pilot "), user.name());
        mvc.perform(get("/api/me").cookie(session(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
        logIn(email, code(email)).andExpect(status().isBadRequest()); // a code logs in once
    }

    @Test
    void anAddressIsOneUserWhateverItsCase() throws Exception {
        val email = email("apollo");
        val shouted = "  " + email.toUpperCase() + " ";

        requestCode(shouted).andExpect(status().isNoContent());

        assertEquals(email, users.findByEmail(email).orElseThrow().email());
        logIn(shouted, code(email)).andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void aProviderLoginAndAnEmailLoginAreOneUser() throws Exception {
        val email = email("helo");
        val google = users.login("google", UUID.randomUUID().toString(), email, "Helo");

        requestCode(email).andExpect(status().isNoContent());

        logIn(email, code(email)).andExpect(jsonPath("$.id").value(google.id().toString()))
                .andExpect(jsonPath("$.name").value("Helo"));
    }

    @Test
    void fiveWrongCodesVoidTheCode() throws Exception {
        val email = email("boomer");
        requestCode(email).andExpect(status().isNoContent());
        val code = code(email);
        val wrong = "%06d".formatted((Integer.parseInt(code) + 1) % 1_000_000);

        for (int i = 0; i < EmailLogin.ATTEMPTS; i++) {
            logIn(email, wrong)
                    .andExpect(status().isBadRequest())
                    .andExpect(status().reason("The code is wrong or has expired"));
        }

        logIn(email, code).andExpect(status().isBadRequest());
    }

    @Test
    void anExpiredCodeIsRefused() throws Exception {
        val email = email("athena");
        requestCode(email).andExpect(status().isNoContent());
        jdbc.sql("UPDATE login_code SET expires_at = now() - interval '1 second' WHERE user_id = :id")
                .param("id", users.findByEmail(email).orElseThrow().id())
                .update();

        logIn(email, code(email)).andExpect(status().isBadRequest());
    }

    @Test
    void aCodeForAnAddressNobodyAskedForIsRefused() throws Exception {
        logIn(email("nobody"), "123456").andExpect(status().isBadRequest());
        logIn(email("nobody"), "12ab56").andExpect(status().isBadRequest());
    }

    @Test
    void anotherCodeWaitsAMinute() throws Exception {
        val email = email("gaeta");

        requestCode(email).andExpect(status().isNoContent());

        requestCode(email).andExpect(status().isTooManyRequests());
    }

    @Test
    void anAddressGetsFiveCodesAnHourAndAnIpTen() {
        val clock = new MovingClock();
        val emailLogin = new EmailLogin(users, jdbc, mail, "secret", clock);
        val email = email("dualla");
        for (int i = 0; i < EmailLogin.CODES_PER_EMAIL; i++) {
            emailLogin.request(email, ip);
            clock.advance(EmailLogin.COOLDOWN.plusSeconds(1));
        }

        assertStatus(HttpStatus.TOO_MANY_REQUESTS, () -> emailLogin.request(email, ip));
        clock.advance(EmailLogin.WINDOW);
        emailLogin.request(email, ip);

        val other = "10.255.255." + ThreadLocalRandom.current().nextInt(1, 255);
        for (int i = 0; i < EmailLogin.CODES_PER_IP; i++) {
            emailLogin.request(email("tigh" + i), other);
        }
        assertStatus(HttpStatus.TOO_MANY_REQUESTS, () -> emailLogin.request(email("ellen"), other));
    }

    @Test
    void aCodeThatCouldntBeSentIsGone() {
        LoginMail broken = (email, code) -> {
            throw new IllegalStateException("The mail server is away");
        };
        val emailLogin = new EmailLogin(users, jdbc, broken, "secret", Clock.systemUTC());
        val email = email("cally");

        assertStatus(HttpStatus.SERVICE_UNAVAILABLE, () -> emailLogin.request(email, ip));

        assertEquals(0, jdbc.sql("SELECT count(*) FROM login_code WHERE user_id = :id")
                .param("id", users.findByEmail(email).orElseThrow().id()).query(Integer.class).single());
    }

    @Test
    void withoutAMailSenderTheEmailLoginIsOff() {
        val emailLogin = new EmailLogin(users, jdbc, null, "secret", Clock.systemUTC());

        assertFalse(emailLogin.enabled());
        assertStatus(HttpStatus.NOT_FOUND, () -> emailLogin.request(email("tyrol"), ip));
        assertStatus(HttpStatus.NOT_FOUND, () -> emailLogin.verify(email("tyrol"), "123456"));
    }

    @Test
    void anAddressHasToBeOne() throws Exception {
        requestCode("starbuck").andExpect(status().isBadRequest());
        requestCode("starbuck@galactica").andExpect(status().isBadRequest());
        requestCode("a@" + "b".repeat(250) + ".com").andExpect(status().isBadRequest());
    }

    @Test
    void theRequestsNeedTheCsrfToken() throws Exception {
        mvc.perform(post("/api/login/email").with(from()).contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email("zarek") + "\"}"))
                .andExpect(status().isForbidden());
    }

    private ResultActions requestCode(String email) throws Exception {
        return mvc.perform(post("/api/login/email").with(csrf()).with(from()).contentType(APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"));
    }

    private ResultActions logIn(String email, String code) throws Exception {
        return mvc.perform(post("/api/login/email/verify").with(csrf()).with(from()).contentType(APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"code\":\"" + code + "\"}"));
    }

    private String code(String email) {
        return mail.lastCode(email).orElseThrow();
    }

    private RequestPostProcessor from() {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    // Spring Session finds the session by its SESSION cookie.
    private static Cookie session(ResultActions login) {
        val header = login.andReturn().getResponse().getHeaders("Set-Cookie").stream()
                .filter(value -> value.startsWith("SESSION="))
                .findFirst()
                .orElseThrow();
        return new Cookie("SESSION", header.substring("SESSION=".length(), header.indexOf(';')));
    }

    private static String email(String name) {
        return name + "-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    private static void assertStatus(HttpStatus expected, Runnable call) {
        assertEquals(expected, assertThrows(ResponseStatusException.class, call::run).getStatusCode());
    }

    private static final class MovingClock extends Clock {

        private Instant now = Instant.now();

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
