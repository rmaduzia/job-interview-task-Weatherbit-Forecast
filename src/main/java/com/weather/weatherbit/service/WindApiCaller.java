package com.weather.weatherbit.service;

import com.weather.weatherbit.model.WeatherApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class WindApiCaller {

    @Value("${weatherbit.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public Mono<WeatherApiResponse> getWeatherApiResponse(String location) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .queryParam("key", apiKey)
                .queryParam("city", location)
                .build())
            .retrieve()
            .bodyToMono(WeatherApiResponse.class)
            .onErrorMap(e -> {
                if (e instanceof WebClientResponseException ex) {
                    if (ex.getStatusCode().is4xxClientError() || ex.getStatusCode().is5xxServerError()) {
                        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error response from server for location: " + location);
                    }
                }
                return e;
            });
    }
}