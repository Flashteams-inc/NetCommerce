package com.netcommerce.kernel.persistence.kernel;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.netcommerce.kernel.compliance.encryption.CryptoProvider;
import com.netcommerce.kernel.core.encryption.BlindIndex;
import com.netcommerce.kernel.core.encryption.EncryptedData;

/**
 * The .NET DevelopmentCryptoProvider (tests/NetCommerce.Domain.Tests), byte for
 * byte: AES-256 in .NET's default mode (CBC, PKCS7 padding) under the fixed
 * development key, a random IV or, when deterministic, the first 16 bytes of the
 * text's SHA-256, key id "dev-crypto-v1". The source has no production provider;
 * this is the one its tests define.
 */
public class DevelopmentCryptoProvider implements CryptoProvider {

    private static final String KEY_ID = "dev-crypto-v1";
    private static final SecretKeySpec KEY = new SecretKeySpec(
            "DevelopmentMasterKey1234567890!!".getBytes(StandardCharsets.UTF_8), "AES");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final byte[] salt;

    public DevelopmentCryptoProvider(byte[] salt) {
        this.salt = salt;
    }

    @Override
    public EncryptedData encrypt(CharSequence plaintext, boolean deterministic) {
        if (plaintext.length() == 0) {
            return new EncryptedData(new byte[0], KEY_ID, new byte[0]);
        }
        try {
            byte[] bytes = plaintext.toString().getBytes(StandardCharsets.UTF_8);
            byte[] iv = deterministic ? Arrays.copyOf(MessageDigest.getInstance("SHA-256").digest(bytes), 16) : randomIv();
            Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aes.init(Cipher.ENCRYPT_MODE, KEY, new IvParameterSpec(iv));
            return new EncryptedData(aes.doFinal(bytes), KEY_ID, iv);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String decrypt(EncryptedData data) {
        if (data.ciphertext().length == 0) {
            return "";
        }
        if (!KEY_ID.equals(data.keyId())) {
            throw new IllegalStateException("Unknown key ID: " + data.keyId());
        }
        try {
            Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aes.init(Cipher.DECRYPT_MODE, KEY, new IvParameterSpec(data.iv()));
            return new String(aes.doFinal(data.ciphertext()), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public BlindIndex computeBlindIndex(CharSequence plaintext) {
        return BlindIndex.compute(plaintext.toString(), salt);
    }

    private static byte[] randomIv() {
        byte[] iv = new byte[16];
        RANDOM.nextBytes(iv);
        return iv;
    }
}
