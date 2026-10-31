package com.netcommerce.kernel.compliance.encryption;

/**
 * The key management service (Azure Key Vault, AWS KMS, Vault or an HSM):
 * unwraps a data encryption key with the master key.
 */
public interface KeyManagementService {

    /** The data encryption key, unwrapped. The slow, remote call. */
    byte[] unwrapKey(String encryptedDekBase64);

    /** The currently active encrypted data encryption key. */
    String getActiveEncryptedDek();
}
