package de.canchanchara.oidc;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lädt die Userinfo von Church Tools und vereinheitlicht sie.
 * <p>
 * Church Tools liefert Felder teils auf oberster Ebene, teils unter {@code data}. Damit alle weiteren Stellen
 * (Benutzername/{@code sub}, Claims) aus einer Quelle lesen, werden die Felder aus {@code data} auf die oberste
 * Ebene übernommen. Bei doppelt vorhandenen Feldern gewinnt die oberste Ebene.
 * <p>
 * Welches Attribut als Benutzername ({@code sub}) dient, legt {@code user-name-attribute} fest
 * (= {@code OPENID_SUB}, siehe application.properties).
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    static final String DATA = "data";

    public CustomOAuth2UserService() {
        setAttributesConverter(request -> attributes -> normalize(request, attributes));
    }

    private static Map<String, Object> normalize(OAuth2UserRequest request, Map<String, Object> attributes) {
        Map<String, Object> normalized = flatten(attributes);

        String userNameAttribute = request.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        if (normalized.get(userNameAttribute) == null) {
            // Ohne dieses Attribut gibt es kein "sub". Als OAuth2AuthenticationException endet das im Login-Fehler
            // statt in einem 500.
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info_response",
                    "Attribut '" + userNameAttribute + "' (OPENID_SUB) fehlt in der Userinfo-Antwort", null));
        }
        return normalized;
    }

    static Map<String, Object> flatten(Map<String, Object> attributes) {
        Map<String, Object> result = new LinkedHashMap<>(attributes);
        if (attributes.get(DATA) instanceof Map<?, ?> data) {
            data.forEach((key, value) -> {
                if (key instanceof String name && value != null) {
                    result.putIfAbsent(name, value);
                }
            });
        }
        return result;
    }
}
