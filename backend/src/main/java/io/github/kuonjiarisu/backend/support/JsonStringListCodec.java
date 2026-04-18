package io.github.kuonjiarisu.backend.support;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonStringListCodec {

    private static final Logger log = LoggerFactory.getLogger(JsonStringListCodec.class);

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public JsonStringListCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<String> parse(String valueJson) {
        if (valueJson == null || valueJson.isBlank()) {
            return List.of();
        }

        try {
            return DomainSupport.normalizeList(objectMapper.readValue(valueJson, STRING_LIST_TYPE));
        } catch (JsonProcessingException exception) {
            log.warn(
                "Failed to parse string list JSON, using empty list: length={} reason={}",
                valueJson.length(),
                exception.getOriginalMessage()
            );
            return List.of();
        }
    }

    public String serialize(List<String> values) {
        try {
            return objectMapper.writeValueAsString(DomainSupport.normalizeList(values));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("字符串列表格式无效", exception);
        }
    }
}
