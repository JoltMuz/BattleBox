package io.github.JoltMuz.BattleBox;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

public class Tournament {
    private static Tournament currentTournament; // Hold the current tournament instance
    private List<Team> teams;
    private List<List<Match>> rounds;
    private ArenaManager arenaManager;
    private MatchManager matchManager;
    private int roundIndex;

    private Tournament(List<Team> teams, ArenaManager arenaManager, MatchManager matchManager) {
        this.teams = teams;
        this.rounds = new ArrayList<>();
        this.arenaManager = arenaManager;
        this.matchManager = matchManager;
        this.roundIndex = 0;
    }

    public static Tournament getCurrentTournament() {
        return currentTournament;
    }

    public static void createTournament(List<Team> teams, ArenaManager arenaManager, MatchManager matchManager) {
        currentTournament = new Tournament(teams, arenaManager, matchManager);
        
    }
    
    public void cancelTournament()
    {
    	currentTournament = null;
    }

    public void startTournament() {
        startNextRound();
    }

    public void generatePairings() {
        List<Team> teamsToPair = teams;
        int numTeams = teamsToPair.size();
        int numRounds = numTeams - 1;
        
        if (numTeams % 2 != 0) {
            throw new IllegalArgumentException("Odd number of teams");
        }
        
        int numArenas = arenaManager.getArenas().size();
        if (numArenas < numTeams/2) {
            throw new IllegalArgumentException("Number of Arenas is less than the number of matches per round.");
        }
        
        List<List<Match>> schedule = new ArrayList<>();
        
        
        for (int roundNum = 0; roundNum < numRounds; roundNum++) {
        	List<Arena> usedArenas = new ArrayList<>(); // Track used arenas
            List<Match> matches = new ArrayList<>();
            for (int i = 0; i < numTeams / 2; i++) {
                if (teamsToPair.get(i) != null && teamsToPair.get(numTeams - 1 - i) != null) {
                    Arena arena = arenaManager.getRandomAvailableArena(usedArenas); // Get a random available arena excluding the used ones
                    Match match = new Match(teamsToPair.get(numTeams - 1 - i), teamsToPair.get(i), arena);
                    matches.add(match);
                    usedArenas.add(arena); // Add the used arena to the list
                }
            }
            teamsToPair.add(1, teamsToPair.remove(numTeams - 1)); // Rotate the players except the first one
            schedule.add(matches);
        }

        rounds = schedule;
    }

    public void startNextRound() {
    	 if (roundIndex >= rounds.size() && matchManager.getActiveMatches().isEmpty()) {
    	        Bukkit.broadcastMessage(Commands.getPrefix() + "All matches of the tournament have been conducted.");
    	        return;
    	    }

    	    List<Match> currentRound = rounds.get(roundIndex);

    	    // Check if any matches from the current round are still ongoing
    	    for (Match match : currentRound) {
    	        if (matchManager.getActiveMatches().containsValue(match)) {
    	            System.out.println("A match is still ongoing, can't start the next round.");
    	            return;
    	        }
    	    }

    	    for (Match match : currentRound) {
    	    	try
    	    	{
    	    		matchManager.startMatch(match.getArena(), match.getTeam1(), match.getTeam2());
    	    	}
    	    	catch (IllegalArgumentException e)
    	    	{
    	    		Bukkit.broadcastMessage(Commands.getPrefix() + e.getMessage());
    	    	}
    	    	
    	        
    	    }
    	    roundIndex++;
    }

    public int getRoundIndex() {
        return roundIndex;
    }

    public List<Match> getCurrentMatches() {
        return rounds.get(roundIndex);
    }

    public List<List<Match>> getRounds() {
        return rounds;
    }
}
