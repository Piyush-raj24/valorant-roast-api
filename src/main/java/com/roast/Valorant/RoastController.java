package com.roast.Valorant;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

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

    @GetMapping("/roast/{region}/{name}/{tag}")
    public Map<String, String> getRoast(@PathVariable String region, @PathVariable String name, @PathVariable String tag) {
        Map<String, String> result = new HashMap<>();

        try {
            Map response = valorantService.fetchPlayerStats(region, name, tag);

            List matches = (List) response.get("data");
            Map mostRecentMatch = (Map) matches.get(0);
            List allPlayers = (List) mostRecentMatch.get("players");

            int kills = 0, deaths = 0;
            int headshots = 0, bodyshots = 0, legshots = 0;
            String agentName = "Unknown";

            for (Object p : allPlayers) {
                Map playerNode = (Map) p;
                if (name.equalsIgnoreCase((String) playerNode.get("name"))) {

                    Map stats = (Map) playerNode.get("stats");
                    kills = (int) stats.get("kills");
                    deaths = (int) stats.get("deaths");

                    if (stats.get("headshots") != null) {
                        headshots = (int) stats.get("headshots");
                        bodyshots = (int) stats.get("bodyshots");
                        legshots = (int) stats.get("legshots");
                    }

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

            // Calculate KD and determine Tier
            double kdRatio = (deaths == 0) ? kills : (double) kills / deaths;
            String tier = "Unrated";
            if (kdRatio >= 2.0) tier = "Certified Smurf";
            else if (kdRatio >= 1.2) tier = "Sweaty Tryhard";
            else if (kdRatio >= 0.8) tier = "Average Andy";
            else tier = "Plastic 1";

            // Package the data into a JSON structure
            result.put("roast", roastEngine.generateRoast(kills, deaths, headshots, bodyshots, legshots, agentName));
            result.put("kd", String.format("%.2f", kdRatio));
            result.put("agent", agentName);
            result.put("tier", tier);

            return result;

        } catch (Exception e) {
            result.put("error", "Failed to fetch stats. Error: " + e.getMessage());
            return result;
        }
    }
}