package xyz.zlatanov.frakkintoasters.backend.lobby;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.zlatanov.frakkintoasters.backend.lobby.Lobby.Games;
import xyz.zlatanov.frakkintoasters.backend.lobby.Lobby.Role;
import xyz.zlatanov.frakkintoasters.backend.lobby.Lobby.Room;
import xyz.zlatanov.frakkintoasters.backend.user.AppUserAuthentication.AppUserPrincipal;

import java.util.UUID;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class LobbyController {

    private final Lobby lobby;

    public record Host(String name) {
    }

    public record Join(Role as) {
    }

    @GetMapping
    public Games games(@AuthenticationPrincipal AppUserPrincipal user) {
        return lobby.games(user.id());
    }

    @PostMapping
    public Room host(@AuthenticationPrincipal AppUserPrincipal user, @RequestBody Host host) {
        return lobby.host(user.id(), host.name());
    }

    @GetMapping("/{id}")
    public Room room(@PathVariable UUID id) {
        return lobby.room(id);
    }

    @PostMapping("/{id}/join")
    public Room join(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable UUID id, @RequestBody Join join) {
        return lobby.join(id, user.id(), join.as() == null ? Role.PLAYER : join.as());
    }

    @PostMapping("/{id}/leave")
    public Room leave(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable UUID id) {
        return lobby.leave(id, user.id());
    }

    @PostMapping("/{id}/start")
    public Room start(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable UUID id) {
        return lobby.start(id, user.id());
    }
}
