package de.canchanchara.oidc;

import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Map;
import java.util.Set;

public class FederatedIdentityIdTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {

        Set<String> scopes = context.getAuthorizedScopes();

        if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue()) &&
                context.getPrincipal().getPrincipal() instanceof OAuth2User oauth2User) {

            if (scopes.contains(OidcScopes.PROFILE)) {
                var dataMap = (Map) oauth2User.getAttributes().get("data");
                context.getClaims().claim("preferred_username", dataMap.get("userName"));
                context.getClaims().claim("given_name", getValueAsString(dataMap.get("firstName")));
                context.getClaims().claim("family_name", getValueAsString(dataMap.get("lastName")));
                context.getClaims().claim("name", getValueAsString(dataMap.get("displayName")));
                context.getClaims().claim("profile", getValueAsString(dataMap.get("imageUrl")));
            }

            if (scopes.contains(OidcScopes.EMAIL)) {
                context.getClaims().claim("email", oauth2User.getAttribute("email"));
            }
        }

    }

    private static String getValueAsString(Object value) {
        return value == null ? "" : (String) value;
    }


}
