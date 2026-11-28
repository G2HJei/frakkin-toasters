package xyz.zlatanov.frakkintoasters.backend.user;

import lombok.val;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ConfigurationProperties("frakkin.login")
public record LoginProviders(Client google, Client discord) {

    public record Client(String clientId, String clientSecret) {

        boolean isSet() {
            return clientId != null && !clientId.isBlank();
        }
    }

    public List<ClientRegistration> registrations() {
        val registrations = new ArrayList<ClientRegistration>();
        if (google != null && google.isSet()) {
            registrations.add(CommonOAuth2Provider.GOOGLE.getBuilder("google")
                    .clientId(google.clientId())
                    .clientSecret(google.clientSecret())
                    .scope("openid", "email") // the subject and the email are all we keep
                    .build());
        }
        // Spring has no preset for Discord.
        if (discord != null && discord.isSet()) {
            registrations.add(ClientRegistration.withRegistrationId("discord")
                    .clientId(discord.clientId())
                    .clientSecret(discord.clientSecret())
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("identify", "email")
                    .authorizationUri("https://discord.com/oauth2/authorize")
                    .tokenUri("https://discord.com/api/oauth2/token")
                    .userInfoUri("https://discord.com/api/users/@me")
                    .userNameAttributeName("id")
                    .clientName("Discord")
                    .build());
        }
        return registrations;
    }

    /**
     * The email of a provider's user, only once the provider verified it: the login joins the user with that email, so an
     * unverified one would open someone else's account.
     */
    public static Optional<String> verifiedEmail(String registrationId, Map<String, Object> attributes) {
        val verified = switch (registrationId) {
            case "google" -> attributes.get("email_verified");
            case "discord" -> attributes.get("verified");
            default -> null;
        };
        if (!Boolean.TRUE.equals(verified) && !"true".equals(verified)) {
            return Optional.empty();
        }
        return attributes.get("email") instanceof String email && !email.isBlank() ? Optional.of(email) : Optional.empty();
    }
}
