package xyz.zlatanov.frakkintoasters.backend.user;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MockMvc;
import xyz.zlatanov.frakkintoasters.backend.BackendTest;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;

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

        val first = users.login("google", subject, "Starbuck");
        val second = users.login("google", subject, "Apollo");

        assertEquals(first, second);
        assertEquals("Starbuck", second.name());
        assertEquals(1, jdbc.sql("SELECT count(*) FROM user_identity WHERE subject = :subject")
                .param("subject", subject).query(Integer.class).single());
        assertNotEquals(first.id(), users.login("discord", subject, "Starbuck").id()); // another provider's subject
    }

    @Test
    void theApiRefusesACallerWhoIsNotLoggedIn() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/games")).andExpect(status().isUnauthorized());
        mvc.perform(get("/ws/games/" + UUID.randomUUID())).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/login/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.devLogin").value(true));
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
    void restCallsNeedTheCsrfToken() throws Exception {
        mvc.perform(post("/api/dev/login").contentType(APPLICATION_JSON).content("{\"name\":\"Gaeta\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void theDevLoginCantBeOnWithARealProvider() {
        val providers = new LoginProviders(new LoginProviders.Client("id", "secret"), null);

        assertThrows(IllegalStateException.class, () -> new DevLoginController(users, null, providers));
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
