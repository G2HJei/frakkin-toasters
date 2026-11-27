package xyz.zlatanov.frakkintoasters.backend.lobby;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import xyz.zlatanov.frakkintoasters.backend.lobby.Lobby.Games;
import xyz.zlatanov.frakkintoasters.backend.lobby.Lobby.Role;
import xyz.zlatanov.frakkintoasters.backend.lobby.Lobby.Room;
import xyz.zlatanov.frakkintoasters.backend.lobby.Seats.VoteKind;
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

    public record Kick(UUID userId, boolean ban) {
    }

    public record TakeSeat(int playerNumber) {
    }

    public record Ballot(VoteKind kind, int playerNumber) {
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
    public Room room(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable UUID id) {
        return lobby.roomFor(id, user.id());
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

    @PostMapping("/{id}/kick")
    public Room kick(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable UUID id, @RequestBody Kick kick) {
        if (kick.userId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Whom to remove?");
        }
        return lobby.kick(id, user.id(), kick.userId(), kick.ban());
    }

    @PostMapping("/{id}/cancel")
    public Room cancel(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable UUID id) {
        return lobby.cancel(id, user.id());
    }

    @PostMapping("/{id}/seat")
    public Room takeSeat(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable UUID id,
                         @RequestBody TakeSeat seat) {
        return lobby.takeSeat(id, user.id(), seat.playerNumber());
    }

    @PostMapping("/{id}/votes")
    public Room vote(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable UUID id, @RequestBody Ballot ballot) {
        return lobby.vote(id, user.id(), kind(ballot), ballot.playerNumber());
    }

    @PostMapping("/{id}/votes/withdraw")
    public Room withdraw(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable UUID id,
                         @RequestBody Ballot ballot) {
        return lobby.withdraw(id, user.id(), kind(ballot), ballot.playerNumber());
    }

    @PostMapping("/{id}/rematch")
    public Room rematch(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable UUID id) {
        return lobby.rematch(id, user.id());
    }

    private static VoteKind kind(Ballot ballot) {
        if (ballot.kind() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A vote needs a kind");
        }
        return ballot.kind();
    }
}
