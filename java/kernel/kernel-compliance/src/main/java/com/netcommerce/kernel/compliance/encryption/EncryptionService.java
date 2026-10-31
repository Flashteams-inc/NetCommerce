package com.netcommerce.kernel.compliance.encryption;

import com.netcommerce.kernel.core.encryption.BlindIndex;
import com.netcommerce.kernel.core.encryption.EncryptedData;

/** Encryption for application code and the persistence converters (IEncryptionService). */
public interface EncryptionService {

    EncryptedData encrypt(String plaintext, boolean deterministic);

    String decrypt(EncryptedData encryptedData);

    BlindIndex computeBlindIndex(String plaintext);
}
