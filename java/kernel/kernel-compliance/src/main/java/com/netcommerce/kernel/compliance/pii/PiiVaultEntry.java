package com.netcommerce.kernel.compliance.pii;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import com.netcommerce.kernel.core.ArgumentException;

/**
 * A person's personal data, each field encrypted, kept apart from their profile
 * (PiiVaultEntry.cs). Purging after deletion overwrites every field and breaks
 * the link to the profile.
 */
public final class PiiVaultEntry extends PiiVaultEntryBase {

    private static final SecureRandom RANDOM = new SecureRandom();

    private String encryptedFullName = "";
    private String encryptedEmail = "";
    private String emailBlindIndex = "";
    private String encryptedPhoneNumber = "";
    private String phoneBlindIndex = "";
    private String encryptedAddress = "";
    private String encryptedDateOfBirth;
    private String encryptedNationalId;

    /** For the persistence layer. */
    protected PiiVaultEntry() {
    }

    private PiiVaultEntry(UUID profileId, String userId, String encryptedFullName, String encryptedEmail,
                          String emailBlindIndex, String encryptedPhoneNumber, String phoneBlindIndex,
                          String encryptedAddress, String encryptedDateOfBirth, String encryptedNationalId,
                          int keyVersion, String tenantId) {
        super(profileId, userId);
        this.tenantId = tenantId != null ? tenantId : "";
        this.encryptedFullName = encryptedFullName;
        this.encryptedEmail = encryptedEmail;
        this.emailBlindIndex = emailBlindIndex;
        this.encryptedPhoneNumber = encryptedPhoneNumber;
        this.phoneBlindIndex = phoneBlindIndex;
        this.encryptedAddress = encryptedAddress;
        this.encryptedDateOfBirth = encryptedDateOfBirth;
        this.encryptedNationalId = encryptedNationalId;
        this.keyVersion = keyVersion;
    }

    public static PiiVaultEntry create(UUID profileId, String userId, String encryptedFullName, String encryptedEmail,
                                       String emailBlindIndex, String encryptedPhoneNumber, String phoneBlindIndex,
                                       String encryptedAddress, String encryptedDateOfBirth,
                                       String encryptedNationalId, int keyVersion, String tenantId) {
        if (profileId == null || new UUID(0L, 0L).equals(profileId)) {
            throw new ArgumentException("Profile ID cannot be empty.", "profileId");
        }
        if (userId == null || userId.isBlank()) {
            throw new ArgumentException("User ID is required.", "userId");
        }
        if (encryptedFullName == null || encryptedFullName.isBlank()) {
            throw new ArgumentException("Encrypted full name is required.", "encryptedFullName");
        }
        if (encryptedEmail == null || encryptedEmail.isBlank()) {
            throw new ArgumentException("Encrypted email is required.", "encryptedEmail");
        }
        if (emailBlindIndex == null || emailBlindIndex.isBlank()) {
            throw new ArgumentException("Email blind index is required.", "emailBlindIndex");
        }
        return new PiiVaultEntry(profileId, userId, encryptedFullName, encryptedEmail, emailBlindIndex,
                encryptedPhoneNumber, phoneBlindIndex, encryptedAddress, encryptedDateOfBirth, encryptedNationalId,
                keyVersion, tenantId);
    }

    public void update(String encryptedFullName, String encryptedEmail, String emailBlindIndex,
                       String encryptedPhoneNumber, String phoneBlindIndex, String encryptedAddress) {
        this.encryptedFullName = encryptedFullName;
        this.encryptedEmail = encryptedEmail;
        this.emailBlindIndex = emailBlindIndex;
        this.encryptedPhoneNumber = encryptedPhoneNumber;
        this.phoneBlindIndex = phoneBlindIndex;
        this.encryptedAddress = encryptedAddress;
        markAsUpdated();
    }

    @Override
    public void markAsDeleted() {
        if (isDeleted()) {
            throw new IllegalStateException("PII entry is already deleted.");
        }
        super.markAsDeleted();
    }

    /** Overwrites every field with random data and breaks the profile link; only once deleted. */
    public void purgeData() {
        if (!isDeleted()) {
            throw new IllegalStateException("Cannot purge data that is not marked as deleted.");
        }
        encryptedFullName = randomBase64();
        encryptedEmail = randomBase64();
        emailBlindIndex = randomBase64();
        encryptedPhoneNumber = randomBase64();
        phoneBlindIndex = randomBase64();
        encryptedAddress = randomBase64();
        encryptedDateOfBirth = randomBase64();
        encryptedNationalId = randomBase64();
        profileId = new UUID(0L, 0L);
    }

    public void reEncrypt(String newEncryptedFullName, String newEncryptedEmail, String newEncryptedPhoneNumber,
                          String newEncryptedAddress, String newEncryptedDateOfBirth, String newEncryptedNationalId,
                          int newKeyVersion) {
        encryptedFullName = newEncryptedFullName;
        encryptedEmail = newEncryptedEmail;
        encryptedPhoneNumber = newEncryptedPhoneNumber;
        encryptedAddress = newEncryptedAddress;
        encryptedDateOfBirth = newEncryptedDateOfBirth;
        encryptedNationalId = newEncryptedNationalId;
        keyVersion = newKeyVersion;
        markAsUpdated();
    }

    private static String randomBase64() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(ByteBuffer.wrap(bytes).array());
    }

    public String getEncryptedFullName() {
        return encryptedFullName;
    }

    public String getEncryptedEmail() {
        return encryptedEmail;
    }

    public String getEmailBlindIndex() {
        return emailBlindIndex;
    }

    public String getEncryptedPhoneNumber() {
        return encryptedPhoneNumber;
    }

    public String getPhoneBlindIndex() {
        return phoneBlindIndex;
    }

    public String getEncryptedAddress() {
        return encryptedAddress;
    }

    public String getEncryptedDateOfBirth() {
        return encryptedDateOfBirth;
    }

    public String getEncryptedNationalId() {
        return encryptedNationalId;
    }
}
