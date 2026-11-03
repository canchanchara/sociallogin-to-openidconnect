package de.canchanchara.oidc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Pattern;

@Component
public class OpenIDConfiguration {

    @Value("${OPENID_CLIENT_ID}")
    private String clientId;

    @Value("${OPENID_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${OPENID_REDIRECT_URI}")
    private String redirectUri;

    @Bean
    public RegisteredClientRepository registeredClientRepository() {

        RegisteredClient registeredClient = RegisteredClient.withId(clientId)
                .clientId(clientId)
                .clientSecret(encodeClientSecret(clientSecret))
                .clientAuthenticationMethods(authMethods -> {
                    authMethods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
                    authMethods.add(ClientAuthenticationMethod.CLIENT_SECRET_POST);
                })
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(redirectUri)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(5))
                        .refreshTokenTimeToLive(Duration.ofDays(30))
                        .reuseRefreshTokens(true)
                        .build())
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(true)
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    /**
     * Secrets mit Encoder-Präfix (z. B. {@code {bcrypt}...} oder {@code {noop}...}) werden unverändert übernommen.
     * Secrets ohne Präfix werden beim Start mit bcrypt gehasht, damit sie nicht im Klartext im Repository liegen.
     * Secrets über 72 Bytes (Grenze von bcrypt) werden stattdessen mit PBKDF2 gehasht.
     */
    static String encodeClientSecret(String secret) {
        if (ENCODER_PREFIX.matcher(secret).find()) {
            return secret;
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            // bcrypt verarbeitet höchstens 72 Bytes und lehnt längere Passwörter beim Hashen ab.
            return "{" + PBKDF2_ID + "}" + Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8().encode(secret);
        }
        return PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(secret);
    }

    private static final int BCRYPT_MAX_BYTES = 72;

    /** ID, unter der {@link PasswordEncoderFactories} den passenden PBKDF2-Encoder registriert. */
    private static final String PBKDF2_ID = "pbkdf2@SpringSecurity_v5_8";

    private static final Pattern ENCODER_PREFIX = Pattern.compile("^\\{[^{}]+}");
}
