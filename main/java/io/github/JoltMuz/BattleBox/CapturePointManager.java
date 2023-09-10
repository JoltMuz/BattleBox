package io.github.JoltMuz.BattleBox;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

public class CapturePointManager {
    private Map<Location, Team> capturePoints;

    public CapturePointManager() {
        this.capturePoints = new HashMap<>();
    }

    public void capturePoint(Location point, Team capturingTeam) {
        capturePoints.put(point, capturingTeam);
    }

    public void releasePoint(Location point) {
        capturePoints.remove(point);
    }

    public boolean isCaptured(Location point) {
        return capturePoints.containsKey(point);
    }
    public int getCapturedPointCount(Team team) {
    	return (int) capturePoints.values().stream()
                .filter(capturedTeam -> capturedTeam.equals(team))
                .count();
    }

    public Team getCapturingTeam(Location point) {
        return capturePoints.get(point);
    }

    public void resetCapturePoints() {
        capturePoints.clear();
    }

    public Map<Location, Team> getCapturedPoints() {
        return capturePoints;
    }
}