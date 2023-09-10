package io.github.JoltMuz.BattleBox;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NameTagColorManager {
    private static Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    private static Team redTeam = scoreboard.getTeam("red");
    private static Team blueTeam = scoreboard.getTeam("blue");

    public static void addColor(Player player, String color) {
    	if (color.equalsIgnoreCase("red"))
    	{
    		if (redTeam == null) {
                redTeam = scoreboard.registerNewTeam("red");
                redTeam.setPrefix(ChatColor.RED.toString());
            }

            redTeam.addEntry(player.getName());
    	}
    	else if (color.equalsIgnoreCase("blue"))
    	{
    		if (blueTeam == null) {
                blueTeam = scoreboard.registerNewTeam("blue");
                blueTeam.setPrefix(ChatColor.BLUE.toString());
            }

            blueTeam.addEntry(player.getName());
    	}
        
    }

    public static void removeColor(Player player) {
        if (redTeam != null) {
            redTeam.removeEntry(player.getName());
        }

        if (blueTeam != null) {
            blueTeam.removeEntry(player.getName());
        }
    }
}
