package com.weather.weatherbit.service;

import com.weather.weatherbit.config.Locations;
import com.weather.weatherbit.model.WeatherApiResponse;
import com.weather.weatherbit.model.WeatherData;
import com.weather.weatherbit.model.WindsurfingConditions;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class WindsurfingService {

    @Value("${weatherbit.api.key}")
    private String apiKey;


    private final WebClient webClient;

    public WebClient getWebClient() {
        return webClient;
    }

    private final Locations locations;

    public WindsurfingConditions getBestWindsurfingLocation(String lookingDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(lookingDate, formatter);

        List<WeatherApiResponse> allResponses = Flux.fromIterable(locations.getLocationsString())
            .flatMap(this::getWeatherApiResponse)
            .map(weatherApiResponse -> filterWeatherData(weatherApiResponse, date))
            .filter(weatherApiResponse -> !weatherApiResponse.getData().isEmpty())
            .collectList()
            .block();

        if (allResponses == null || allResponses.isEmpty()) {
            return null;
        }

        return findBestLocation(allResponses, lookingDate);
    }

    private WeatherApiResponse filterWeatherData(WeatherApiResponse weatherApiResponse, LocalDate date) {
        List<WeatherData> filteredData = weatherApiResponse.getData().stream()
            .filter(weatherData -> LocalDate.parse(weatherData.getDatetime()).isEqual(date))
            .collect(Collectors.toList());

        weatherApiResponse.setData(filteredData);
        return weatherApiResponse;
    }

    private Mono<WeatherApiResponse> getWeatherApiResponse(String location) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .queryParam("key", apiKey)
                .queryParam("city", location)
                .build())
            .retrieve()
            .bodyToMono(WeatherApiResponse.class);
    }

    private WindsurfingConditions findBestLocation(List<WeatherApiResponse> weatherApiResponses, String date) {
        WindsurfingConditions bestConditions = null;
        double bestScore = Double.MIN_VALUE;
        String location;

        for (WeatherApiResponse weatherApiResponse : weatherApiResponses)
        {
            location = weatherApiResponse.getCity_name();
            for (WeatherData weatherData : weatherApiResponse.getData()) {
                double windSpeed = weatherData.getWind_spd();
                double temperature = weatherData.getTemp();

                if (isSuitableForWindsurfing(windSpeed, temperature)) {
                    double score = windSpeed * 3 + temperature;

                    if (score > bestScore) {
                        bestScore = score;
                        bestConditions = new WindsurfingConditions(location, temperature,
                            windSpeed);
                    }
                }
            }
        }

        if (bestConditions == null) {
            throw new IllegalArgumentException("No suitable conditions found for windsurfing on the specified date: " + date);
        }

        return bestConditions;
    }

    private boolean isSuitableForWindsurfing(double windSpeed, double temperature) {
        return windSpeed >= 5 && windSpeed <= 18
            && temperature >= 5 && temperature <= 35;
    }
}