package com.farm.management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherData getWeather(String city) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("q", city + ",TZ")
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .queryParam("lang", "en")
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            WeatherData data = new WeatherData();
            data.setCity(city);
            data.setTemperature(root.path("main").path("temp").asDouble());
            data.setFeelsLike(root.path("main").path("feels_like").asDouble());
            data.setHumidity(root.path("main").path("humidity").asInt());
            data.setDescription(root.path("weather").get(0)
                    .path("description").asText());
            data.setIcon(root.path("weather").get(0)
                    .path("icon").asText());
            data.setWindSpeed(root.path("wind").path("speed").asDouble());
            data.setCountry(root.path("sys").path("country").asText());
            return data;

        } catch (Exception e) {
            WeatherData fallback = new WeatherData();
            fallback.setCity(city);
            fallback.setTemperature(0);
            fallback.setDescription("Data unavailable");
            fallback.setIcon("01d");
            return fallback;
        }
    }

    // Inner class for weather data
    public static class WeatherData {
        private String city;
        private double temperature;
        private double feelsLike;
        private int humidity;
        private String description;
        private String icon;
        private double windSpeed;
        private String country;

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public double getFeelsLike() { return feelsLike; }
        public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }
        public int getHumidity() { return humidity; }
        public void setHumidity(int humidity) { this.humidity = humidity; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getIconUrl() {
            return "https://openweathermap.org/img/wn/" + icon + "@2x.png";
        }
    }
}