package xyz.zlatanov.frakkintoasters.backend.user;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MockMvc;
import xyz.zlatanov.frakkintoasters.backend.BackendTest;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LoginTest extends BackendTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    Users   users;

    @Test
    void aFirstLoginCreatesTheUserAndALaterOneFindsHim() {
        val subject = UUID.randomUUID().toString();
        val email = "starbuck-" + subject + "@example.com";

        val first = users.login("google", subject, email, "Starbuck");
        val second = users.login("google", subject, email, "Apollo");

        assertEquals(first, second);
        assertEquals("Starbuck", second.name());
        assertEquals(email, second.email());
        assertEquals(1, jdbc.sql("SELECT count(*) FROM user_identity WHERE subject = :subject")
                .param("subject", subject).query(Integer.class).single());
        assertNotEquals(first.id(), users.login("discord", subject, null, "Starbuck").id()); // another provider's subject
    }

    @Test
    void theSameEmailIsOneUserWhicheverWayHeLogsIn() {
        val suffix = UUID.randomUUID().toString();
        val email = "Kara-" + suffix + "@Example.com";

        val google = users.login("google", "g-" + suffix, email, "Starbuck");
        val discord = users.login("discord", "d-" + suffix, email.toUpperCase(), "Kara");

        assertEquals(google.id(), discord.id());
        assertEquals(email.toLowerCase(), google.email());
        assertEquals(google.id(), users.byEmail(email, "Pilot").id());
        assertEquals(2, jdbc.sql("SELECT count(*) FROM user_identity WHERE user_id = :id")
                .param("id", google.id()).query(Integer.class).single());
        // a later email at the provider leaves the account as it is
        assertEquals(email.toLowerCase(), users.login("google", "g-" + suffix, "other-" + email, "Starbuck").email());
    }

    @Test
    void onlyAnEmailTheProviderVerifiedCounts() {
        assertEquals(Optional.of("kara@example.com"),
                LoginProviders.verifiedEmail("google", Map.of("email", "kara@example.com", "email_verified", true)));
        assertEquals(Optional.of("kara@example.com"),
                LoginProviders.verifiedEmail("discord", Map.of("email", "kara@example.com", "verified", true)));
        assertEquals(Optional.empty(),
                LoginProviders.verifiedEmail("google", Map.of("email", "kara@example.com", "email_verified", false)));
        assertEquals(Optional.empty(),
                LoginProviders.verifiedEmail("discord", Map.of("email", "kara@example.com", "verified", false)));
        assertEquals(Optional.empty(), LoginProviders.verifiedEmail("discord", Map.of("verified", true)));
        // Google's verified flag belongs to Google
        assertEquals(Optional.empty(),
                LoginProviders.verifiedEmail("discord", Map.of("email", "kara@example.com", "email_verified", true)));
    }

    @Test
    void theApiRefusesACallerWhoIsNotLoggedIn() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/games")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
        mvc.perform(get("/ws/games/" + UUID.randomUUID())).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/login/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.devLogin").value(true))
                .andExpect(jsonPath("$.email").value(true)); // the dev profile's codes go to the log
    }

    @Test
    void theDevLoginLogsInByNameAndKeepsTheSession() throws Exception {
        val name = "Helo " + UUID.randomUUID().toString().substring(0, 6);
        val session = login(name);

        val me = mvc.perform(get("/api/me").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andReturn().getResponse().getContentAsString();
        val again = login(name);
        mvc.perform(get("/api/me").cookie(again))
                .andExpect(jsonPath("$.id").value(JournalCodec.parse(me).get("id").stringValue()));
    }

    @Test
    void aUserChangesHisNameAndSettings() throws Exception {
        val session = login("Athena " + UUID.randomUUID().toString().substring(0, 6));

        mvc.perform(put("/api/me").cookie(session).with(csrf()).contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Boomer\",\"settings\":{\"theme\":\"dark\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Boomer"))
                .andExpect(jsonPath("$.settings.theme").value("dark"));
        mvc.perform(put("/api/me").cookie(session).with(csrf()).contentType(APPLICATION_JSON).content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void theUsersAreListedByNameWhateverItsCase() throws Exception {
        val suffix = UUID.randomUUID().toString().substring(0, 6);
        val session = login("Zarek " + suffix);
        login("adama " + suffix);

        val users = JournalCodec.parse(mvc.perform(get("/api/users").cookie(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        val names = new ArrayList<String>();
        for (int i = 0; i < users.size(); i++) {
            names.add(users.get(i).get("name").stringValue());
        }

        // by bytes "Zarek" would come first
        assertTrue(names.indexOf("adama " + suffix) >= 0);
        assertTrue(names.indexOf("adama " + suffix) < names.indexOf("Zarek " + suffix));
    }

    @Test
    void restCallsNeedTheCsrfToken() throws Exception {
        mvc.perform(post("/api/dev/login").contentType(APPLICATION_JSON).content("{\"name\":\"Gaeta\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void theDevLoginCantBeOnWithARealProvider() {
        val providers = new LoginProviders(new LoginProviders.Client("id", "secret"), null);

        assertThrows(IllegalStateException.class, () -> new DevLoginController(users, null, providers));
    }

    @Test
    void theDevLoginCantBeOnWithAMailServer() {
        assertThrows(IllegalStateException.class, () -> new DevLoginMail("smtp.example.com"));
    }

    // Spring Session finds the session by its SESSION cookie.
    private Cookie login(String name) throws Exception {
        val result = mvc.perform(post("/api/dev/login").with(csrf()).contentType(APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        val session = result.getResponse().getHeaders("Set-Cookie").stream()
                .filter(header -> header.startsWith("SESSION="))
                .map(header -> header.substring("SESSION=".length(), header.indexOf(';')))
                .findFirst()
                .orElseThrow();
        return new Cookie("SESSION", session);
    }
}
