package com.netcommerce.kernel.core.ids;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson for strongly typed ids, as StronglyTypedIdJsonConverter: an id is
 * written as its bare GUID string, as a value and as a map key, and read back
 * from one; anything else fails with "Unable to convert JSON to {Type}".
 * Spring Boot registers every Module bean with its ObjectMapper.
 */
public class StronglyTypedIdModule extends SimpleModule {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public StronglyTypedIdModule() {
        super("StronglyTypedIdModule");
        addSerializer((Class) StronglyTypedId.class, new JsonSerializer<StronglyTypedId<?>>() {
            @Override
            public void serialize(StronglyTypedId<?> id, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeString(id.value().toString());
            }
        });
        addKeySerializer((Class) StronglyTypedId.class, new JsonSerializer<StronglyTypedId<?>>() {
            @Override
            public void serialize(StronglyTypedId<?> id, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeFieldName(id.value().toString());
            }
        });
        setDeserializers(new SimpleDeserializers() {
            @Override
            public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription bean) {
                return StronglyTypedId.class.isAssignableFrom(type.getRawClass()) ? new IdDeserializer(type.getRawClass()) : null;
            }
        });
        setKeyDeserializers(new SimpleKeyDeserializers() {
            @Override
            public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config, BeanDescription bean) {
                if (!StronglyTypedId.class.isAssignableFrom(type.getRawClass())) {
                    return null;
                }
                Class raw = type.getRawClass();
                return new KeyDeserializer() {
                    @Override
                    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
                        UUID value = parse(key);
                        if (value == null) {
                            return ctxt.handleWeirdKey(raw, key, "Unable to convert property name to " + raw.getSimpleName());
                        }
                        return StronglyTypedIds.create(raw, value);
                    }
                };
            }
        });
    }

    private static UUID parse(String text) {
        try {
            return text == null ? null : UUID.fromString(text.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static final class IdDeserializer extends JsonDeserializer<Object> {

        private final Class<?> type;

        private IdDeserializer(Class<?> type) {
            this.type = type;
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            UUID value = p.currentToken() == JsonToken.VALUE_STRING ? parse(p.getText()) : null;
            if (value == null) {
                return ctxt.handleWeirdStringValue(type, p.getText(), "Unable to convert JSON to " + type.getSimpleName());
            }
            return StronglyTypedIds.create((Class) type, value);
        }
    }
}
