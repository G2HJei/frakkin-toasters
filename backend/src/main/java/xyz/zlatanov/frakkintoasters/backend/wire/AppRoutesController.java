package xyz.zlatanov.frakkintoasters.backend.wire;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * A reload asks the server for a frontend route: they all get the app, which routes itself.
 */
@Controller
public class AppRoutesController {

    @GetMapping({"/login", "/play", "/users", "/about", "/profile", "/games", "/games/{id}", "/sandbox/new"})
    public String app() {
        return "forward:/index.html";
    }
}
