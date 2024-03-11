package com.weather.weatherbit.controller;

import com.weather.weatherbit.model.WindsurfingConditions;
import com.weather.weatherbit.service.WindsurfingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WeatherController {

    private final WindsurfingService windsurfingService;

    @GetMapping("/weather")
    public WindsurfingConditions getWeather(@RequestParam String date) {
        return windsurfingService.getBestWindsurfingLocation(date);
    }
}