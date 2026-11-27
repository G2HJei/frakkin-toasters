package xyz.zlatanov.frakkintoasters.backend.user;

import lombok.val;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.List;

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
                    .scope("openid") // the subject is all we keep
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
                    .scope("identify")
                    .authorizationUri("https://discord.com/oauth2/authorize")
                    .tokenUri("https://discord.com/api/oauth2/token")
                    .userInfoUri("https://discord.com/api/users/@me")
                    .userNameAttributeName("id")
                    .clientName("Discord")
                    .build());
        }
        return registrations;
    }
}
