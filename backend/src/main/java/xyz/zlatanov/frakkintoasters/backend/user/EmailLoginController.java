package xyz.zlatanov.frakkintoasters.backend.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Every address gets the same answer: the user of a new one is created with the code.
 */
@RestController
@RequestMapping("/api/login/email")
@RequiredArgsConstructor
public class EmailLoginController {

    private final EmailLogin                emailLogin;
    private final Users                     users;
    private final SecurityContextRepository contexts;

    public record CodeRequest(String email) {
    }

    public record CodeLogin(String email, String code) {
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void request(@RequestBody CodeRequest body, HttpServletRequest request) {
        emailLogin.request(body.email(), request.getRemoteAddr());
    }

    @PostMapping("/verify")
    public AppUser verify(@RequestBody CodeLogin body, HttpServletRequest request, HttpServletResponse response) {
        val userId = emailLogin.verify(body.email(), body.code());
        SecurityConfiguration.logIn(userId, request, response, contexts);
        return users.find(userId).orElseThrow();
    }
}
