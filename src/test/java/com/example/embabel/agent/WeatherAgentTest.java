package com.example.embabel.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class WeatherAgentTest {

    private WeatherAgent weatherAgent;

    @BeforeEach
    void setUp() {
        weatherAgent = new WeatherAgent(new RestTemplate());
    }

    @Test
    void testRetrieveWeather_NullCity() {
        var result = weatherAgent.retrieveWeather(null);
        assertNull(result);
    }

    @Test
    void testRetrieveWeather_EmptyCityName() {
        var city = new WeatherAgent.City("");
        var result = weatherAgent.retrieveWeather(city);
        assertNull(result);
    }

    @Test
    void testWeatherDataRecord() {
        var weatherData = new WeatherAgent.WeatherData(
                "London", "GB", 20.0, 18.0, "sunny", "01d", 60, 5.0, "06:00", "20:00"
        );

        assertNotNull(weatherData);
        assertEquals("London", weatherData.cityName());
        assertEquals("GB", weatherData.country());
        assertEquals(20.0, weatherData.temperature());
        assertEquals(18.0, weatherData.feelsLike());
        assertEquals("sunny", weatherData.description());
        assertEquals(60, weatherData.humidity());
    }

    @Test
    void testGeoResponseClass() {
        var geoResponse = new WeatherAgent.GeoResponse();
        geoResponse.name = "London";
        geoResponse.lat = 51.5074;
        geoResponse.lon = -0.1278;
        geoResponse.country = "GB";

        assertNotNull(geoResponse);
        assertEquals("London", geoResponse.name);
        assertEquals(51.5074, geoResponse.lat);
    }

    @Test
    void testCityRecord() {
        var city = new WeatherAgent.City("Paris");
        assertEquals("Paris", city.name());
    }
}
