package xyz.pobob.barebonesvc.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class Util {
    public static final Random RANDOM = new Random();

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static CompletableFuture<HttpResponse<String>> httpRequestAsync(String method, String url, String body, Map<String, String> headers) {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30));

        if (headers != null) {
            headers.forEach(builder::header);
        }

        switch (method.toUpperCase()) {
            case "GET":
                builder.GET();
                break;

            case "POST":
                builder.POST(HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
                break;

            case "PUT":
                builder.PUT(HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
                break;

            case "DELETE":
                if (body == null) {
                    builder.DELETE();
                } else {
                    builder.method("DELETE",
                            HttpRequest.BodyPublishers.ofString(body));
                }
                break;

            case "PATCH":
                builder.method("PATCH",
                        HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
                break;

            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        return CLIENT.sendAsync(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }
}
