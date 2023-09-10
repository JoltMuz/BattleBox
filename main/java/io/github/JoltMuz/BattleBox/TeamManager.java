package io.github.JoltMuz.BattleBox;

import java.util.Map;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.entity.Player;

public class TeamManager 
{
	private Map<Player, Team> leaderTeams;
	private Map<Player, Team> playerTeams;
	private int maxSize;
	
	private static TeamManager instance;

    public TeamManager() {
    	leaderTeams = new HashMap<>();
    	playerTeams = new HashMap<>();
    	this.maxSize = 4;
    }
    
    public static TeamManager getInstance() {
		if (instance == null) {
			instance = new TeamManager();
		}
		return instance;
	}

    public void createTeam(Player leader) 
    {
        Team team = new Team(leader);
        leaderTeams.put(leader, team);
        playerTeams.put(leader, team);
    }
    public void addToTeam(Player member, Team team)
    {
    	if (team.getSize() < maxSize)
    	{
    		team.addMember(member);
        	playerTeams.put(member, team);
    	}
    	else
    	{
    		throw new IllegalArgumentException("Cannot add to team, exceeding maximum size.");
    	}
    	
    }
    public void removeFromTeam(Player member, Team team)
    {
    	playerTeams.remove(member);
    	team.removeMember(member);
    }
    public void promoteToLeader(Player player, Player promoted, Team team)
    {
    	team.setLeader(player, promoted);
    	leaderTeams.remove(player);
    	leaderTeams.put(promoted, team);
    }
    public void removeTeam(Player leader) 
    {
       if (leaderTeams.containsKey(leader))
       {
    	   for (Player p: leaderTeams.get(leader).getMembers())
    	   {
    		   playerTeams.remove(p);
    	   }
    	   leaderTeams.remove(leader);
    	   playerTeams.remove(leader);
       }
       else
       {
    	   throw new IllegalArgumentException("Tried to remove team, but it doesn't exist.");
       }
    }

    public Team getTeamByLeader(Player player) 
    {
        return leaderTeams.get(player);
    }
    public Team getTeamByName(String name) 
    {
    	Team team = null;
        for (Team t: leaderTeams.values())
        {
        	if (t.getName().equalsIgnoreCase(name))
        	{
        		team = t;
        		break;
        	}
        }
        return team;
    }
    public Team getTeamByPlayer(Player player) 
    {
        return playerTeams.get(player);
    }
    public Collection<Team> getAllTeams() {
        return leaderTeams.values();
    }
    public void setMaxSize(int maxSize) 
    {
        this.maxSize = maxSize;
    }
    public int getMaxSize() 
    {
        return this.maxSize;
    }

}
