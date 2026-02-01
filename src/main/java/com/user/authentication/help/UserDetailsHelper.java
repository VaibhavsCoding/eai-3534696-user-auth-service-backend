package com.user.authentication.help;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 🧭 UserDetailsHelper — Extracts client IP, device info, and geo data.
 * Handles proxies, identifies OS/browser, and resolves geo-location (even for localhost).
 */
@Component
public class UserDetailsHelper {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsHelper.class);
    private final RestTemplate restTemplate = new RestTemplate();

    // -------------------------------------------------------------------------
    // 🧩 Extract Real Client IP (Handles Proxies & IPv6)
    // -------------------------------------------------------------------------
    public String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // If multiple proxies, use the first IP (real client)
            ip = ip.split(",")[0].trim();
        } else {
            // Try other proxy headers
            String[] headers = {
                    "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                    "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
            };
            for (String header : headers) {
                ip = request.getHeader(header);
                if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                    break;
                }
            }
        }

        // Fallback
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Normalize IPv6 localhost
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }

        logger.debug("✅ Extracted client IP: {}", ip);
        return ip;
    }

    // -------------------------------------------------------------------------
    // 🌍 Fetch Geo Data from IP
    // -------------------------------------------------------------------------
    @Cacheable("geoData")
    public Map<String, String> fetchGeoData(String ip) {
        Map<String, String> geo = new HashMap<>();
        geo.put("city", "Unknown");
        geo.put("country", "Unknown");

        try {
            // 🏠 Handle localhost or internal IPs — fetch actual public IP
            if (ip == null || ip.isBlank() || ip.equals("127.0.0.1") || ip.startsWith("192.") || ip.startsWith("10.")) {
                logger.debug("🔍 Local or private IP detected ({}), resolving public IP...", ip);
                String publicIp = restTemplate.getForObject("https://api.ipify.org", String.class);

                if (publicIp != null && !publicIp.isBlank()) {
                    logger.debug("🌐 Resolved public IP for localhost: {}", publicIp);
                    ip = publicIp.trim();
                } else {
                    logger.warn("⚠️ Could not resolve public IP for localhost. Using fallback.");
                }
            }

            // 🌍 Lookup geo-location using ipapi.co
            String url = "https://ipapi.co/" + ip + "/json/";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                geo.put("city", (String) body.getOrDefault("city", "Unknown"));
                geo.put("country", (String) body.getOrDefault("country_name", "Unknown"));
            } else {
                logger.warn("⚠️ Geo API returned non-200 for IP {}", ip);
            }

        } catch (Exception e) {
            logger.warn("🌐 Geo fetch failed for IP {}: {}", ip, e.getMessage());
        }

        logger.debug("📍 Geo data for {} => {}", ip, geo);
        return geo;
    }

    // -------------------------------------------------------------------------
    // 💻 Parse Device Type (OS + Browser)
    // -------------------------------------------------------------------------
    public String getDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown OS - Unknown Browser";
        }

        String ua = userAgent.toLowerCase();

        // Detect OS
        String os;
        if (ua.contains("windows")) {
            os = "Windows";
        } else if (ua.contains("mac")) {
            os = "macOS";
        } else if (ua.contains("android")) {
            os = "Android";
        } else if (ua.contains("iphone") || ua.contains("ipad")) {
            os = "iOS";
        } else if (ua.contains("linux")) {
            os = "Linux";
        } else {
            os = "Unknown OS";
        }

        // Detect Browser
        String browser;
        if (ua.contains("edg")) {
            browser = "Edge";
        } else if (ua.contains("chrome") && !ua.contains("edg")) {
            browser = "Chrome";
        } else if (ua.contains("firefox")) {
            browser = "Firefox";
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            browser = "Safari";
        } else if (ua.contains("opera") || ua.contains("opr")) {
            browser = "Opera";
        } else {
            browser = "Unknown Browser";
        }

        String deviceType = os + " - " + browser;
        logger.debug("🖥️ Parsed device type: {}", deviceType);
        return deviceType;
    }
}
