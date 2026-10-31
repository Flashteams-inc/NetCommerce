package com.netcommerce.kernel.compliance.encryption;

import com.netcommerce.kernel.core.encryption.BlindIndex;
import com.netcommerce.kernel.core.encryption.EncryptedData;

/** Encryption itself: fast and synchronous; implemented in an adapter. */
public interface CryptoProvider {

    EncryptedData encrypt(CharSequence plaintext, boolean deterministic);

    String decrypt(EncryptedData data);

    /** HMAC-SHA256 of the text for searching: the same input and salt give the same index. */
    BlindIndex computeBlindIndex(CharSequence plaintext);
}
