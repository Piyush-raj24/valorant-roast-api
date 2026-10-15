package com.roast.Valorant;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class RoastEngine {

    private final Random random = new Random();

    public String generateRoast(int kills, int deaths, int headshots, int bodyshots, int legshots, String agent) {
        double kdRatio = (deaths == 0) ? kills : (double) kills / deaths;
        int totalShots = headshots + bodyshots + legshots;
        double hsPercentage = (totalShots == 0) ? 0 : ((double) headshots / totalShots) * 100;

        String formattedKD = String.format("%.2f", kdRatio);
        String formattedHS = String.format("%.1f%%", hsPercentage);
        String agentUpper = (agent != null && !agent.isBlank()) ? agent : "Unknown Agent";

        // 1. Extreme Smurf / Carry (KD >= 2.2)
        if (kdRatio >= 2.2) {
            String[] roasts = {
                String.format("A %s K/D and %s HS on %s? Okay, you carried the lobby. But your room smells like warm radiator water and you desperately need to touch grass.", formattedKD, formattedHS, agentUpper),
                String.format("%s K/D on %s? Bro is smurfing in a lobby of toddlers to feel something. Go play in your own rank.", formattedKD, agentUpper),
                String.format("Carrying your 4 deadweight teammates with a %s K/D on %s. Your chiropractor bill is going to be astronomical.", formattedKD, agentUpper)
            };
            return roasts[random.nextInt(roasts.length)];
        }

        // 2. Horrendous Performance (KD < 0.4)
        if (kdRatio < 0.4) {
            String[] roasts = {
                String.format("A %s K/D? Are you playing %s with a Samsung Smart Fridge touch screen? The practice range bots have better game sense.", formattedKD, agentUpper),
                String.format("%s K/D on %s. You're not even a spectator at this point, you're free ultimate points for the enemy team.", formattedKD, agentUpper),
                String.format("Dropping a %s K/D on %s is technically a war crime. Your teammates are filing a police report right now.", formattedKD, agentUpper)
            };
            return roasts[random.nextInt(roasts.length)];
        }

        // 3. Agent-Specific Roasts
        String lowerAgent = agentUpper.toLowerCase();

        if ((lowerAgent.contains("reyna") || lowerAgent.contains("jett")) && kdRatio < 1.0) {
            return String.format("Instalocking %s in 0.2 seconds just to finish with a %s K/D and %s HS? You dismissed away your dignity and gave the enemy team free elo.", agentUpper, formattedKD, formattedHS);
        }

        if (lowerAgent.contains("sage") && kdRatio < 0.8) {
            return String.format("A %s K/D on Sage. Self-healing yourself with 0 revives while your duelists die. Your heal orb has more combat value than your Vandal.", formattedKD);
        }

        if (lowerAgent.contains("sova") && kdRatio < 0.9) {
            return String.format("Lineup Larry spent 40 minutes on YouTube learning %s shock darts just to bounce them off your own face and drop a %s K/D.", agentUpper, formattedKD);
        }

        if (lowerAgent.contains("cypher") && kdRatio < 0.85) {
            return String.format("Sitting on your %s camera watching all 4 teammates get slaughtered like a Netflix documentary, only to lose the 1v1 with a %s K/D.", agentUpper, formattedKD);
        }

        if (lowerAgent.contains("omen") && kdRatio < 0.9) {
            return String.format("Teleported straight into enemy crosshairs with %s and died before the animation ended. Paranoia hit your own squad.", agentUpper);
        }

        if (lowerAgent.contains("raze") && kdRatio < 1.0) {
            return String.format("Double-satcheled into site at Mach 3 on %s just to whiff an entire Phantom mag and fall over. Spectacular suicide.", agentUpper);
        }

        if (lowerAgent.contains("phoenix") && kdRatio < 1.0) {
            return String.format("Flashed your own team 4 times, threw a molly at your own feet, and ulted right into a sniper crosshair on %s.", agentUpper);
        }

        if (lowerAgent.contains("clove") && kdRatio < 1.0) {
            return String.format("Dying twice in the same round with your %s ult just to give the enemy team 200 bonus combat score. Impressive efficiency.", agentUpper);
        }

        if (lowerAgent.contains("killjoy") && kdRatio < 0.9) {
            return String.format("Your %s turret got more kills than your actual gun. Disconnect and let the automated gadget carry.", agentUpper);
        }

        if (lowerAgent.contains("iso") && kdRatio < 1.0) {
            return String.format("Popped your double-tap shield on %s and immediately lost a 1v1 duel to a Classic right-click. Tragic.", agentUpper);
        }

        // 4. Aim & Hit Location Roasts
        if (legshots > headshots && totalShots > 10) {
            return String.format("You hit %d legshots and only %d headshots on %s. Stop aiming at their shoelaces; you're playing Valorant, not soccer.", legshots, headshots, agentUpper);
        }

        if (hsPercentage < 12.0 && totalShots > 15) {
            return String.format("A %s headshot percentage? Your crosshair is magnetically glued to the floor. Lift your mouse or buy a bigger mousepad.", formattedHS);
        }

        if (hsPercentage >= 40.0 && kdRatio < 1.0) {
            return String.format("A %s headshot rate with a %s K/D on %s? All aim, absolutely zero brain. You're hitting gorgeous one-taps straight into a 1v5 loss.", formattedHS, formattedKD, agentUpper);
        }

        // 5. Solid / Sweaty Player (1.2 <= KD < 2.2)
        if (kdRatio >= 1.2) {
            return String.format("A %s K/D and %s HS on %s. Sweaty enough to brag on Discord, but not good enough to get out of your hardstuck rank.", formattedKD, formattedHS, agentUpper);
        }

        // 6. Painfully Average (0.8 <= KD < 1.2)
        return String.format("A %s K/D and %s HS on %s. You are the human equivalent of unflavored oatmeal. Completely invisible in the lobby.", formattedKD, formattedHS, agentUpper);
    }

    public String calculateTier(double kdRatio) {
        if (kdRatio >= 2.2) return "Radiant Menace";
        if (kdRatio >= 1.6) return "Certified Smurf";
        if (kdRatio >= 1.15) return "Sweaty Tryhard";
        if (kdRatio >= 0.85) return "Average Andy";
        if (kdRatio >= 0.6) return "Iron Imposter";
        if (kdRatio >= 0.4) return "Cardboard 2";
        return "Plastic 1";
    }
}