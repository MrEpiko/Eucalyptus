package me.mrepiko.eucalyptus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface HttpRequest extends Closeable {
    @NotNull
    HttpMethod getMethod();
    @NotNull
    String getUrl();
    @Nullable
    RequestBody getBody();
    @Nullable
    String getStringBody();
    @Nullable
    MediaType getBodyMediaType();
    @Nullable
    JsonNode getBodyAsNode() throws JsonProcessingException;
    @Nullable
    Map<String, String> getHeaders();
    @Nullable
    Map<String, String> getParams();
    @NotNull
    OkHttpClient getClient();
    @Nullable
    Response getRawResponse();
    @NotNull
    HttpResponse execute() throws Exception;
    @NotNull
    CompletableFuture<HttpResponse> executeAsync() throws Exception;
    void executeAsync(@NotNull Consumer<HttpResponse> consumer);

    @Getter
    class Builder {

        private final String url;
        private final HttpMethod method;

        @Nullable private RequestBody body;
        @Nullable private String stringBody;
        @Nullable private MediaType bodyMediaType;
        @Nullable private Map<String, String> headers;
        @Nullable private Map<String, String> params;
        @Nullable private OkHttpClient client;

        private Builder(@NotNull String url, @NotNull HttpMethod method) {
            this.url = url;
            this.method = method;
        }

        public static Builder create(@NotNull String url, @NotNull HttpMethod method) {
            return new Builder(url, method);
        }

        public Builder setBody(@NotNull String stringBody) {
            this.stringBody = stringBody;
            return this;
        }

        public Builder setBody(@NotNull ContainerNode<?> body) {
            this.stringBody = body.toString();
            return this;
        }

        public Builder setBody(@NotNull RequestBody body) {
            this.body = body;
            return this;
        }

        public Builder setBodyMediaType(@NotNull String mediaType) {
            this.bodyMediaType = MediaType.parse(mediaType);
            if (this.bodyMediaType == null) throw new IllegalArgumentException("Invalid media type: " + mediaType);
            return this;
        }

        public Builder addHeader(@NotNull String key, @NotNull String value) {
            if (headers == null) headers = new HashMap<>();
            headers.put(key, value);
            return this;
        }

        public Builder addParam(@NotNull String key, @NotNull String value) {
            if (params == null) params = new HashMap<>();
            params.put(key, value);
            return this;
        }

        public Builder setClient(@NotNull OkHttpClient client) {
            this.client = client;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequestImpl(this);
        }

    }

}
