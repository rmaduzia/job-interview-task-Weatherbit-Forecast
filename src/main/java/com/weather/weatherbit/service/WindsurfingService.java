package com.weather.weatherbit.service;

import com.weather.weatherbit.config.Locations;
import com.weather.weatherbit.model.WeatherApiResponse;
import com.weather.weatherbit.model.WeatherData;
import com.weather.weatherbit.model.WindsurfingConditions;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
public class WindsurfingService {

    private final WindApiCaller windApiCaller;
    private final Locations locations;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public WindsurfingConditions getBestWindsurfingLocation(String lookingDate) {

        LocalDate date;

        try {
            date = LocalDate.parse(lookingDate, FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is not correct");
        }

        return Flux.fromIterable(locations.getLocationsString())
            .flatMap(windApiCaller::getWeatherApiResponse)
            .map(weatherApiResponse -> filterWeatherData(weatherApiResponse, date))
            .filter(weatherApiResponse -> !weatherApiResponse.getData().isEmpty())
            .collectList()
            .filter(list -> !list.isEmpty())
            .map(allResponses -> findBestLocation(allResponses, lookingDate))
            .block();
    }

    private WeatherApiResponse filterWeatherData(WeatherApiResponse weatherApiResponse, LocalDate date) {
        List<WeatherData> filteredData = weatherApiResponse.getData().stream()
            .filter(weatherData -> LocalDate.parse(weatherData.getDatetime()).isEqual(date))
            .collect(Collectors.toList());

        weatherApiResponse.setData(filteredData);
        return weatherApiResponse;
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