package io.github.JoltMuz.BattleBox;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

public class MatchManager {
    private static MatchManager instance;
    private Map<Arena, Match> activeMatches;
    private Map<Integer, Match> matches;

    private MatchManager() {
        activeMatches = new ConcurrentHashMap<>();
        matches = new HashMap<>();
    }

    public static synchronized MatchManager getInstance() {
        if (instance == null) {
            instance = new MatchManager();
        }
        return instance;
    }

    public synchronized void startMatch(Arena arena, Team team1, Team team2) {
    	if (team1 == null || team2 == null)
    	{
    		throw new IllegalArgumentException("Those teams do not exist.");
    	}
        if (!isArenaAvailable(arena)) {
            throw new IllegalArgumentException("That arena is not available currently.");
        }
        if (!isTeamAvailable(team1) || !isTeamAvailable(team2)) {
            throw new IllegalArgumentException("One or both teams are not available currently.");
        }
        Match match = new Match(team1, team2, arena);
        matches.put(match.getID(), match);
        activeMatches.put(arena, match);
        match.start();
    }

    public synchronized void endMatch(Arena arena, Team winningTeam) {
        Match match = activeMatches.get(arena);
        if (match != null) {
            match.end(winningTeam);
            activeMatches.remove(arena);
        }
        if (Tournament.getCurrentTournament() != null && activeMatches.isEmpty()) {
            Tournament.getCurrentTournament().startNextRound();
        }
    }

    public synchronized boolean isArenaAvailable(Arena arena) {
        return !activeMatches.containsKey(arena);
    }
    public synchronized boolean isTeamAvailable(Team team) {
        for (Match match : activeMatches.values()) {
            if (match.getTeam1() == team || match.getTeam2() == team) {
                return false;
            }
        }
        return true;
    }
    
    public synchronized Map<Integer, Match> getMatches()
    {
    	return matches;
    }
    public synchronized Map<Arena, Match> getActiveMatches()
    {
    	return activeMatches;
    }
    public synchronized Match getOngoingMatchByPlayer(Player player) {
        for (Match match : activeMatches.values()) {
            if (match.getTeam1().contains(player) || match.getTeam2().contains(player)) {
                return match;
            }
        }
        return null;
    }
    public synchronized Match getMatchAtArena(Arena arena)
    {
    	if (!activeMatches.containsKey(arena))
    	{
    		throw new IllegalArgumentException("Invalid arena given");
    	}
    	return activeMatches.get(arena);
    }
    public synchronized boolean isInProgress(Match match)
    {
    	return match.isInProgress();
    }
    public synchronized void clearHistory()
    {
    	matches.clear();
    }

}
