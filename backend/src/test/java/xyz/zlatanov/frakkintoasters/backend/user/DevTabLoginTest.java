package xyz.zlatanov.frakkintoasters.backend.user;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import xyz.zlatanov.frakkintoasters.backend.BackendTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// The dev login per tab: a request with a dev user's name is that user's, for itself alone.
class DevTabLoginTest extends BackendTest {

    @Autowired
    MockMvc mvc;

    @Test
    void aRequestWithADevUsersNameIsTheirsAndLeavesNoSession() throws Exception {
        val roslin = "Roslin " + UUID.randomUUID().toString().substring(0, 6);
        val adama = "Adama " + UUID.randomUUID().toString().substring(0, 6);

        mvc.perform(get("/api/me").header(DevTabLogin.HEADER, roslin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(roslin));
        mvc.perform(get("/api/me").header(DevTabLogin.HEADER, adama))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(adama));
        // the socket's handshake can't send a header: the name comes as a parameter there
        mvc.perform(get("/api/me").param(DevTabLogin.PARAMETER, roslin))
                .andExpect(jsonPath("$.name").value(roslin));
        val response = mvc.perform(get("/api/me").header(DevTabLogin.HEADER, adama)).andReturn().getResponse();
        assertNull(response.getCookie("SESSION"), "nothing goes into a session");
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }
}
