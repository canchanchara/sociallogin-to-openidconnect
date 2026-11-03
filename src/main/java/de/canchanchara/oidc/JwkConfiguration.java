package de.canchanchara.oidc;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/**
 * Stellt den Schlüssel bereit, mit dem ID- und Access-Tokens signiert werden.
 * <p>
 * Der Schlüssel wird aus der Umgebungsvariable {@code OPENID_SIGNING_KEY} gelesen, damit er einen Neustart
 * oder Redeploy ohne Volume übersteht. Erwartet wird ein RSA-Schlüssel im PKCS#8-Format, entweder als
 * PEM-Text ({@code -----BEGIN PRIVATE KEY-----}) oder als einzeiliges Base64 (DER):
 * <pre>
 * openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -outform DER | base64 -w0
 * </pre>
 * Ist die Variable nicht gesetzt, wird ein temporärer Schlüssel erzeugt.
 */
@Configuration
public class JwkConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JwkConfiguration.class);

    @Value("${OPENID_SIGNING_KEY:}")
    private String signingKey;

    @Bean
    public JWKSource<SecurityContext> jwkSource() throws GeneralSecurityException, JOSEException {
        RSAKey rsaKey;
        if (signingKey == null || signingKey.isBlank()) {
            log.warn("OPENID_SIGNING_KEY ist nicht gesetzt - es wird ein temporärer Schlüssel erzeugt. "
                    + "Nach einem Neustart sind alle ausgestellten Tokens ungültig.");
            rsaKey = generateRsaKey();
        } else {
            rsaKey = parseRsaKey(signingKey);
            log.info("Signaturschlüssel aus OPENID_SIGNING_KEY geladen (kid={})", rsaKey.getKeyID());
        }
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    static RSAKey parseRsaKey(String value) throws GeneralSecurityException, JOSEException {
        if (value.contains("BEGIN RSA PRIVATE KEY")) {
            throw new IllegalArgumentException("OPENID_SIGNING_KEY ist im PKCS#1-Format. Bitte PKCS#8 verwenden, "
                    + "z. B. erzeugt mit: openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048");
        }
        String base64 = value
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\n", "")
                .replaceAll("\\s", "");

        byte[] der;
        try {
            der = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("OPENID_SIGNING_KEY ist kein gültiges Base64/PEM", e);
        }

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(der));
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
                new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent()));

        return withThumbprintKeyId(new RSAKey.Builder(publicKey).privateKey(privateKey).build());
    }

    private static RSAKey generateRsaKey() throws GeneralSecurityException, JOSEException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var keyPair = generator.generateKeyPair();
        return withThumbprintKeyId(new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey(keyPair.getPrivate())
                .build());
    }

    /** Stabile Key-ID aus dem Schlüssel selbst: gleicher Schlüssel ergibt dieselbe {@code kid}. */
    private static RSAKey withThumbprintKeyId(RSAKey rsaKey) throws JOSEException {
        return new RSAKey.Builder(rsaKey).keyID(rsaKey.computeThumbprint().toString()).build();
    }
}
