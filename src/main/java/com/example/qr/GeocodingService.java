package com.example.qr;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
public class GeocodingService {

    private final String apiKey = "e16daae92bf8433fb02ccf15a13944ad";

    public String getCoordinates(String address) throws Exception {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.opencagedata.com/geocode/v1/json";

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("q", address)
                    .queryParam("key", apiKey)
                    .queryParam("limit", 1);

            String response = restTemplate.getForObject(builder.toUriString(), String.class);
            return parseCoordinates(response);
        } catch (Exception e) {

            throw new Exception("Error geodaten: " + e.getMessage());
        }
    }

    private String parseCoordinates(String response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode results = rootNode.path("results");

        if (!results.isEmpty()) {
            JsonNode geometry = results.get(0).path("geometry");
            double lat = geometry.path("lat").asDouble();
            double lng = geometry.path("lng").asDouble();
            return lat + "," + lng;
        } else {
            // Handle the case where no results are found
            throw new IOException("Kein Geocoding Ergebnis gefunden");
        }
    }
}
