package xyz.pobob.barebonesvc.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Util {
    public static final Random RANDOM = new Random();

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static CompletableFuture<HttpResponse<String>> httpRequestAsync(String method, String url, String body, Map<String, String> headers, Executor executor) {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30));

        if (headers != null) {
            headers.forEach(builder::header);
        }

        switch (method.toUpperCase()) {
            case "GET" -> builder.GET();
            case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
            case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
            case "DELETE" -> {
                if (body == null) {
                    builder.DELETE();
                } else {
                    builder.method("DELETE",
                            HttpRequest.BodyPublishers.ofString(body));
                }
            }
            case "PATCH" -> builder.method("PATCH",
                    HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                return null;
            }
        }, executor);
    }
}
