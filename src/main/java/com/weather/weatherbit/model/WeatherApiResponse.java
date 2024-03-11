package com.weather.weatherbit.model;

import java.util.List;
import lombok.Data;

@Data
public class WeatherApiResponse {

    private List<WeatherData> data;
    private String city_name;
}