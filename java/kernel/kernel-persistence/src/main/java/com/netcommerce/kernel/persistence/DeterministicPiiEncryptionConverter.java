package com.netcommerce.kernel.persistence;

import com.netcommerce.kernel.compliance.encryption.CryptoProvider;

import jakarta.persistence.Converter;

/** PII encryption for a field searched by equality: @PiiSensitive(deterministic = true). */
@Converter
public class DeterministicPiiEncryptionConverter extends PiiEncryptionConverter {

    public DeterministicPiiEncryptionConverter(CryptoProvider crypto) {
        super(crypto, true);
    }
}
