package com.roast.Valorant;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import java.util.Map;

@Service
public class ValorantService {
    private final WebClient webClient;

    public ValorantService() {
        // 1. Increase the buffer size limit to 16MB
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        // 2. Apply the strategies to the WebClient
        this.webClient = WebClient.builder()
                .baseUrl("https://api.henrikdev.xyz/valorant")
                .exchangeStrategies(strategies)
                // MAKE SURE TO PASTE YOUR ACTUAL API KEY BACK IN HERE:
                .defaultHeader("Authorization", "HDEV-838aea69-6af3-4a78-9357-5b35eaf95652")
                .build();
    }

    public Map fetchPlayerStats(String region, String name, String tag) {
        return this.webClient.get()
                .uri("/v4/matches/{region}/pc/{name}/{tag}", region, name, tag)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}