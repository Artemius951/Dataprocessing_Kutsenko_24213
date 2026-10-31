package ru.nsu.kutsenko.J3;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpClientService {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpClientService(String host, int port) {
        this.baseUrl = "http://" + host + ":" + port;

        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

        this.objectMapper = new ObjectMapper();
    }

    public ServerResponse get(String path)
        throws IOException, InterruptedException {

        String normalizedPath = normalizePath(path);

        URI uri = URI.create(baseUrl + normalizedPath);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(Duration.ofSeconds(20))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                "HTTP status " + response.statusCode()
            );
        }

        return objectMapper.readValue(
            response.body(),
            ServerResponse.class
        );
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }

        return path.startsWith("/")
            ? path
            : "/" + path;
    }
}