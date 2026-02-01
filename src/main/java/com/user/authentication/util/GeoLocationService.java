package com.user.authentication.util;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class GeoLocationService {

    private static final String API_URL = "https://ipapi.co/%s/json/";

    public Map<String, String> getGeoLocation(String ip) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map response = restTemplate.getForObject(String.format(API_URL, ip), Map.class);

            Map<String, String> geo = new HashMap<>();
            geo.put("city", (String) response.getOrDefault("city", "Unknown"));
            geo.put("country", (String) response.getOrDefault("country_name", "Unknown"));
            return geo;
        } catch (Exception e) {
            return null;
        }
    }
}
