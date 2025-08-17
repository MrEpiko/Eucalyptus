# Eucalyptus

A lightweight, fluent wrapper around [OkHttp](https://square.github.io/okhttp/) for building and sending HTTP requests in Java.

## Features

- Simple, fluent API for constructing requests
- Support for global (default) and per-request configuration
- Easy addition of custom headers, query parameters, and request bodies
- Synchronous and asynchronous execution methods
- Built-in JSON parsing support via Jackson

## Installation

_Coming soon..._ 

## Quick Example

```java
HttpRequest request = HttpRequest.Builder.create("https://api.example.com/data", HttpMethod.GET)
    .addHeader("Authorization", "Bearer token")
    .addParam("limit", "10")
    .build();

try (request) {
    HttpResponse response = request.execute();
    System.out.println("Status: " + response.getStatusCode());
    System.out.println("Body: " + response.getBody());
}
