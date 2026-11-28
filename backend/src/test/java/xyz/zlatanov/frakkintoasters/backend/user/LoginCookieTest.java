package xyz.zlatanov.frakkintoasters.backend.user;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import xyz.zlatanov.frakkintoasters.backend.TestcontainersConfiguration;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

// On a real server: MockMvc's mock servlet context counts as a war deployment, whose session cookie Boot doesn't configure.
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("dev")
@Import(TestcontainersConfiguration.class)
class LoginCookieTest {

    @LocalServerPort
    int        port;
    @Autowired
    JdbcClient jdbc;

    @Test
    void aLoginIsKeptOnTheDeviceFor30Days() throws Exception {
        val days30 = Duration.ofDays(30).toSeconds();
        val cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        val http = HttpClient.newBuilder().cookieHandler(cookies).build();
        http.send(HttpRequest.newBuilder(uri("/api/login/providers")).build(), BodyHandlers.discarding());

        val login = http.send(HttpRequest.newBuilder(uri("/api/dev/login"))
                .header("Content-Type", "application/json")
                .header("X-XSRF-TOKEN", cookie(cookies, "XSRF-TOKEN"))
                .POST(BodyPublishers.ofString("{\"name\":\"Dualla " + UUID.randomUUID().toString().substring(0, 6) + "\"}"))
                .build(), BodyHandlers.discarding());
        val header = login.headers().allValues("Set-Cookie").stream()
                .filter(value -> value.startsWith("SESSION="))
                .findFirst()
                .orElseThrow();
        val sessionId = new String(Base64.getDecoder().decode(cookie(cookies, "SESSION")));

        assertEquals(200, login.statusCode());
        assertTrue(header.contains("; Max-Age=" + days30 + ";"), header); // a cookie with an age outlives the browser
        assertTrue(header.contains("; HttpOnly"), header);
        assertEquals(days30, jdbc.sql("SELECT max_inactive_interval FROM spring_session WHERE session_id = :id")
                .param("id", sessionId).query(Long.class).single());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private static String cookie(CookieManager cookies, String name) {
        return cookies.getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals(name))
                .map(HttpCookie::getValue)
                .findFirst()
                .orElseThrow();
    }
}
