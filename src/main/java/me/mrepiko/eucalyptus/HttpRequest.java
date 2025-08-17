package me.mrepiko.eucalyptus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import lombok.Getter;
import lombok.NonNull;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface HttpRequest extends Closeable {
    @NonNull
    HttpMethod getMethod();
    @NonNull
    String getUrl();
    @Nullable
    String getBody();
    @Nullable
    MediaType getBodyMediaType();
    @Nullable
    JsonNode getBodyAsNode() throws JsonProcessingException;
    @Nullable
    Map<String, String> getHeaders();
    @Nullable
    Map<String, String> getParams();
    @NonNull
    OkHttpClient getClient();
    @Nullable
    Response getRawResponse();
    @NonNull
    HttpResponse execute() throws Exception;
    @NonNull
    CompletableFuture<HttpResponse> executeAsync() throws Exception;
    void executeAsync(@NonNull Consumer<HttpResponse> consumer);

    @Getter
    class Builder {

        private final String url;
        private final HttpMethod method;

        @Nullable private String body;
        @Nullable private MediaType bodyMediaType;
        @Nullable private Map<String, String> headers;
        @Nullable private Map<String, String> params;
        @Nullable private OkHttpClient client;

        private Builder(@NonNull String url, @NonNull HttpMethod method) {
            this.url = url;
            this.method = method;
        }

        public static Builder create(@NonNull String url, @NonNull HttpMethod method) {
            return new Builder(url, method);
        }

        public Builder setBody(@NonNull String body) {
            this.body = body;
            return this;
        }

        public Builder setBody(@NonNull ContainerNode<?> body) {
            this.body = body.toString();
            return this;
        }

        public Builder setBodyMediaType(@NonNull String mediaType) {
            this.bodyMediaType = MediaType.parse(mediaType);
            if (this.bodyMediaType == null) throw new IllegalArgumentException("Invalid media type: " + mediaType);
            return this;
        }

        public Builder addHeader(@NonNull String key, @NonNull String value) {
            if (headers == null) headers = new HashMap<>();
            headers.put(key, value);
            return this;
        }

        public Builder addParam(@NonNull String key, @NonNull String value) {
            if (params == null) params = new HashMap<>();
            params.put(key, value);
            return this;
        }

        public Builder setClient(@NonNull OkHttpClient client) {
            this.client = client;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequestImpl(this);
        }

    }

}
