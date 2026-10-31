package com.netcommerce.kernel.persistence;

import com.netcommerce.kernel.compliance.encryption.CryptoProvider;
import com.netcommerce.kernel.core.encryption.EncryptedData;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Encrypts a @PiiSensitive string when stored and decrypts it when read, as
 * EF Core's PiiEncryptionConverter: an empty value stays empty, a stored value
 * in the EncryptedData format is decrypted, and anything else (stored before
 * encryption) is read as it is. Random IVs; see DeterministicPiiEncryptionConverter.
 */
@Converter
public class PiiEncryptionConverter implements AttributeConverter<String, String> {

    private final CryptoProvider crypto;
    private final boolean deterministic;

    public PiiEncryptionConverter(CryptoProvider crypto) {
        this(crypto, false);
    }

    protected PiiEncryptionConverter(CryptoProvider crypto, boolean deterministic) {
        this.crypto = crypto;
        this.deterministic = deterministic;
    }

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return "";
        }
        return crypto.encrypt(plaintext, deterministic).toStorageFormat();
    }

    @Override
    public String convertToEntityAttribute(String stored) {
        if (stored == null || stored.isEmpty()) {
            return "";
        }
        if (!stored.startsWith("v")) {
            return stored;
        }
        return crypto.decrypt(EncryptedData.fromStorageFormat(stored));
    }
}
