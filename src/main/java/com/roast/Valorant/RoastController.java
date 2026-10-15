package com.roast.Valorant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RoastController {

    private final ValorantService valorantService;
    private final RoastEngine roastEngine;

    public RoastController(ValorantService valorantService, RoastEngine roastEngine) {
        this.valorantService = valorantService;
        this.roastEngine = roastEngine;
    }

    /**
     * Health check endpoint for uptime monitors (Render, Railway, UptimeRobot).
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "valorant-roast-api");
        health.put("version", "1.1.0");
        health.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(health);
    }

    /**
     * Main roast endpoint: /api/roast/{region}/{name}/{tag}
     */
    @GetMapping("/roast/{region}/{name}/{tag}")
    public ResponseEntity<Map<String, Object>> getRoast(
            @PathVariable String region,
            @PathVariable String name,
            @PathVariable String tag,
            @RequestParam(required = false, defaultValue = "false") boolean demo) {

        Map<String, Object> result = new HashMap<>();

        // 1. Check for Demo Profiles / Simulated Mode
        if (demo || "TenZ".equalsIgnoreCase(name) || "Whiff".equalsIgnoreCase(name) || "Bot".equalsIgnoreCase(name)) {
            return ResponseEntity.ok(generateDemoResponse(name, tag, region));
        }

        try {
            Map response = valorantService.fetchPlayerStats(region.toLowerCase().trim(), name.trim(), tag.trim());

            if (response == null || !response.containsKey("data")) {
                return ResponseEntity.status(404).body(createError("No data returned from Valorant match service. Check your Riot ID and Region."));
            }

            Object dataObj = response.get("data");
            if (!(dataObj instanceof List) || ((List) dataObj).isEmpty()) {
                return ResponseEntity.status(404).body(createError("No recent matches found for " + name + "#" + tag + ". Ensure the profile has played recently and is not private."));
            }

            List matches = (List) dataObj;
            Map mostRecentMatch = (Map) matches.get(0);

            // Defensive parser for players: HenrikDev v4 returns a Map with "all_players", earlier versions returned a List
            List allPlayers = extractPlayersList(mostRecentMatch.get("players"));

            if (allPlayers == null || allPlayers.isEmpty()) {
                return ResponseEntity.status(404).body(createError("Could not parse players for the latest match."));
            }

            int kills = 0, deaths = 0, assists = 0;
            int headshots = 0, bodyshots = 0, legshots = 0;
            String agentName = "Unknown";
            boolean playerFound = false;

            for (Object p : allPlayers) {
                if (!(p instanceof Map)) continue;
                Map playerNode = (Map) p;

                String pName = (String) playerNode.get("name");
                String pTag = (String) playerNode.get("tag");

                if (name.equalsIgnoreCase(pName) || (pTag != null && tag.equalsIgnoreCase(pTag) && name.equalsIgnoreCase(pName))) {
                    playerFound = true;

                    // Extract stats node
                    if (playerNode.get("stats") instanceof Map) {
                        Map stats = (Map) playerNode.get("stats");
                        kills = getIntSafely(stats, "kills");
                        deaths = getIntSafely(stats, "deaths");
                        assists = getIntSafely(stats, "assists");
                        headshots = getIntSafely(stats, "headshots");
                        bodyshots = getIntSafely(stats, "bodyshots");
                        legshots = getIntSafely(stats, "legshots");
                    }

                    // Extract agent/character
                    Object characterObj = playerNode.get("character");
                    if (characterObj instanceof String) {
                        agentName = (String) characterObj;
                    } else if (characterObj instanceof Map) {
                        Map characterMap = (Map) characterObj;
                        if (characterMap.get("name") != null) {
                            agentName = (String) characterMap.get("name");
                        }
                    }
                    break;
                }
            }

            if (!playerFound) {
                return ResponseEntity.status(404).body(createError("Player " + name + "#" + tag + " was not found in the most recent match roster."));
            }

            // Calculations
            double kdRatio = (deaths == 0) ? kills : (double) kills / deaths;
            int totalShots = headshots + bodyshots + legshots;
            double hsPercent = (totalShots == 0) ? 0.0 : ((double) headshots / totalShots) * 100.0;
            String tier = roastEngine.calculateTier(kdRatio);
            String roastText = roastEngine.generateRoast(kills, deaths, headshots, bodyshots, legshots, agentName);

            // Extract Map Name if available
            String mapName = "Competitive";
            if (mostRecentMatch.get("metadata") instanceof Map) {
                Map metadata = (Map) mostRecentMatch.get("metadata");
                if (metadata.get("map") instanceof Map) {
                    Map mapObj = (Map) metadata.get("map");
                    if (mapObj.get("name") != null) mapName = (String) mapObj.get("name");
                } else if (metadata.get("map") instanceof String) {
                    mapName = (String) metadata.get("map");
                }
            }

            // Package rich response payload
            result.put("player", name + "#" + tag);
            result.put("region", region.toUpperCase());
            result.put("agent", agentName);
            result.put("kills", kills);
            result.put("deaths", deaths);
            result.put("assists", assists);
            result.put("kd", String.format(Locale.US, "%.2f", kdRatio));
            result.put("headshots", headshots);
            result.put("bodyshots", bodyshots);
            result.put("legshots", legshots);
            result.put("hsPercent", String.format(Locale.US, "%.1f%%", hsPercent));
            result.put("tier", tier);
            result.put("roast", roastText);
            result.put("map", mapName);

            return ResponseEntity.ok(result);

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 429) {
                return ResponseEntity.status(429).body(createError("HenrikDev API rate limit reached. Please retry in a few moments."));
            } else if (e.getStatusCode().value() == 404) {
                return ResponseEntity.status(404).body(createError("Player " + name + "#" + tag + " not found on region '" + region + "'. Check spelling and tag."));
            }
            return ResponseEntity.status(e.getStatusCode()).body(createError("Valorant upstream error: " + e.getStatusText()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createError("Failed to fetch stats: " + e.getMessage()));
        }
    }

    private List extractPlayersList(Object playersObj) {
        if (playersObj instanceof List) {
            return (List) playersObj;
        }
        if (playersObj instanceof Map) {
            Map map = (Map) playersObj;
            if (map.get("all_players") instanceof List) {
                return (List) map.get("all_players");
            }
        }
        return Collections.emptyList();
    }

    private int getIntSafely(Map map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return 0;
    }

    private Map<String, Object> createError(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    private Map<String, Object> generateDemoResponse(String name, String tag, String region) {
        Map<String, Object> demo = new HashMap<>();
        boolean isSmurf = "TenZ".equalsIgnoreCase(name);
        int kills = isSmurf ? 28 : 6;
        int deaths = isSmurf ? 11 : 17;
        int assists = isSmurf ? 7 : 2;
        int headshots = isSmurf ? 24 : 3;
        int bodyshots = isSmurf ? 16 : 28;
        int legshots = isSmurf ? 2 : 12;
        String agent = isSmurf ? "Jett" : "Reyna";

        double kd = (double) kills / deaths;
        double hs = ((double) headshots / (headshots + bodyshots + legshots)) * 100.0;
        String tier = roastEngine.calculateTier(kd);
        String roast = roastEngine.generateRoast(kills, deaths, headshots, bodyshots, legshots, agent);

        demo.put("player", name + "#" + tag);
        demo.put("region", region.toUpperCase());
        demo.put("agent", agent);
        demo.put("kills", kills);
        demo.put("deaths", deaths);
        demo.put("assists", assists);
        demo.put("kd", String.format(Locale.US, "%.2f", kd));
        demo.put("headshots", headshots);
        demo.put("bodyshots", bodyshots);
        demo.put("legshots", legshots);
        demo.put("hsPercent", String.format(Locale.US, "%.1f%%", hs));
        demo.put("tier", tier);
        demo.put("roast", roast);
        demo.put("map", "Ascent");
        demo.put("isDemo", true);
        return demo;
    }
}