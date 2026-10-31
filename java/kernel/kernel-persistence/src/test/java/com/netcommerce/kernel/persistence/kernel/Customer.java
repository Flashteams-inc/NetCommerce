package com.netcommerce.kernel.persistence.kernel;

import java.util.UUID;

import com.netcommerce.kernel.compliance.pii.PiiSensitive;
import com.netcommerce.kernel.core.domain.Entity;
import com.netcommerce.kernel.persistence.DeterministicPiiEncryptionConverter;
import com.netcommerce.kernel.persistence.PiiEncryptionConverter;

import jakarta.persistence.Convert;
import jakarta.persistence.Table;

@jakarta.persistence.Entity
@Table(name = "customers")
public class Customer extends Entity<UUID> {

    @PiiSensitive(deterministic = true)
    @Convert(converter = DeterministicPiiEncryptionConverter.class)
    private String email;

    @PiiSensitive
    @Convert(converter = PiiEncryptionConverter.class)
    private String fullName;

    protected Customer() {
    }

    public Customer(String email, String fullName) {
        super(UUID.randomUUID());
        this.email = email;
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }
}
