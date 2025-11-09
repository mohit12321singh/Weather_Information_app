package com.weatherapp.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WeatherService {
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";

    // Fallback API key provided (used only if env var / system property are not set)
    private static final String FALLBACK_API_KEY = "b312b3652e3e574264b3f80898c65510";

    // Fetch API key from environment or system property; fall back to provided key.
    private static String getApiKey() {
        String key = System.getenv("OPENWEATHER_API_KEY");
        if (key == null || key.isBlank()) {
            key = System.getProperty("openweather.api.key");
        }
        if (key == null || key.isBlank()) {
            // Use fallback key supplied
            key = FALLBACK_API_KEY;
        }
        return key;
    }

    public JsonObject getWeather(String city) throws Exception {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City parameter is required");
        }
        // Allow a local "mock" mode for development when real API keys are not available.
        // Set environment variable WEATHERAPP_MOCK=true to enable. This returns a static
        // sample response that matches OpenWeather's structure.
        String mockFlag = System.getenv("WEATHERAPP_MOCK");
        if (mockFlag != null && (mockFlag.equalsIgnoreCase("1") || mockFlag.equalsIgnoreCase("true"))) {
            String sample = "{\"coord\":{\"lon\":-0.1257,\"lat\":51.5085},\"weather\":[{\"id\":804,\"main\":\"Clouds\",\"description\":\"overcast clouds\",\"icon\":\"04d\"}],\"base\":\"stations\",\"main\":{\"temp\":17.18,\"feels_like\":17.04,\"temp_min\":16.45,\"temp_max\":17.52,\"pressure\":1005,\"humidity\":80},\"visibility\":10000,\"wind\":{\"speed\":4.02,\"deg\":244},\"clouds\":{\"all\":100},\"dt\":1762437181,\"sys\":{\"country\":\"GB\"},\"timezone\":0,\"id\":2643743,\"name\":\"London\",\"cod\":200}";
            return JsonParser.parseString(sample).getAsJsonObject();
        }

        String apiKey = getApiKey();
        String encodedCity = URLEncoder.encode(city.trim(), StandardCharsets.UTF_8);
        String url = String.format("%s?q=%s&appid=%s&units=metric", API_URL, encodedCity, apiKey);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            return httpClient.execute(request, (ClassicHttpResponse httpResponse) -> {
                int status = httpResponse.getCode();
                String body = httpResponse.getEntity() != null ? EntityUtils.toString(httpResponse.getEntity()) : "";
                if (status != 200) {
                    // Include status and body to help debug "API key not working" (401 responses, etc.)
                    throw new RuntimeException("Error fetching weather: HTTP " + status + " - " + body);
                }
                return JsonParser.parseString(body).getAsJsonObject();
            });
        }
    }
}
