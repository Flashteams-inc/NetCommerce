package com.netcommerce.kernel.persistence.kernel;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.netcommerce.kernel.compliance.audit.AuditRepository;
import com.netcommerce.kernel.compliance.encryption.CryptoProvider;
import com.netcommerce.kernel.persistence.audit.AuditLogRecords;
import com.netcommerce.kernel.persistence.audit.JpaAuditRepository;

@SpringBootApplication
@EntityScan(basePackages = {"com.netcommerce.kernel.persistence.kernel", "com.netcommerce.kernel.persistence.audit", "com.netcommerce.kernel.persistence"})
@EnableJpaRepositories(basePackageClasses = AuditLogRecords.class)
public class KernelTestApplication {

    @Bean
    MutableTenantContext tenantContext() {
        return new MutableTenantContext();
    }

    @Bean
    CryptoProvider cryptoProvider() {
        return new DevelopmentCryptoProvider("pepper-salt".getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    AuditRepository auditRepository(AuditLogRecords records) {
        return new JpaAuditRepository(records);
    }
}
