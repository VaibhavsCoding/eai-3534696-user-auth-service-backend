package com.user.authentication.util;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GeoLocationServiceTest {

    private final GeoLocationService geoLocationService = new GeoLocationService();

    @Test
    void getGeoLocation_shouldReturnCityAndCountry() {
        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("city", "Berlin");
        apiResponse.put("country_name", "Germany");

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(
                RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(Map.class)))
                        .thenReturn(apiResponse)
        )) {
            Map<String, String> result = geoLocationService.getGeoLocation("8.8.8.8");

            assertNotNull(result);
            assertEquals("Berlin", result.get("city"));
            assertEquals("Germany", result.get("country"));
        }
    }

    @Test
    void getGeoLocation_shouldReturnUnknownWhenFieldsMissing() {
        Map<String, Object> apiResponse = new HashMap<>();

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(
                RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(Map.class)))
                        .thenReturn(apiResponse)
        )) {
            Map<String, String> result = geoLocationService.getGeoLocation("1.1.1.1");

            assertNotNull(result);
            assertEquals("Unknown", result.get("city"));
            assertEquals("Unknown", result.get("country"));
        }
    }

    @Test
    void getGeoLocation_shouldReturnNullWhenRestTemplateThrowsException() {
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(
                RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(Map.class)))
                        .thenThrow(new RuntimeException("API error"))
        )) {
            Map<String, String> result = geoLocationService.getGeoLocation("127.0.0.1");
            assertNull(result);
        }
    }
}
