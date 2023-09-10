package io.github.JoltMuz.BattleBox;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Arena 
{
	private String name;
	private Location team1SpawnPoint;
    private Location team2SpawnPoint;
    private List<Location> capturePoints;

    public Arena(String name, Location team1SpawnPoint, Location team2SpawnPoint, Location capturePointMid) {
        this.name = name;
        this.team1SpawnPoint = team1SpawnPoint.setDirection(team2SpawnPoint.toVector().subtract(team1SpawnPoint.toVector()).normalize());
        this.team2SpawnPoint = team2SpawnPoint.setDirection(team1SpawnPoint.toVector().subtract(team2SpawnPoint.toVector()).normalize());
        this.capturePoints = new ArrayList<>();

        Vector[] capturePointVectors = {
            new Vector(1, 0, 1),
            new Vector(1, 0, 0),
            new Vector(1, 0, -1),
            new Vector(0, 0, -1),
            new Vector(0, 0, 0),
            new Vector(0, 0, 1),
            new Vector(-1, 0, -1),
            new Vector(-1, 0, 0),
            new Vector(-1, 0, 1)
        };
        Location roundedMid = new Location(capturePointMid.getWorld(), Math.floor(capturePointMid.getX()),Math.floor(capturePointMid.getBlockY()),Math.floor(capturePointMid.getBlockZ()));
        for (Vector vector : capturePointVectors) {
            Location capturePoint = roundedMid.clone().add(vector);
            capturePoints.add(capturePoint);
        }
    }
    public String getName()
    {
    	return this.name;
    }
    public Location getTeam1SpawnPoint() {
        return team1SpawnPoint;
    }

    public Location getTeam2SpawnPoint() {
        return team2SpawnPoint;
    }
    
    public Location getCapturePointsMid() {
        return capturePoints.get(4);
    }
    public List<Location> getCapturePoints() {
        return capturePoints;
    }
}
