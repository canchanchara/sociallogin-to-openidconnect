package de.canchanchara.oidc;

import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Set;

/**
 * Überträgt die Church-Tools-Attribute als OIDC-Claims ins ID-Token (und damit auch nach {@code /userinfo}).
 * Die Attribute sind bereits durch {@link CustomOAuth2UserService} vereinheitlicht; fehlende Felder werden
 * einfach weggelassen.
 */
public class FederatedIdentityIdTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {

        if (!OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue()) ||
                !(context.getPrincipal().getPrincipal() instanceof OAuth2User user)) {
            return;
        }

        Set<String> scopes = context.getAuthorizedScopes();

        if (scopes.contains(OidcScopes.PROFILE)) {
            copy(context, user, "userName", StandardClaimNames.PREFERRED_USERNAME);
            copy(context, user, "firstName", StandardClaimNames.GIVEN_NAME);
            copy(context, user, "lastName", StandardClaimNames.FAMILY_NAME);
            copy(context, user, "displayName", StandardClaimNames.NAME);
            copy(context, user, "imageUrl", StandardClaimNames.PICTURE);
        }

        if (scopes.contains(OidcScopes.EMAIL) && copy(context, user, "email", StandardClaimNames.EMAIL)) {
            // Church Tools verwaltet die E-Mail-Adresse, daher gilt sie als verifiziert.
            context.getClaims().claim(StandardClaimNames.EMAIL_VERIFIED, true);
        }
    }

    private static boolean copy(JwtEncodingContext context, OAuth2User user, String attribute, String claim) {
        Object value = user.getAttribute(attribute);
        if (value == null || (value instanceof String s && s.isBlank())) {
            return false;
        }
        context.getClaims().claim(claim, value);
        return true;
    }
}
