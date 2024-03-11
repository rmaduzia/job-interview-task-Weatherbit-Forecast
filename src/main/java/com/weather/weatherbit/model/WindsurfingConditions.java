package com.weather.weatherbit.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WindsurfingConditions {
    private String locationName;
    private double averageTemperature;
    private double windSpeed;
}