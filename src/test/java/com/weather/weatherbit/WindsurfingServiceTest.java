package com.weather.weatherbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.weatherbit.config.Locations;
import com.weather.weatherbit.model.WeatherApiResponse;
import com.weather.weatherbit.model.WeatherData;
import com.weather.weatherbit.model.WindsurfingConditions;
import com.weather.weatherbit.service.WindsurfingService;
import java.util.ArrayList;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.util.Collections;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WindsurfingServiceTest {

    private MockWebServer mockWebServer;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private WindsurfingService windsurfingService;
    private Locations locations;


    @BeforeEach
    void setUp() {

        List<String> cities = new ArrayList<>();
        cities.add("Jastarnia,PL");
        cities.add("Bridgetown,BB");
        cities.add("Fortaleza,BR");

        locations = new Locations(cities);

        mockWebServer = new MockWebServer();
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());

        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
        windsurfingService = new WindsurfingService(webClient, locations);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGetBestWindsurfingLocation_Success() throws Exception {

        prepareCorrectDataToResponse();

        WindsurfingConditions result = windsurfingService.getBestWindsurfingLocation("2024-03-10");

        assertNotNull(result, "Result should not be null");
        assertEquals("Bridgetown", result.getLocationName(), "Location name should match");
        assertEquals(33, result.getAverageTemperature(), "Average temperature should match");
        assertEquals(17, result.getWindSpeed(), "Wind speed should match");
    }

    @Test
    void testGetBestWindsurfingLocation_NoSuitableConditions() throws IOException {
        WeatherApiResponse weatherApiResponse = new WeatherApiResponse();
        weatherApiResponse.setData(Collections.singletonList(new WeatherData(3, 10, "2024-03-10")));
        weatherApiResponse.setCity_name("NoWindsurfCity");

        locations.getLocationsString().clear();
        locations = new Locations(List.of("Bridgetown,BB"));

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(new ObjectMapper().writeValueAsString(weatherApiResponse))
            .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE));

        WindsurfingConditions result = windsurfingService.getBestWindsurfingLocation("2024-03-10");

        assertNull(result);
    }

    @Test
    void testGetBestWindsurfingLocation_WhenServiceDoesNotReturnAnyData() {

        locations.getLocationsString().clear();
        locations = new Locations(List.of("Bridgetown,BB"));

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE));

        WindsurfingConditions result = windsurfingService.getBestWindsurfingLocation("2024-03-10");

        assertNull(result);
    }

    private void prepareCorrectDataToResponse() throws JsonProcessingException {

        WeatherApiResponse jastarniaWeatherApiResponse = new WeatherApiResponse();

        WeatherApiResponse bridgetownWeatherApiResponse = new WeatherApiResponse();
        WeatherApiResponse fortalezanWeatherApiResponse = new WeatherApiResponse();

        WeatherData jastarniaFirstWeatherData = new WeatherData(15, 25, "2024-03-10");
        WeatherData jastarniaSecondWeatherData = new WeatherData(17, 30, "2024-03-10");

        WeatherData bridgetownFirstWeatherData = new WeatherData(17, 32, "2024-03-10");
        WeatherData bridgetownSecondWeatherData = new WeatherData(17, 33, "2024-03-10");

        WeatherData fortalezaFirstWeatherData = new WeatherData(16, 27, "2024-03-10");
        WeatherData fortalezaSecondWeatherData = new WeatherData(17, 31, "2024-03-10");

        jastarniaWeatherApiResponse.setData(List.of(jastarniaFirstWeatherData, jastarniaSecondWeatherData));
        jastarniaWeatherApiResponse.setCity_name("Jastarnia");

        bridgetownWeatherApiResponse.setData(List.of(bridgetownFirstWeatherData, bridgetownSecondWeatherData));
        bridgetownWeatherApiResponse.setCity_name("Bridgetown");

        fortalezanWeatherApiResponse.setData(List.of(fortalezaFirstWeatherData, fortalezaSecondWeatherData));
        fortalezanWeatherApiResponse.setCity_name("Bridgetown");

        MockResponse response1 = new MockResponse()
            .setResponseCode(200)
            .setBody(new ObjectMapper().writeValueAsString(jastarniaWeatherApiResponse))
            .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        mockWebServer.enqueue(response1);

        MockResponse response2 = new MockResponse()
            .setResponseCode(200)
            .setBody(new ObjectMapper().writeValueAsString(bridgetownWeatherApiResponse))
            .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        mockWebServer.enqueue(response2);

        MockResponse response3 = new MockResponse()
            .setResponseCode(200)
            .setBody(new ObjectMapper().writeValueAsString(fortalezanWeatherApiResponse))
            .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        mockWebServer.enqueue(response3);
    }
}