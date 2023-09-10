package io.github.JoltMuz.BattleBox;

import java.util.ArrayList;

import org.bukkit.entity.Player;

public class Team {
    private String name;
    private Player leader;
    private ArrayList<Player> members;
    private int score;

    public Team(Player leader) 
    {
        this.leader = leader;
        this.score = 0;
        this.members = new ArrayList<>();
    }

    public String getName() 
    {
    	if (this.name != null)
    	{
    		return name;
    	}
    	else
    	{
    		return leader.getName() + "'s_Team";
    	}
    }
    public void setName(String name)
    {
    	this.name = name;
    }

    public Player getLeader() 
    {
        return leader;
    }
    public void setLeader(Player player, Player promoted)
    {
    	this.addMember(this.leader);
    	this.removeMember(promoted);
    	this.leader = promoted;
    }

    public int getScore() 
    {
        return score;
    }

    public void setScore(int score) 
    {
        this.score = score;
    }
    public int getSize()
    {
    	return this.getMembers().size() + 1;
    }

    public ArrayList<Player> getMembers() 
    {
        return members;
    }
    public ArrayList<Player> getAllPlayers() 
    {
    	ArrayList<Player> allPlayers = new ArrayList<Player>();
    	allPlayers.addAll(members);
    	allPlayers.add(leader);
        return allPlayers;
    }

    public void addMember(Player player) 
    {
        members.add(player);
    }
    public void removeMember(Player player)
    {
    	if (this.members.contains(player))
    	{
    		this.members.remove(player);
    	}
    	else
    	{
    		throw new IllegalArgumentException("The member you tried to remove from team is not in a team");
    	}
    }
    public boolean contains(Player player)
    {
    	if (this.leader.equals(player) || this.getMembers().contains(player))
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
}

