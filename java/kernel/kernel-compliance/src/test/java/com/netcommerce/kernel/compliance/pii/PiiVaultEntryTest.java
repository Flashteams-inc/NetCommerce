package com.netcommerce.kernel.compliance.pii;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** The PII vault entry keeps the .NET rules for creating, deleting and purging. */
class PiiVaultEntryTest {

    private static PiiVaultEntry entry() {
        return PiiVaultEntry.create(UUID.randomUUID(), "user-1", "enc-name", "enc-mail", "mail-index",
                "enc-phone", "phone-index", "enc-address", null, null, 1, "tenant-a");
    }

    @Test
    void requiredFieldsAreChecked() {
        assertThatThrownBy(() -> PiiVaultEntry.create(new UUID(0, 0), "user-1", "n", "e", "i", "p", "pi", "a", null, null, 1, null))
                .hasMessage("Profile ID cannot be empty. (Parameter 'profileId')");
        assertThatThrownBy(() -> PiiVaultEntry.create(UUID.randomUUID(), "user-1", "n", "e", " ", "p", "pi", "a", null, null, 1, null))
                .hasMessage("Email blind index is required. (Parameter 'emailBlindIndex')");
    }

    @Test
    void aPurgeOnlyFollowsADeletionAndBreaksTheProfileLink() {
        PiiVaultEntry entry = entry();
        assertThatThrownBy(entry::purgeData).hasMessage("Cannot purge data that is not marked as deleted.");
        entry.markAsDeleted();
        assertThatThrownBy(entry::markAsDeleted).hasMessage("PII entry is already deleted.");
        entry.purgeData();
        assertThat(entry.getProfileId()).isEqualTo(new UUID(0, 0));
        assertThat(entry.getEncryptedEmail()).isNotEqualTo("enc-mail");
        assertThat(entry.getDeletedBy()).isEqualTo("system");
        assertThat(entry.getTenantId()).isEqualTo("tenant-a");
    }

    static class Customer {
        @PiiSensitive(deterministic = true, blindIndexColumnName = "email_blind_index")
        String email;
    }

    @Test
    void thePiiMarkerIsReadableAtRuntime() throws Exception {
        Field email = Customer.class.getDeclaredField("email");
        PiiSensitive pii = email.getAnnotation(PiiSensitive.class);
        assertThat(pii.deterministic()).isTrue();
        assertThat(pii.blindIndexColumnName()).isEqualTo("email_blind_index");
    }
}
