package com.weather.weatherbit.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Locations {

    @Value("#{'${locations}'.split(';')}")
    private List<String> locationsString;

    public List<String> getLocationsString() {
        return locationsString;
    }

    public Locations(@Value("#{'${locations}'.split(';')}") List<String> locationsString){
        this.locationsString = locationsString;
    }
}
