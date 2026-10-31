package com.netcommerce.kernel.core.ids;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An id is the bare GUID on the wire, as the .NET StronglyTypedIdJsonConverter
 * writes it (captured: "3f2a8b9c-1d4e-4f6a-8b7c-9d0e1f2a3b4c").
 */
class StronglyTypedIdJsonTest {

    record ProbeId(UUID value) implements StronglyTypedId<ProbeId> {
    }

    private static final UUID GUID = UUID.fromString("3f2a8b9c-1d4e-4f6a-8b7c-9d0e1f2a3b4c");
    private final ObjectMapper json = new ObjectMapper().registerModule(new StronglyTypedIdModule());

    @Test
    void anIdIsWrittenAsItsBareGuidAndReadBack() throws Exception {
        String written = json.writeValueAsString(new ProbeId(GUID));
        assertThat(written).isEqualTo("\"3f2a8b9c-1d4e-4f6a-8b7c-9d0e1f2a3b4c\"");
        assertThat(json.readValue(written, ProbeId.class)).isEqualTo(new ProbeId(GUID));
    }

    @Test
    void anIdAsAMapKeyIsItsGuidToo() throws Exception {
        String written = json.writeValueAsString(Map.of(new ProbeId(GUID), 1));
        assertThat(written).isEqualTo("{\"3f2a8b9c-1d4e-4f6a-8b7c-9d0e1f2a3b4c\":1}");
        Map<ProbeId, Integer> back = json.readValue(written, new TypeReference<Map<ProbeId, Integer>>() { });
        assertThat(back).containsEntry(new ProbeId(GUID), 1);
    }

    @Test
    void somethingThatIsNotAGuidFailsAsInDotNet() {
        assertThatThrownBy(() -> json.readValue("\"not-a-guid\"", ProbeId.class))
                .hasMessageContaining("Unable to convert JSON to ProbeId");
    }

    @Test
    void idsCompareByTheirGuidAndCanBeMadeNewOrEmpty() {
        assertThat(StronglyTypedIds.empty(ProbeId.class).value()).isEqualTo(new UUID(0L, 0L));
        assertThat(StronglyTypedIds.newId(ProbeId.class)).isNotEqualTo(StronglyTypedIds.newId(ProbeId.class));
        assertThat(new ProbeId(new UUID(0, 1)).compareTo(new ProbeId(new UUID(0, 2)))).isNegative();
    }
}
