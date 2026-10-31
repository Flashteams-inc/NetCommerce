package com.netcommerce.kernel.core.encryption;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** A searchable fingerprint of an encrypted value: HMAC-SHA256 of its text, base64. */
public record BlindIndex(String value) {

    public static BlindIndex fromHash(String hashValue) {
        return new BlindIndex(hashValue);
    }

    public static BlindIndex compute(String plaintext, byte[] salt) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(salt, "HmacSHA256"));
            return new BlindIndex(Base64.getEncoder().encodeToString(hmac.doFinal(plaintext.getBytes(StandardCharsets.UTF_8))));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is not available", e);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
