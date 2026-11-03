package de.canchanchara.oidc;

import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwkConfigurationTest {

    private static KeyPair keyPair;
    private static String base64Der;

    @BeforeAll
    static void createKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
        base64Der = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }

    @Test
    void parsesSingleLineBase64Der() throws Exception {
        RSAKey key = JwkConfiguration.parseRsaKey(base64Der);

        assertTrue(key.isPrivate());
        assertEquals(((RSAPublicKey) keyPair.getPublic()).getModulus(), key.toRSAPublicKey().getModulus());
    }

    @Test
    void parsesPem() throws Exception {
        String pem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----\n";

        assertEquals(JwkConfiguration.parseRsaKey(base64Der).getKeyID(), JwkConfiguration.parseRsaKey(pem).getKeyID());
    }

    @Test
    void parsesPemWithEscapedNewlines() throws Exception {
        String pem = "-----BEGIN PRIVATE KEY-----\\n" + base64Der + "\\n-----END PRIVATE KEY-----";

        assertEquals(JwkConfiguration.parseRsaKey(base64Der).getKeyID(), JwkConfiguration.parseRsaKey(pem).getKeyID());
    }

    @Test
    void keyIdIsStableForSameKey() throws Exception {
        assertEquals(JwkConfiguration.parseRsaKey(base64Der).getKeyID(),
                JwkConfiguration.parseRsaKey(base64Der).getKeyID());
    }

    @Test
    void rejectsPkcs1() {
        assertThrows(IllegalArgumentException.class,
                () -> JwkConfiguration.parseRsaKey("-----BEGIN RSA PRIVATE KEY-----\nabc\n-----END RSA PRIVATE KEY-----"));
    }

    @Test
    void rejectsInvalidBase64() {
        assertThrows(IllegalArgumentException.class, () -> JwkConfiguration.parseRsaKey("kein-base64!"));
    }
}
