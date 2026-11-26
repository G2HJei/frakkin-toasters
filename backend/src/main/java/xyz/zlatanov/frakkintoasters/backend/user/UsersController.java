package xyz.zlatanov.frakkintoasters.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The users of the site, for the users page. Logged in users only, like the rest of the API.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

    private final Users users;

    @GetMapping
    public List<Users.Listed> list() {
        return users.list();
    }
}
