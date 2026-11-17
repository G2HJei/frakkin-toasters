package xyz.zlatanov.frakkintoasters.backend.game;

import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

/**
 * The token a client echoes to name the state it answers: the sequence of the game, hashed with a server secret. A bare
 * sequence would let a client count the entries it wasn't shown, e.g. how many players passed in an interrupt window
 * (backend decision 10).
 */
@Component
public class StateTokens {

    private final SecretKeySpec key;

    public StateTokens(@Value("${frakkin.token-secret}") String secret) {
        key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @SneakyThrows
    public String token(UUID gameBundleId, int sequence) {
        val mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        val hash = mac.doFinal((gameBundleId + ":" + sequence).getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 22);
    }

    public boolean matches(String token, UUID gameBundleId, int sequence) {
        return token != null && MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8),
                token(gameBundleId, sequence).getBytes(StandardCharsets.UTF_8));
    }
}
