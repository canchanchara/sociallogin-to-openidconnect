package de.canchanchara.oidc;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FederatedIdentityIdTokenCustomizerTest {

    @Test
    void mapsPictureAndEmailVerified() {
        Map<String, Object> claims = customize(CustomOAuth2UserService.flatten(Map.of(
                "email", "max@example.org",
                "data", Map.of("userName", "max", "imageUrl", "https://example.org/max.png"))));

        assertEquals("max", claims.get("preferred_username"));
        assertEquals("https://example.org/max.png", claims.get("picture"));
        assertFalse(claims.containsKey("profile"));
        assertEquals("max@example.org", claims.get("email"));
        assertEquals(true, claims.get("email_verified"));
    }

    @Test
    void missingFieldsAreOmitted() {
        Map<String, Object> claims = customize(Map.of("userName", "max"));

        assertEquals("max", claims.get("preferred_username"));
        assertFalse(claims.containsKey("email"));
        assertFalse(claims.containsKey("email_verified"));
        assertFalse(claims.containsKey("picture"));
    }

    private static Map<String, Object> customize(Map<String, Object> attributes) {
        var user = new DefaultOAuth2User(AuthorityUtils.NO_AUTHORITIES, attributes, "userName");
        var principal = new TestingAuthenticationToken(user, null);

        var headers = JwsHeader.with(SignatureAlgorithm.RS256);
        var claims = JwtClaimsSet.builder().subject("max");
        var context = JwtEncodingContext.with(headers, claims)
                .principal(principal)
                .tokenType(new OAuth2TokenType(OidcParameterNames.ID_TOKEN))
                .authorizedScopes(Set.of(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL))
                .build();

        new FederatedIdentityIdTokenCustomizer().customize(context);

        return claims.build().getClaims();
    }
}
