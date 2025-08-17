package me.mrepiko.eucalyptus.httprequest;

import me.mrepiko.eucalyptus.HttpMethod;
import me.mrepiko.eucalyptus.HttpRequest;
import me.mrepiko.eucalyptus.HttpResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestUsageTest {

    private MockWebServer mockServer;

    @BeforeEach
    void setup() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterEach
    void shutdown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void testHttpRequestExecutionWithParamsAndHeaders() throws Exception {
        String expectedBody = "{\"message\":\"success\"}";
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(expectedBody)
                .addHeader("Content-Type", "application/json"));

        String url = mockServer.url("/data").toString();

        HttpRequest request = HttpRequest.Builder.create(url, HttpMethod.GET)
                .addHeader("Authorization", "Bearer token")
                .addParam("limit", "10")
                .build();

        HttpResponse response;
        try (request) {
            response = request.execute();
        }

        RecordedRequest recordedRequest = mockServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/data?limit=10", recordedRequest.getPath());
        assertEquals("Bearer token", recordedRequest.getHeader("Authorization"));

        assertEquals(200, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
        assertNotNull(response.getHeaders());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void testPostRequestWithJsonBody() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(201));

        String url = mockServer.url("/submit").toString();

        String jsonBody = "{\"name\":\"test\"}";
        HttpRequest request = HttpRequest.Builder.create(url, HttpMethod.POST)
                .setBody(jsonBody)
                .setBodyMediaType("application/json; charset=utf-8")
                .build();

        try (request) {
            HttpResponse response = request.execute();
            assertEquals(201, response.getStatusCode());
        }

        RecordedRequest recordedRequest = mockServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals(jsonBody, recordedRequest.getBody().readUtf8());
        assertEquals("application/json; charset=utf-8", recordedRequest.getHeader("Content-Type"));
    }

    @Test
    void testRequestWithDefaultClientAndMediaType() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(200));

        String url = mockServer.url("/default").toString();

        HttpRequest request = HttpRequest.Builder.create(url, HttpMethod.POST)
                .setBody("test")
                .build();

        try (request) {
            HttpResponse response = request.execute();
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    void testAsyncExecution() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("async"));

        String url = mockServer.url("/async").toString();

        HttpRequest request = HttpRequest.Builder.create(url, HttpMethod.GET).build();

        HttpResponse[] result = new HttpResponse[1];
        try (request) {
            request.executeAsync(r -> result[0] = r);
            Thread.sleep(100);
        }

        assertNotNull(result[0]);
        assertEquals(200, result[0].getStatusCode());
        assertEquals("async", result[0].getBody());
    }

    @Test
    void testInvalidMediaTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> HttpRequest.Builder.create("https://localhost", HttpMethod.POST)
                .setBodyMediaType("invalid_type")
                .build());
    }

    @Test
    void testHeadersParsing() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("X-Test", "true"));

        String url = mockServer.url("/headers").toString();

        HttpRequest request = HttpRequest.Builder.create(url, HttpMethod.GET).build();
        HttpResponse response;
        try (request) {
            response = request.execute();
        }

        Map<String, String> headers = response.getHeaders();
        assertNotNull(headers);
        assertEquals("true", headers.get("X-Test"));
    }
}
