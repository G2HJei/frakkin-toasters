package xyz.zlatanov.frakkintoasters.backend.sandbox;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import xyz.zlatanov.frakkintoasters.backend.journal.CodecException;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.backend.user.AppUserAuthentication.AppUserPrincipal;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SandboxController {

    // an exported journal of a long game is a few megabytes
    static final int MAX_IMPORT_CHARACTERS = 32 * 1024 * 1024;

    private final Sandboxes sandboxes;

    public record NewSandbox(int playerCount, Long seed, String name) {
    }

    @PostMapping("/api/sandbox")
    public Map<String, UUID> start(@AuthenticationPrincipal AppUserPrincipal user, @RequestBody NewSandbox sandbox) {
        return Map.of("id", sandboxes.start(user.id(), sandbox.playerCount(), sandbox.seed(), sandbox.name()));
    }

    @PostMapping("/api/sandbox/import")
    public Map<String, UUID> importJournal(@AuthenticationPrincipal AppUserPrincipal user, @RequestBody String file,
                                           @RequestParam(required = false) String name) {
        if (file.length() > MAX_IMPORT_CHARACTERS) {
            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "The journal is too large");
        }
        final JsonNode json;
        try {
            json = JournalCodec.parse(file);
        } catch (CodecException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return Map.of("id", sandboxes.importJournal(user.id(), json, name));
    }

    @GetMapping("/api/games/{id}/journal")
    public ResponseEntity<String> export(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable UUID id) {
        val json = JournalCodec.stringify(sandboxes.export(id, user.id()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sandbox-" + id + ".json\"")
                .body(json);
    }
}
