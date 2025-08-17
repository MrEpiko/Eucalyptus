package me.mrepiko.eucalyptus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NonNull;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class HttpRequestImpl implements HttpRequest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Nullable private Response response;

    @Getter private final String url;
    @Getter private final HttpMethod method;

    @Getter @Nullable private final String body;
    @Nullable private final MediaType bodyMediaType;
    @Getter @Nullable private final Map<String, String> headers;
    @Getter @Nullable private final Map<String, String> params;
    @Nullable private final OkHttpClient client;

    HttpRequestImpl(@NonNull Builder builder) {
        this.url = builder.getUrl();
        this.method = builder.getMethod();
        this.body = builder.getBody();
        this.bodyMediaType = builder.getBodyMediaType();
        this.headers = builder.getHeaders();
        this.params = builder.getParams();
        this.client = builder.getClient();
    }

    // Method implementations

    @Override
    @NonNull
    public OkHttpClient getClient() {
        return (client != null) ? client : DefaultHttpRequestConfig.getDefaultClient();
    }

    @Override
    @Nullable
    public JsonNode getBodyAsNode() throws JsonProcessingException {
        if (body == null) return null;
        return mapper.readTree(body);
    }

    @Override
    @Nullable
    public MediaType getBodyMediaType() {
        return (bodyMediaType != null) ? bodyMediaType : DefaultHttpRequestConfig.getDefaultMediaType();
    }

    @Nullable
    @Override
    public Response getRawResponse() {
        return response;
    }

    @Override
    @NonNull
    public HttpResponse execute() throws IOException {
        Call call = getCall();
        response = call.execute();
        return getResponse(response);
    }

    @Override
    @NonNull
    public CompletableFuture<HttpResponse> executeAsync() {
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();

        Call call = getCall();
        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    HttpResponse httpResponse = getResponse(response);
                    future.complete(httpResponse);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    @Override
    public void executeAsync(@NonNull Consumer<HttpResponse> consumer) {
        executeAsync().whenComplete((resp, ex) -> {
            if (ex != null) consumer.accept(new HttpResponse(520, null, null));
            else consumer.accept(resp);
        });
    }

    @Override
    public void close() {
        if (response == null) return;
        response.close();
        response = null;
    }

    // Helper methods

    @NonNull
    private Call getCall() {
        Request.Builder builder = new Request.Builder();
        setupMethodAndBody(builder);
        setupParams(builder);
        setupHeaders(builder);

        Request request = builder.build();
        return getClient().newCall(request);
    }

    @NonNull
    private HttpResponse getResponse(@NonNull Response response) throws IOException {
        this.response = response;
        String responseBody = (response.body() == null) ? null : response.body().string();
        return new HttpResponse(response.code(), responseBody, getHeaders(response.headers()));
    }

    private void setupMethodAndBody(@NonNull Request.Builder builder) {
        switch (method) {
            case GET -> builder.get();
            case POST -> builder.post(getRequestBody());
            case PUT -> builder.put(getRequestBody());
            case PATCH -> builder.patch(getRequestBody());
            case DELETE -> builder.delete(getRequestBody());
            case HEAD -> builder.head();
            case OPTIONS -> builder.method("OPTIONS", getRequestBody());
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }

    private void setupParams(@NonNull Request.Builder builder) {
        if (params == null) {
            builder.url(url);
            return;
        }

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) -> {
            urlBuilder.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
            urlBuilder.append("=");
            urlBuilder.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            urlBuilder.append("&");
        });
        urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        builder.url(urlBuilder.toString());
    }

    private void setupHeaders(@NonNull Request.Builder builder) {
        if (headers == null) return;
        headers.forEach(builder::addHeader);
    }

    @NonNull
    private RequestBody getRequestBody() {
        return RequestBody.create(Objects.requireNonNullElse(body, ""), getBodyMediaType());
    }

    @Nullable
    private HashMap<String, String> getHeaders(@Nullable Headers headers) {
        if (headers == null) return null;
        HashMap<String, String> headersMap = new HashMap<>();
        headers.forEach(x -> headersMap.put(x.getFirst(), x.getSecond()));
        return headersMap;
    }

}
