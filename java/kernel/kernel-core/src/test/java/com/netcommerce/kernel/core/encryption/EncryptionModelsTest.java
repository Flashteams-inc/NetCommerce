package com.netcommerce.kernel.core.encryption;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Encrypted values are stored in exactly the .NET format, and blind indexes are
 * the same HMAC: expected strings captured from the .NET kernel.
 */
class EncryptionModelsTest {

    @Test
    void theStorageFormatIsTheDotNetOne() {
        EncryptedData data = new EncryptedData(new byte[] {1, 2, 3, (byte) 250}, "key-7", new byte[] {9, 8, 7},
                new byte[] {4, 5}, 2, "AES-256-GCM", 3);
        assertThat(data.toStorageFormat()).isEqualTo("v2|aAES-256-GCM:3|kkey-7|CQgH|AQID+g==|BAU=");
        EncryptedData back = EncryptedData.fromStorageFormat(data.toStorageFormat());
        assertThat(back.keyId()).isEqualTo("key-7");
        assertThat(back.version()).isEqualTo(2);
        assertThat(back.algorithmVersion()).isEqualTo(3);
        assertThat(back.ciphertext()).containsExactly(1, 2, 3, (byte) 250);
        assertThat(back.encryptedDek()).containsExactly(4, 5);
    }

    @Test
    void anOlderFivePartValueStillReads() {
        EncryptedData legacy = EncryptedData.fromStorageFormat("v1|x|kold|AQID|BAUG");
        assertThat(legacy.keyId()).isEqualTo("old");
        assertThat(legacy.algorithmType()).isEqualTo("AES-256-GCM");
        assertThat(legacy.encryptedDek()).isNull();
    }

    @Test
    void theBlindIndexIsTheSameHmacAsDotNets() {
        assertThat(BlindIndex.compute("jane@example.com", "pepper-salt".getBytes(StandardCharsets.UTF_8)).value())
                .isEqualTo("Mf7K7pUnIV0TDZk7ugR9i8qKdTIBwJk7yb9oA99xF6w=");
    }
}
