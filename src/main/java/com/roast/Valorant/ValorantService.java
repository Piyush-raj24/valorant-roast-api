package com.roast.Valorant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import java.time.Duration;
import java.util.Map;

@Service
public class ValorantService {
    private final WebClient webClient;

    public ValorantService(
            @Value("${valorant.api.base-url:https://api.henrikdev.xyz/valorant}") String baseUrl,
            @Value("${valorant.api.key:HDEV-838aea69-6af3-4a78-9357-5b35eaf95652}") String apiKey) {

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(strategies)
                .defaultHeader("Authorization", apiKey)
                .build();
    }

    /**
     * Fetches recent match history from HenrikDev v4 matches endpoint.
     * Uses size=1 to fetch only the most recent match for ultra-fast response.
     */
    public Map fetchPlayerStats(String region, String name, String tag) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v4/matches/{region}/pc/{name}/{tag}")
                        .queryParam("size", "1")
                        .build(region, name, tag))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(12))
                .block();
    }
}