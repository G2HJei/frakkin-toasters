package xyz.zlatanov.frakkintoasters.backend.user;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * A logged in user, however he logged in. It lives in the HTTP session, which the web socket handshake uses too.
 */
public class AppUserAuthentication extends AbstractAuthenticationToken {

    /**
     * The principal of a logged in user: his id.
     */
    public record AppUserPrincipal(UUID id) implements Principal, Serializable {

        @Override
        public String getName() {
            return id.toString();
        }
    }

    private final AppUserPrincipal principal;

    public AppUserAuthentication(UUID userId) {
        super(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.principal = new AppUserPrincipal(userId);
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public AppUserPrincipal getPrincipal() {
        return principal;
    }
}
