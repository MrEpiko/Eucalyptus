package me.mrepiko.eucalyptus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

@Getter
@AllArgsConstructor
public class HttpResponse {

    private final int statusCode;
    @Nullable private final String body;
    @Nullable private final HashMap<String, String> headers;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Nullable
    public JsonNode getBodyAsJsonNode() throws JsonProcessingException {
        if (body == null) return null;
        return mapper.readTree(body);
    }

}
