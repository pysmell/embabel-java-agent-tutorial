/*
 * Copyright 2024-2025 Embabel Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.common.ai.model.ModelSelectionCriteria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Agent(description = "Generate a weather info on user input")
public class WeatherAgent {

    record City(String name) {
    }

    record WeatherData(
            String cityName,
            String country,
            double temperature,
            double feelsLike,
            String description,
            String icon,
            int humidity,
            double windSpeed,
            String sunrise,
            String sunset
    ) {
    }



    @Value("${openweather.api.key}")
    private String openWeatherApiKey;

    private final RestTemplate restTemplate;

    public WeatherAgent(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Action
    public City extractCity(UserInput userInput, OperationContext operationContext) {
        return operationContext.ai().withLlm(LlmOptions.fromCriteria(ModelSelectionCriteria.getAuto())).createObjectIfPossible(
                """
                        Extract the city name from this user input.
                        - city name: the name of the city

                        User input: %s""".formatted(userInput.getContent()),
                City.class
        );
    }

    @Action
    public WeatherData retrieveWeather(City city) {
        if (city == null || city.name() == null || city.name().isEmpty()) {
            System.err.println("WeatherAgent: City is null or empty");
            return null;
        }

        if (openWeatherApiKey == null || openWeatherApiKey.isEmpty()) {
            System.err.println("WeatherAgent: OpenWeather API Key is not configured");
            return null;
        }

        String geoQuery = city.name();
        String geoApiUrl = String.format("https://api.openweathermap.org/geo/1.0/direct?q=%s&limit=1&appid=%s",
                geoQuery,
                openWeatherApiKey);

        System.out.println("WeatherAgent: Calling geo API: " + geoApiUrl);

        GeoResponse[] geoResponses = restTemplate.getForObject(geoApiUrl, GeoResponse[].class);

        if (geoResponses == null || geoResponses.length == 0) {
            System.err.println("WeatherAgent: Geo API returned empty or null response");
            return null;
        }

        GeoResponse geoResponse = geoResponses[0];
        if (geoResponse.lat == null || geoResponse.lon == null) {
            return null;
        }

        String weatherApiUrl = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric",
                geoResponse.lat,
                geoResponse.lon,
                openWeatherApiKey);

        OpenWeatherResponse response = restTemplate.getForObject(weatherApiUrl, OpenWeatherResponse.class);

        if (response != null &&
            response.main != null &&
            response.sys != null &&
            response.weather != null &&
            response.weather.length > 0 &&
            response.wind != null) {
            return new WeatherData(
                    response.name,
                    response.sys.country,
                    response.main.temp,
                    response.main.feels_like,
                    response.weather[0].description,
                    response.weather[0].icon,
                    response.main.humidity,
                    response.wind.speed,
                    formatTimestamp(response.sys.sunrise),
                    formatTimestamp(response.sys.sunset)
            );
        }

        return null;
    }

    private String formatTimestamp(long timestamp) {
        return DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochSecond(timestamp));
    }

    @AchievesGoal(description = "generate weather related response to user, based on user's input and weather data")
    @Action
    public String reply(UserInput userInput, WeatherData weatherData, Ai ai) {
        var reply = ai
                .withAutoLlm()
                .generateText(String.format("""
                                Generate a friendly weather response for the user based on the following data:

                                # Weather Data
                                City: %s
                                Country: %s
                                Temperature: %.1f°C
                                Feels like: %.1f°C
                                Condition: %s
                                Humidity: %d%%
                                Wind Speed: %.1f m/s
                                Sunrise: %s
                                Sunset: %s

                                # User input
                                %s
                                """,
                        weatherData.cityName(),
                        weatherData.country(),
                        weatherData.temperature(),
                        weatherData.feelsLike(),
                        weatherData.description(),
                        weatherData.humidity(),
                        weatherData.windSpeed(),
                        weatherData.sunrise(),
                        weatherData.sunset(),
                        userInput.getContent()
                ).trim());

        return reply;
    }

    public static class GeoResponse {
        public String name;
        public Double lat;
        public Double lon;
        public String country;
        public String zip;

        public GeoResponse() {}

        public GeoResponse(String name, Double lat, Double lon, String country) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
            this.country = country;
        }
    }

    public static class OpenWeatherResponse {
        public String name;
        public Main main;
        public Weather[] weather;
        public Wind wind;
        public Sys sys;

        public static class Main {
            public double temp;
            public double feels_like;
            public int humidity;
        }

        public static class Weather {
            public String description;
            public String icon;
        }

        public static class Wind {
            public double speed;
        }

        public static class Sys {
            public String country;
            public long sunrise;
            public long sunset;
        }
    }
}
