package com.netcommerce.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netcommerce.kernel.core.ids.StronglyTypedId;

/** The application's ObjectMapper writes a strongly typed id as its bare GUID. */
@JsonTest
@Import(JsonConfiguration.class)
class JsonConfigurationTest {

    record OrderId(UUID value) implements StronglyTypedId<OrderId> {
    }

    @Autowired
    private ObjectMapper json;

    @Test
    void theApplicationsMapperWritesIdsAsBareGuids() throws Exception {
        UUID guid = UUID.fromString("3f2a8b9c-1d4e-4f6a-8b7c-9d0e1f2a3b4c");
        assertThat(json.writeValueAsString(new OrderId(guid))).isEqualTo("\"3f2a8b9c-1d4e-4f6a-8b7c-9d0e1f2a3b4c\"");
    }
}
