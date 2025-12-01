package com.hyperativa.be.config.security.sanitization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

public abstract class SanitizingRequestDeserializer<T> extends StdDeserializer<T> {

    private final Class<T> recordClass;

    protected SanitizingRequestDeserializer(Class<T> recordClass) {
        super(recordClass);
        this.recordClass = recordClass;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        var codec = p.getCodec();
        JsonNode node = codec.readTree(p);

        try {
            var components = recordClass.getRecordComponents();
            List<Object> args = new ArrayList<>(components.length);

            for (RecordComponent component : components) {
                var name = component.getName();
                JsonNode valueNode = node.get(name);

                if (valueNode == null || valueNode.isNull()) {
                    args.add(null);
                    continue;
                }

                var type = component.getType();

                if (type == String.class) {
                    var raw = valueNode.asText();
                    var sanitized = SanitizerUtils.sanitize(raw);
                    args.add(sanitized);
                } else if (type == int.class || type == Integer.class) {
                    args.add(valueNode.asInt());
                } else if (type == boolean.class || type == Boolean.class) {
                    args.add(valueNode.asBoolean());
                } else {
                    var value = codec.treeToValue(valueNode, type);
                    args.add(value);
                }
            }

            var ctor = recordClass.getDeclaredConstructor(
                    List.of(recordClass.getRecordComponents()).stream()
                            .map(RecordComponent::getType)
                            .toArray(Class[]::new)
            );
            return ctor.newInstance(args.toArray());

        } catch (Exception e) {
            // TODO - create a dedocated exception
            throw new RuntimeException("Failed to deserialize and sanitize record " + recordClass.getSimpleName(), e);
        }
    }
}
