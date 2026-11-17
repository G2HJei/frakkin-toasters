package xyz.zlatanov.frakkintoasters.backend.wire;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The frontend's routes, which the browser asks the server for on a reload: they all get the app, which routes itself.
 * The jar serves the built app from {@code static/}.
 */
@Controller
public class AppRoutesController {

    @GetMapping({"/login", "/settings", "/games", "/games/{id}", "/sandbox/new"})
    public String app() {
        return "forward:/index.html";
    }
}
