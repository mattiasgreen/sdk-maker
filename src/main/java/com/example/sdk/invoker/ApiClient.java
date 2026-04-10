package com.example.sdk.invoker;

import java.net.http.HttpClient;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class ApiClient {
    private final HttpClient httpClient;
    private final JSON json;
    private final String basePath;

    public ApiClient(JSON json, String basePath) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), json, basePath);
    }

    public ApiClient(HttpClient httpClient, JSON json, String basePath) {
        this.httpClient = httpClient;
        this.json = json;
        this.basePath = basePath;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public JSON getJson() {
        return json;
    }

    public String getBasePath() {
        return basePath;
    }

    public static String urlEncode(String s) {
        if (s == null) return "";
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
