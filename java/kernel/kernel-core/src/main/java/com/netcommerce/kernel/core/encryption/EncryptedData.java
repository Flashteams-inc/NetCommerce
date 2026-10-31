package com.netcommerce.kernel.core.encryption;

import java.util.Base64;

/**
 * An encrypted value and what decrypting it needs, stored as one string in the
 * format the .NET EncryptedData writes:
 * "v{version}|a{algorithm}:{algorithmVersion}|k{keyId}|{iv}|{ciphertext}|{encryptedDek}",
 * base64 parts, the DEK empty when there is none. Older five-part values (no
 * algorithm part) still read.
 */
public record EncryptedData(byte[] ciphertext, String keyId, byte[] iv, byte[] encryptedDek, int version,
                            String algorithmType, int algorithmVersion) {

    public static final String DEFAULT_ALGORITHM = "AES-256-GCM";

    public EncryptedData(byte[] ciphertext, String keyId, byte[] iv) {
        this(ciphertext, keyId, iv, null, 1, DEFAULT_ALGORITHM, 1);
    }

    public String toStorageFormat() {
        Base64.Encoder b64 = Base64.getEncoder();
        String dek = encryptedDek != null ? b64.encodeToString(encryptedDek) : "";
        return "v" + version + "|a" + algorithmType + ":" + algorithmVersion + "|k" + keyId + "|"
                + b64.encodeToString(iv) + "|" + b64.encodeToString(ciphertext) + "|" + dek;
    }

    public static EncryptedData fromStorageFormat(String storageFormat) {
        String[] parts = storageFormat.split("\\|", -1);
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid encrypted storage format.");
        }
        int version = 1;
        String algorithmType = DEFAULT_ALGORITHM;
        int algorithmVersion = 1;
        if (parts[0].startsWith("v")) {
            version = Integer.parseInt(parts[0].substring(1));
        }
        if (parts.length >= 6 && parts[1].startsWith("a")) {
            String[] algorithm = parts[1].substring(1).split(":", -1);
            if (algorithm.length == 2) {
                algorithmType = algorithm[0];
                algorithmVersion = Integer.parseInt(algorithm[1]);
            }
        }
        if (!parts[2].startsWith("k")) {
            throw new IllegalArgumentException("Invalid key ID format");
        }
        Base64.Decoder b64 = Base64.getDecoder();
        byte[] dek = parts.length >= 6 && !parts[5].isEmpty() ? b64.decode(parts[5]) : null;
        return new EncryptedData(b64.decode(parts[4]), parts[2].substring(1), b64.decode(parts[3]), dek,
                version, algorithmType, algorithmVersion);
    }
}
