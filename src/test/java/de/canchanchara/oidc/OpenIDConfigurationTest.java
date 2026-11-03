package de.canchanchara.oidc;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenIDConfigurationTest {

    private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Test
    void plainSecretIsHashedWithBcrypt() {
        String encoded = OpenIDConfiguration.encodeClientSecret("secret");

        assertTrue(encoded.startsWith("{bcrypt}"));
        assertTrue(encoder.matches("secret", encoded));
    }

    @Test
    void longSecretIsHashedWithPbkdf2() {
        String secret = "x".repeat(100);

        String encoded = OpenIDConfiguration.encodeClientSecret(secret);

        assertTrue(encoded.startsWith("{pbkdf2@SpringSecurity_v5_8}"));
        assertTrue(encoder.matches(secret, encoded));
        assertFalse(encoder.matches("x".repeat(99), encoded));
    }

    @Test
    void secretWithExactly72BytesIsHashedWithBcrypt() {
        String secret = "x".repeat(72);

        String encoded = OpenIDConfiguration.encodeClientSecret(secret);

        assertTrue(encoded.startsWith("{bcrypt}"));
        assertTrue(encoder.matches(secret, encoded));
    }

    @Test
    void prefixedSecretIsKeptAsIs() {
        assertEquals("{noop}secret", OpenIDConfiguration.encodeClientSecret("{noop}secret"));

        String bcrypt = encoder.encode("secret");
        assertEquals(bcrypt, OpenIDConfiguration.encodeClientSecret(bcrypt));
    }
}
