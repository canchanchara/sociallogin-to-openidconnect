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
                if (dataMap.get("userName") != null) {
                    context.getClaims().claim("preferred_username", dataMap.get("userName"));
                }
                if (dataMap.get("firstName") != null) {
                    context.getClaims().claim("given_name", dataMap.get("firstName"));
                }

                if (dataMap.get("lastName") != null) {
                    context.getClaims().claim("family_name", dataMap.get("lastName"));
                }

                if (dataMap.get("displayName") != null) {
                    context.getClaims().claim("name", dataMap.get("displayName"));
                }

                if (dataMap.get("imageUrl") != null) {
                    context.getClaims().claim("profile", dataMap.get("imageUrl"));
                }
            }

            if (scopes.contains(OidcScopes.EMAIL)) {
                if (oauth2User.getAttribute("email") != null) {
                    context.getClaims().claim("email", oauth2User.getAttribute("email"));
                }
            }
        }

    }


}
