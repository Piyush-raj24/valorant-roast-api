package com.roast.Valorant;

import org.springframework.stereotype.Component;

@Component
public class RoastEngine {

    public String generateRoast(int kills, int deaths, int headshots, int bodyshots, int legshots, String agent) {
        double kdRatio = (deaths == 0) ? kills : (double) kills / deaths;
        int totalShots = headshots + bodyshots + legshots;
        double hsPercentage = (totalShots == 0) ? 0 : ((double) headshots / totalShots) * 100;

        String formattedKD = String.format("%.2f", kdRatio);
        String formattedHS = String.format("%.1f%%", hsPercentage);

        // Dynamic Roast Logic
        if (kdRatio > 2.0) {
            return String.format("A %s K/D and %s HS on %s? Okay, you carried. But you desperately need to touch grass and take a shower.", formattedKD, formattedHS, agent);
        }

        if ((agent.equalsIgnoreCase("Reyna") || agent.equalsIgnoreCase("Jett")) && kdRatio < 1.0) {
            return String.format("Instalocking %s just to drop a %s K/D is a federal crime. You are a walking ultimate orb for the enemy team.", agent, formattedKD);
        }

        if (hsPercentage < 15.0 && totalShots > 0) {
            return String.format("A %s headshot rate on %s? Your crosshair is glued to the floor. Stop aiming at their toes.", formattedHS, agent);
        }

        if (kdRatio < 0.5) {
            return String.format("A %s K/D? Are you playing %s with a steering wheel? The bots in the practice range have better game sense.", formattedKD, agent);
        }

        return String.format("A %s K/D and %s HS on %s. You're painfully average, which is almost worse than being terrible.", formattedKD, formattedHS, agent);
    }
}