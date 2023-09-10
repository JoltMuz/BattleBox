package io.github.JoltMuz.BattleBox;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Commands implements CommandExecutor {
    private final ArenaManager arenaManager;
    private final TeamManager teamManager;
    private final MatchManager matchManager;
    
    private static String prefix = ChatColor.DARK_AQUA + "Battle"+ ChatColor.AQUA + "Box " + ChatColor.DARK_GRAY + "》 " + ChatColor.GRAY;
    private static int specHeight = 16;
    
    public static int getSpecHeight() {
		return specHeight;
	}

	public static void setSpecHeight(int specHeight) {
		Commands.specHeight = specHeight;
	}

    private static boolean slimeJump = true;
    
    public static boolean isSlimeJump() {
		return slimeJump;
	}

	public void setSlimeJump(boolean slimeJump) {
		Commands.slimeJump = slimeJump;
	}
	public static double getSlimeJumpValue() {
		return slimeJumpValue;
	}

	public void setSlimeJumpValue(double slimeJumpValue) {
		Commands.slimeJumpValue = slimeJumpValue;
	}
	private static double slimeJumpValue = 2;
    
	public Commands(ArenaManager arenaManager, TeamManager teamManager, MatchManager matchManager) {
        this.arenaManager = arenaManager;
        this.teamManager = teamManager;
        this.matchManager = matchManager;
    }
    
    public static String getPrefix()
    {
    	return prefix;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            handleHelpCommand(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("arenas")) 
        {
        	if (!(sender instanceof Player))
        	{
        		sender.sendMessage(prefix + "You must be a player to use this.");
        		return true;
        	}
        	Player player = (Player) sender;
            openArenasGUI(player);
        } 
        else if (args[0].equalsIgnoreCase("matches")) 
        {
        	if (!(sender instanceof Player))
        	{
        		sender.sendMessage(prefix + "You must be a player to use this.");
        		return true;
        	}
        	Player player = (Player) sender;
            if (args.length < 2) 
            {
            	int matchesPerPage = 45; // Maximum number of matches to display per page
                Map<Integer, Match> matches = matchManager.getMatches();
                if (matches.size() == 0)
                {
                	player.sendMessage(prefix + "No Matches yet.");
                	return true;
                }
                int numMatches = matches.size();
                int totalPages = (int) Math.ceil(numMatches / (double) matchesPerPage);
                    
            	openMatchesGUI(player, totalPages); // Show the last page by default
            } else 
            {
                try 
                {
                    int page = Integer.parseInt(args[1]);
                    openMatchesGUI(player, page);
                } catch (NumberFormatException e) {
                    player.sendMessage(prefix + ChatColor.RED + "Invalid page number. Try /bb matches");
                }
            }
        }
        else if (args[0].equalsIgnoreCase("tournament")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(prefix + "You must be a player to use this.");
                return true;
            }
            Player player = (Player) sender;
            
            if (args.length < 2) {
                Team team = teamManager.getTeamByPlayer(player);
                if (team == null) {
                    player.sendMessage(prefix + "You're not in any team.");
                    return true;
                }
                openTeamTournamentGUI(player, team);
            } else {
                Team team = teamManager.getTeamByName(args[1]);
                if (team == null) {
                    player.sendMessage(prefix + args[1] + " team not found.");
                    return true;
                }
                openTeamTournamentGUI(player, team);
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("tournamentall")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(prefix + "You must be a player to use this.");
                return true;
            }
            Player player = (Player) sender;
            int page = 1;
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(prefix + "Invalid page number.");
                    return true;
                }
            }
            openTournamentGUIPage(player, page);
            return true;
        }

        else if (args[0].equalsIgnoreCase("admin")) 
        {
        	if (!sender.isOp())
        	{
        		sender.sendMessage(prefix + "This command can only be used by operators");
        		return true;
        	}
            if (args.length < 2) {
                handleAdminHelpCommand(sender);
            } else if (args[1].equalsIgnoreCase("broadcast")) {
                handleBroadcastCommand(args);
            } else if (args[1].equalsIgnoreCase("arenas")) {
                handleAdminArenasCommand(sender, args);
            } else if (args[1].equalsIgnoreCase("matches")) {
                handleAdminMatchesCommand(sender, args);
            } else if (args[1].equalsIgnoreCase("teams")) {
                handleAdminTeamsCommand(sender, args);
            } else if (args[1].equalsIgnoreCase("tournament")) {
                handleAdminTournamentCommand(sender, args);
            } else {
                handleAdminHelpCommand(sender);
            }
        }
        else
        {
        	handleHelpCommand(sender);
        }

        return true;
    }
    private void handleHelpCommand(CommandSender sender)
    {
    	StringBuilder message = new StringBuilder();
    	message.append(ChatColor.DARK_AQUA + "   │  " + ChatColor.AQUA + "" + "BattleBox" + ChatColor.WHITE + " " + "Commands \n")
        	    .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb matches <page> ‣ " + ChatColor.WHITE + "Shows All Logged Matches\n")
        	    .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + ChatColor.ITALIC + "Note ‣ Shows most recent match if page not specified.\n")
        	    .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb arenas ‣ " + ChatColor.WHITE + "Shows All added Arenas.\n")
        	    .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb tournament ‣ " + ChatColor.WHITE + "Shows your next matches\n")
        	    .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb tournament <team> ‣ " + ChatColor.WHITE + "Shows next matches of <team>\n")
        	    .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb tournamentall ‣ " + ChatColor.WHITE + "Shows all matches of tournament.\n")
        	    .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin ‣ " + ChatColor.WHITE + "Admin Commands\n")
        	    .append(" ");
        	
        	sender.sendMessage(message.toString());
    	
    }
    private void handleAdminHelpCommand(CommandSender sender)
    {
    	StringBuilder message = new StringBuilder();
        message.append(ChatColor.DARK_AQUA + "   │  " + ChatColor.AQUA + "" + "BattleBox" + ChatColor.WHITE + " " + "Admin Commands:\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin arenas ‣ " + ChatColor.WHITE + "Add/Remove Arenas\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin matches ‣ " + ChatColor.WHITE + "Start/Stop Matches.\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin teams ‣ " + ChatColor.WHITE + "Manage Teams\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin tournament ‣ " + ChatColor.WHITE + "Create/Start Tournament.\n")
                .append(" ");


        sender.sendMessage(message.toString());
    	
    }
    private void handleArenasHelpCommand(CommandSender sender)
    {
    	StringBuilder message = new StringBuilder();
        message.append(ChatColor.DARK_AQUA + "   │  " + ChatColor.AQUA + "" + "BattleBox" + ChatColor.WHITE + " " + "Admin Arena Commands\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin arenas add <name> <x1 y1 z1> <x2 y2 z2> " +"\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "<x3 y3 z3> \n")
                .append(ChatColor.DARK_AQUA + "   │      " + ChatColor.GRAY + "<x1 y1 z1> ‣ " + ChatColor.WHITE + "Blue Team Spawn\n")
                .append(ChatColor.DARK_AQUA + "   │      " + ChatColor.GRAY + "<x2 y2 z2> ‣ " + ChatColor.WHITE + "Red Team Spawn\n")
                .append(ChatColor.DARK_AQUA + "   │      " + ChatColor.GRAY + "<x3 y3 z3> ‣ " + ChatColor.WHITE + "Mid-point of Capture Area\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin arenas remove <name>" + "\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin arenas specHeight <height>" + "\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Set the height of spectate area to TP.\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin arenas slimeJump <value>" + "\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Set Slime Jump Value (or disable)\n")
                .append(" ");

        sender.sendMessage(message.toString());
    }
    private void handleMatchesHelpCommand(CommandSender sender)
    {
    	StringBuilder message = new StringBuilder();
        message.append(ChatColor.DARK_AQUA + "   │  " + ChatColor.AQUA + "" + "BattleBox" + ChatColor.WHITE + " " + "Admin Match Commands\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin matches start <team1> <team2> <arena>\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Start a match\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + ChatColor.ITALIC + "Note ‣ One Match on an arena at a time.\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + ChatColor.ITALIC + "Note ‣ One Match of a team at a time.\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin matches endgame <team>  \n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Ends the game with <team> as winner.\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin matches setwinner <team>  \n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Ends the game with <team> as winner.\n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin matches clearhistory  \n")
                .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Clears all data of past matches.\n")
                .append(" ");

        sender.sendMessage(message.toString());
    	
    }
    private void handleAdminTeamsHelpCommand(CommandSender sender)
    {
    	 StringBuilder message = new StringBuilder();
    	    message.append(ChatColor.DARK_AQUA + "   │  " + ChatColor.AQUA + "" + "BattleBox" + ChatColor.WHITE + " " + "Admin Team Commands\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin teams create <Player> <team name>  " + ChatColor.WHITE + "\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Create team of an online player.\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin teams add <team> <player>  " + ChatColor.WHITE + "\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Add a member to an existing team.\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin teams kick <player>  " + ChatColor.WHITE + "\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Remove a member from their team.\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin teams promote <player>  " + ChatColor.WHITE + "\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Promote a member to leader of their team.\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin teams disband <team>  " + ChatColor.WHITE + "\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Disband an existing team.\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin teams setname <team> <name>  " + ChatColor.WHITE + "\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Sets the name of a team.\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin teams setname <teamLeader> <name>  " + ChatColor.WHITE + "\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Sets the name of a team.\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin teams setscore <team> <score>  " + ChatColor.WHITE + "\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Sets the score of a team.\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin teams maxsize <maxSize>  " + ChatColor.WHITE + "\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Set the Max Size of all teams.\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin teams populate  " + ChatColor.WHITE + "\n")
    	            .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Fill all teams with random players.\n")
    	            .append(" ");

    	    sender.sendMessage(message.toString());
    }
    private void handleTournamentHelpCommand(CommandSender sender)
    {
    	StringBuilder message = new StringBuilder();
    	message.append(ChatColor.DARK_AQUA + "   │  " + ChatColor.AQUA + "" + "BattleBox" + ChatColor.WHITE + " " + "Admin Team Commands\n")
        .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin tournament create" + ChatColor.WHITE + "\n")
        .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Create tournament of all teams and arenas..\n")
        .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin tournament start" + ChatColor.WHITE + "\n")
        .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Start a created tourmament.\n")
        .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "/bb admin tournament cancel" + ChatColor.WHITE + "\n")
        .append(ChatColor.DARK_AQUA + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Cancel an on-going tourmament.\n")
        .append(" ");

sender.sendMessage(message.toString());
    }
    
    private void handleAdminTeamsCommand(CommandSender sender, String[] args) 
    {
    	if ( args.length == 5 && args[2].equalsIgnoreCase("create"))
    	{
    		if (teamManager.getTeamByName(args[4]) != null)
            {
            	 sender.sendMessage(prefix + ChatColor.GRAY + "That name has already been taken!");
            	 return;
            }
	    	Player leader = Bukkit.getPlayer(args[3]);
	        if (leader != null)
	        {
	        	if (teamManager.getTeamByLeader(leader) != null)
	        	{
	        		sender.sendMessage(prefix + "The player is already in a team, do /team info <player>");
	        		return;
	        	}
	        	teamManager.createTeam(leader);
	        	Team team = teamManager.getTeamByLeader(leader);
	        	team.setName(args[4]);
	        	sender.sendMessage(prefix + "Team created: " + ChatColor.YELLOW + team.getName());
	        }
    	}
    	else if ( args.length == 5 && args[2].equalsIgnoreCase("add"))
    	{
	        if (teamManager.getTeamByName(args[3]) == null)
	        {
	        	sender.sendMessage(prefix + args[3]+ " team not found.");
	        	return;
	        }
	        Team team = teamManager.getTeamByName(args[3]);
	        Player player = Bukkit.getPlayer(args[4]);
	        if (player == null)
	        {
	        	sender.sendMessage(prefix + args[4]+ " player not found.");
	        	return;
	        }
	        if (teamManager.getTeamByPlayer(player) != null)
	        {
	        	sender.sendMessage(prefix + ChatColor.YELLOW + args[4] + ChatColor.GRAY + " is already in a team.");
	        	return;
	        }
	        try
	        {
	        	teamManager.addToTeam(player, team);
	        	sender.sendMessage(prefix + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has been added to " + team.getName());
	        	sendMessageToTeam(team,ChatColor.GOLD + "TEAM 〉 " + ChatColor.GREEN + "✚ " + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has been added to your team by admin.");
	        } catch (IllegalArgumentException e)
	        {
	        	sender.sendMessage(prefix + ChatColor.YELLOW + args[3] + ChatColor.GRAY + " is a full team.");	
	        }
	       
    	}
    	else if ( args.length == 4 && args[2].equalsIgnoreCase("kick"))
    	{
    		Player player = Bukkit.getPlayer(args[3]);
    		if (player == null)
	        {
	        	sender.sendMessage(prefix + args[3]+ "Player not found.");
	        	return;
	        }
    		Team team = teamManager.getTeamByPlayer(player);
    		if (team == null)
	        {
	        	sender.sendMessage(prefix + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " is not in any team.");
	        	return;
	        }
    		if (player == team.getLeader())
    		{
    			sender.sendMessage(prefix + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " is a leader of the team, promote someone first.");
	        	return;
    		}
    		teamManager.removeFromTeam(player, team);
    		sender.sendMessage(prefix + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has been removed from " + team.getName());
    		player.sendMessage(prefix + ChatColor.GOLD + "TEAM 〉 " + ChatColor.RED + "─ " +  ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has been kicked from your team by admin.");
        	sendMessageToTeam(team,ChatColor.GOLD + "TEAM 〉 " + ChatColor.RED + "─ " +  ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has been kicked from your team by admin.");
    		
    	}
    	else if ( args.length == 4 && args[2].equalsIgnoreCase("promote"))
    	{
    		Player player = Bukkit.getPlayer(args[3]);
    		
    		if (player == null)
	        {
	        	sender.sendMessage(prefix + args[3]+ "Player not found.");
	        	return;
	        }
    		Team team = teamManager.getTeamByPlayer(player);
    		if (team == null)
	        {
	        	sender.sendMessage(prefix + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " is not in any team.");
	        	return;
	        }
    		if (player == team.getLeader())
    		{
    			sender.sendMessage(prefix + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " is already the leader of the team.");
	        	return;
    		}
    		teamManager.promoteToLeader(team.getLeader(), player, team);
    		sender.sendMessage(prefix + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has been promoted to leader of " + team.getName());
        	sendMessageToTeam(team,ChatColor.GOLD + "TEAM 〉 " + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has been promoted to leader by admin.");
    		
    	}
    	else if ( args.length == 4 && args[2].equalsIgnoreCase("disband"))
    	{
    		Team team = teamManager.getTeamByName(args[3]);
    		if (team == null)
	        {
	        	sender.sendMessage(prefix + args[3] + " team not found.");
	        	return;
	        }
    		teamManager.removeTeam(team.getLeader());
    		sender.sendMessage(prefix + ChatColor.YELLOW + team.getName() + ChatColor.GRAY + " has been disbanded.");
        	sendMessageToTeam(team,ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + " Your team has been disbanded by admin.");
    		
    	}
    	else if ( args.length == 5 && args[2].equalsIgnoreCase("setname"))
    	{
    		if (teamManager.getTeamByName(args[4]) != null)
            {
            	 sender.sendMessage(prefix + ChatColor.GRAY + "That name has already been taken!");
            	 return;
            }
    		Team team = teamManager.getTeamByName(args[3]);
    		if (team != null)
	        {
    			team.setName(args[4]);
    			sender.sendMessage(prefix + " Team's name has been changed to " + team.getName());
            	sendMessageToTeam(team,ChatColor.GOLD + "TEAM 〉" + ChatColor.GRAY + " Your team's name been changed to " + ChatColor.YELLOW + team.getName() + ChatColor.GRAY + " by admin.");
    			return;
	        }
    		Player player = Bukkit.getPlayer(args[3]);
    		team = teamManager.getTeamByLeader(player);
    		if (team != null)
	        {
    			team.setName(args[4]);
    			sender.sendMessage(prefix + " Team's name has been changed to " + team.getName());
            	sendMessageToTeam(team,ChatColor.GOLD + "TEAM 〉" + ChatColor.GRAY + " Your team's name been changed to " + ChatColor.YELLOW + team.getName() + ChatColor.GRAY + " by admin.");
    			return;
	        }
    		else
    		{
    			sender.sendMessage(prefix + "No team or leader was found with name: " + args[4]);
    		}
    		
    	}
    	else if ( args.length == 3 && args[2].equalsIgnoreCase("populate"))
    	{
    		int maxSize = teamManager.getMaxSize();

            // Get a list of players without a team
            for (Player player : Bukkit.getOnlinePlayers()) {
                Team playerTeam = teamManager.getTeamByPlayer(player);

                if (playerTeam == null) {
                    // Find a team that is not full
                    for (Team team : teamManager.getAllTeams()) {
                        if (team.getSize() < maxSize) {
                            // Add the player to the team
                            teamManager.addToTeam(player, team);
            	        	sendMessageToTeam(team,ChatColor.GOLD + "TEAM 〉 " + ChatColor.GREEN + "✚ " + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has been added to your team randomly as your team was not full.");
                            break;
                        }
                    }
                }
            }
    		sender.sendMessage(prefix + "populated.");
    	}
    	else if ( args.length == 5 && args[2].equalsIgnoreCase("setscore"))
    	{
    		Team team = teamManager.getTeamByName(args[3]);
    		if (team == null)
	        {
    			sender.sendMessage(prefix + args[3] + " team not found.");
    			return;
	        }
    		try
    		{
    			int newScore = Integer.parseInt(args[4]);
    			team.setScore(newScore);
    			sender.sendMessage(prefix + ChatColor.YELLOW + team.getName() + ChatColor.GRAY + " score has been set to " + ChatColor.YELLOW + newScore);
            	sendMessageToTeam(team,ChatColor.GOLD + "TEAM 〉" + ChatColor.GRAY + " Your team's score been set to " + ChatColor.YELLOW + team.getScore() + ChatColor.GRAY + " by admin.");
    			
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(prefix + "Score must be a number.");
    		}
    	}
    	else if (args.length == 4 && args[2].equalsIgnoreCase("maxSize"))
    	{
    		try
    		{
    			int maxSize = Integer.parseInt(args[3]);
    			sender.sendMessage(prefix + "Max size for all teams has been changed from " + teamManager.getMaxSize() + " to " + args[3]);
    			teamManager.setMaxSize(maxSize);
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(prefix + "Size must be a number.");
    		}
    	}
    	else
    	{
    		handleAdminTeamsHelpCommand(sender);
    	}
    }
    private void handleAdminArenasCommand(CommandSender sender, String[] args) {
 
    	if (args.length == 13 && args[2].equalsIgnoreCase("add")) {
    		if (sender instanceof Player)
    		{
    			Player player = (Player) sender;
    			addNewArena(player, args);
    		}
    		else
    		{
    			sender.sendMessage(prefix + ChatColor.GRAY + "You must be a player to add arenas");
    		}
            
        } 
    	else if (args.length == 4 && args[2].equalsIgnoreCase("remove"))
        {
        	try
        	{
        		arenaManager.removeArena(args[3].toLowerCase());
        		sender.sendMessage(prefix + ChatColor.LIGHT_PURPLE + args[3] + ChatColor.GRAY + " arena removed");
        	}
        	catch (IllegalArgumentException e)
        	{
        		sender.sendMessage(prefix + ChatColor.GRAY + args[3] + " arena not found.");
        	}
        }
    	else if (args.length == 4 && args[2].equalsIgnoreCase("specHeight"))
        {
    		try
    		{
    			int height = Integer.parseInt(args[3]);	
    			setSpecHeight(height);
    			sender.sendMessage(prefix + "Spectate height set to: " + ChatColor.YELLOW + height);
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(prefix + "Invalid height value specified: " + ChatColor.YELLOW + args[3]);
    		}
        }
    	else if (args.length == 4 && args[2].equalsIgnoreCase("slimeJump"))
        {
    		try
    		{
    			double value = Double.parseDouble(args[3]);
    			setSlimeJumpValue(value);
    			sender.sendMessage(prefix + "Slime Jump set to: " + ChatColor.YELLOW + value);
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(prefix + "Invalid slime jump value specified: " + ChatColor.YELLOW + args[3]);
    		}
        }
    	else if (args.length == 3 && args[2].equalsIgnoreCase("slimeJump"))
        {
    		if (slimeJump)
    		{
    			setSlimeJump(false);
    			sender.sendMessage(prefix + "Slime Jump: " + ChatColor.YELLOW + "Disabled");
    		}
    		else
    		{
    			setSlimeJump(true);
    			sender.sendMessage(prefix + "Slime Jump: " + ChatColor.YELLOW + "Enabled");
    		}
        }
    	else
    	{
    		handleArenasHelpCommand(sender);
    	}
    }
    private void handleAdminMatchesCommand(CommandSender sender, String[] args) {
        if (args.length == 6 && args[2].equalsIgnoreCase("start")) {
            try {
                Team team1 = teamManager.getTeamByName(args[3]);
                Team team2 = teamManager.getTeamByName(args[4]);
                Arena arena = arenaManager.getArenaByName(args[5].toLowerCase());
                matchManager.startMatch(arena, team1, team2);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(prefix + "Invalid or Unavailable teams or arena names mentioned.");
            }
            
        } 
        else if (args.length == 4 && (args[2].equalsIgnoreCase("setwinner") || args[2].equalsIgnoreCase("endgame"))) {
        	Team team = teamManager.getTeamByName(args[3]);
        	if (team == null)
        	{
        		sender.sendMessage(prefix + args[3] + " team does not exist.");
        		return;
        	}
        	Match match = matchManager.getOngoingMatchByPlayer(team.getLeader());
        	if (match == null)
        	{
        		sender.sendMessage(prefix + args[3] + " team is not in any match.");
        		return;
        	}
           
            matchManager.endMatch(match.getArena(),team);
            sender.sendMessage(prefix + "Match has been ended.");
              
        }else if (args.length == 3 && args[2].equalsIgnoreCase("clearhistory")) 
        {
        	matchManager.clearHistory();
        	sender.sendMessage(prefix + "Matches history has been cleared.");
        }else {
            handleMatchesHelpCommand(sender);
        }
    }
    private void handleAdminTournamentCommand(CommandSender sender, String[] args) {
        if (args.length == 3 && args[2].equalsIgnoreCase("create")) {
        	if (Tournament.getCurrentTournament() != null && (Tournament.getCurrentTournament().getRoundIndex() >= Tournament.getCurrentTournament().getRounds().size())) {
                Tournament.getCurrentTournament().cancelTournament();
                sender.sendMessage(prefix + "The previous tournament has been cleared.");

            }
            if (Tournament.getCurrentTournament() != null) {
                sender.sendMessage(prefix + "A tournament has already been created.");
                return;
            }
            if (arenaManager.getArenas().size() < teamManager.getAllTeams().size() / 2) {
                sender.sendMessage(prefix + "Number of Arenas is less than the number of matches per round.");
                return;
            }

            try {
                Collection<Team> teamsCollection = teamManager.getAllTeams();
                ArrayList<Team> teamList = new ArrayList<>(teamsCollection);
                Tournament.createTournament(teamList, arenaManager, matchManager);
                Tournament.getCurrentTournament().generatePairings();
                sender.sendMessage(prefix + "A new tournament has been created.");
            } catch (IllegalArgumentException e) {
                sender.sendMessage(prefix + e.getMessage());
            }
        } else if (args.length == 3 && args[2].equalsIgnoreCase("start")) {
            if (Tournament.getCurrentTournament() == null) {
                sender.sendMessage(prefix + "First, do /bb tournament create");
                return;
            }
            if (!matchManager.getActiveMatches().isEmpty()) {
                sender.sendMessage(prefix + "Please ensure there are no ongoing matches.");
                return;
            }

            if (Tournament.getCurrentTournament().getRoundIndex() >= Tournament.getCurrentTournament().getRounds().size()) {
                // All matches of the current tournament have been conducted, so start a new tournament
                Tournament.getCurrentTournament().cancelTournament();
                sender.sendMessage(prefix + "The previous tournament has been cleared.");
                sender.sendMessage(prefix + "Please create a new tournament.");
                return;

            }
            Tournament.getCurrentTournament().startTournament();

        }
        else if (args.length == 3 && args[2].equalsIgnoreCase("cancel")) 
        {
        	if (Tournament.getCurrentTournament() == null)
        	{
        		sender.sendMessage(prefix + "No tournament is currently on-going");
        		return;
        	}
        	Tournament.getCurrentTournament().cancelTournament();
            sender.sendMessage(prefix + "The previous tournament has been cancelled.");
        }
        else {
            handleTournamentHelpCommand(sender);
        }
    }

    private void handleBroadcastCommand(String[] args)
    {
    	StringBuilder combinedArgs = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            combinedArgs.append(args[i]).append(" ");
        }
        String result = combinedArgs.toString().trim();
        if (result.contains("&")) {
            result = result.replace("&", "§"); // Replace '&' with '§' for color coding in Minecraft
        }
        Bukkit.getServer().broadcastMessage(prefix + ChatColor.RESET + result);
    }

    private void addNewArena(Player player, String[] args) {
        try {
            int x1 = Integer.parseInt(args[4]);
            int y1 = Integer.parseInt(args[5]);
            int z1 = Integer.parseInt(args[6]);
            int x2 = Integer.parseInt(args[7]);
            int y2 = Integer.parseInt(args[8]);
            int z2 = Integer.parseInt(args[9]);
            int x3 = Integer.parseInt(args[10]);
            int y3 = Integer.parseInt(args[11]);
            int z3 = Integer.parseInt(args[12]);
            Location loc1 = new Location(player.getWorld(), x1, y1, z1);
            Location loc2 = new Location(player.getWorld(), x2, y2, z2);
            Location loc3 = new Location(player.getWorld(), x3, y3, z3);

            StringBuilder message = new StringBuilder();
        	message.append(prefix + "" + "Arena Added " + ChatColor.LIGHT_PURPLE + args[3].toLowerCase() + "\n")
            	    .append(ChatColor.GRAY + "      Spawn-Point ‣ " + ChatColor.BLUE + x1 + " " + y1 + " " + z1 + "\n")
            	    .append(ChatColor.GRAY + "      Spawn-Point ‣ " + ChatColor.RED + x2 + " " + y2 + " " + z2 + "\n")
            	    .append(ChatColor.GRAY + "      Capture Area ‣ " + ChatColor.WHITE + x3 + " " + y3 + " " + z3 + "\n");
            	
            	player.sendMessage(message.toString());
            	
            Arena arena = new Arena(args[3].toLowerCase(),loc1, loc2, loc3);
            arenaManager.addArena(arena.getName(), arena);
        } catch (NumberFormatException e) {
            player.sendMessage(prefix + "Coordinates must be numbers.");
        }
    }
    private void openArenasGUI(Player player) {
    	int numRows = (int) Math.ceil(arenaManager.getArenas().size() / 9.0);
        Inventory gui = Bukkit.createInventory(null, numRows * 9, "Arenas");

        for (Arena arena : arenaManager.getArenas()) {
            ItemStack arenaItem = new ItemStack(Material.STEP); // You can change the material as desired
            ItemMeta meta = arenaItem.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + arenaManager.getNameOfArena(arena));
            List<String> lore = new ArrayList<>();
            
            if (matchManager.isArenaAvailable(arena)) {
                lore.add(ChatColor.GREEN + "Available: Yes");
                
            } else {
                lore.add(ChatColor.RED + "Available: No");
                lore.add(ChatColor.YELLOW + "On-going Match ID: " + ChatColor.WHITE + matchManager.getMatchAtArena(arena).getID());
            }
            
            meta.setLore(lore);
            arenaItem.setItemMeta(meta);
            gui.addItem(arenaItem);
        }

        player.openInventory(gui);
    }

    
    private void openMatchesGUI(Player player, int page) {
        int matchesPerPage = 45; // Maximum number of matches to display per page
        Map<Integer, Match> matches = matchManager.getMatches();
        int numMatches = matches.size();
        int totalPages = (int) Math.ceil(numMatches / (double) matchesPerPage);

        // Validate the page number
        if (page < 1 || page > totalPages) {
            player.sendMessage(prefix + ChatColor.RED + "Invalid page number.");
            return;
        }

        // Calculate the range of matches to display on the current page
        int startIndex = (page - 1) * matchesPerPage;
        int endIndex = Math.min(startIndex + matchesPerPage, numMatches);

        // Create the inventory for the current page
        int numRows = (int) Math.ceil(matchesPerPage / 9.0);
        Inventory gui = Bukkit.createInventory(null, numRows * 9, "Matches (Page " + page + "/" + totalPages + ")");

        List<Match> matchList = new ArrayList<>(matches.values());
        for (int i = startIndex; i < endIndex; i++) {
        	Match match = matchList.get(i);
        	ItemStack matchItem = new ItemStack(Material.BOOK); // You can change the material as desired
            	ItemMeta meta = matchItem.getItemMeta();
                meta.setDisplayName(ChatColor.DARK_GREEN + "Match ID: " + ChatColor.WHITE + match.getID());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.BLUE + match.getTeam1().getName() + ChatColor.YELLOW + " vs " + ChatColor.RED + match.getTeam2().getName());
                try {
                    lore.add(ChatColor.DARK_PURPLE + "Arena: " + ChatColor.LIGHT_PURPLE + arenaManager.getNameOfArena(match.getArena()));
                }
                catch (IllegalArgumentException e)
                {
                	lore.add(ChatColor.DARK_PURPLE + "Arena: " + ChatColor.DARK_GRAY + "[Deleted]");
                }
                if (match.isInProgress()) {
                    lore.add(ChatColor.GREEN + "On-going: Yes");
                    lore.add(ChatColor.DARK_GREEN + "Match Status:");

                    ChatColor[] colors = new ChatColor[9];
                    for (int j = 0; j < 9; j++) {
                        Location cp = match.getArena().getCapturePoints().get(j);
                        if (match.isPointCaptured(cp) && match.getCapturingTeam(cp).equals(match.getTeam1())) {
                            colors[j] = ChatColor.BLUE;
                        } else if (match.isPointCaptured(cp) && match.getCapturingTeam(cp).equals(match.getTeam2())) {
                            colors[j] = ChatColor.RED;
                        } else {
                            colors[j] = ChatColor.WHITE;
                        }
                    }

                    lore.add("      " + colors[2] + "▇" + colors[1] + "▇" + colors[0] + "▇");
                    lore.add("      " + colors[3] + "▇" + colors[4] + "▇" + colors[5] + "▇");
                    lore.add("      " + colors[6] + "▇" + colors[7] + "▇" + colors[8] + "▇");
                } else {
                    lore.add(ChatColor.YELLOW + "On-going: No");
                    lore.add(ChatColor.DARK_GREEN + "Won by: " + ChatColor.YELLOW + match.getWinner().getName());
                }
                meta.setLore(lore);
                matchItem.setItemMeta(meta);

            gui.addItem(matchItem);
        }

        player.openInventory(gui);
    }
    private void openTeamTournamentGUI(Player player, Team team) {
        if (Tournament.getCurrentTournament() == null) {
            player.sendMessage(prefix + "No tournament ongoing currently.");
            return;
        }
        
        List<List<Match>> rounds = Tournament.getCurrentTournament().getRounds();
        int numRows = (int) Math.ceil(rounds.size() / 9.0);
        Inventory gui = Bukkit.createInventory(null, numRows * 9, "Tournament Matches of " + team.getName());

        int roundNum = 0;
        for (List<Match> round : rounds) {
            for (Match match : round) {
                Team team1 = match.getTeam1();
                Team team2 = match.getTeam2();

                if ((team1 != null && team1.equals(team)) || (team2 != null && team2.equals(team))) {
                    ItemStack matchItem = new ItemStack(Material.DIAMOND_SWORD, roundNum); // You can change the material as desired
                    ItemMeta meta = matchItem.getItemMeta();
                    meta.setDisplayName(ChatColor.GOLD + "Match ID: " + match.getID());
                    List<String> lore = new ArrayList<>();

                    lore.add(ChatColor.BLUE + team1.getName() + ChatColor.YELLOW + " vs " + ChatColor.RED + team2.getName());
                    lore.add(ChatColor.DARK_PURPLE + "Arena: " + ChatColor.LIGHT_PURPLE + arenaManager.getNameOfArena(match.getArena()));
                    lore.add(" ");
                    lore.add(ChatColor.DARK_AQUA + "Round: " + ChatColor.AQUA + roundNum);
                    lore.add(" ");
                    
                    meta.setLore(lore);
                    matchItem.setItemMeta(meta);
                    gui.addItem(matchItem);
                }
            }
            roundNum++;
        }

        player.openInventory(gui);
    }

    private void openTournamentGUIPage(Player player, int page) {
        if (Tournament.getCurrentTournament() == null) {
            player.sendMessage(prefix + "No tournament is currently ongoing.");
            return;
        }

        List<List<Match>> rounds = Tournament.getCurrentTournament().getRounds();
        int totalTeams = teamManager.getAllTeams().size();
        int totalMatches = (totalTeams - 1) * (totalTeams / 2);
        int matchesPerPage = 45;
        int totalPages = (int) Math.ceil((double) totalMatches / matchesPerPage);

        if (page < 1 || page > totalPages) {
            player.sendMessage(prefix + "Invalid page number.");
            return;
        }

        int startIndex = (page - 1) * matchesPerPage;
        int endIndex = Math.min(startIndex + matchesPerPage, totalMatches);
        int numRows = (int) Math.ceil((endIndex - startIndex) / 9.0);
        Inventory gui = Bukkit.createInventory(null, numRows * 9, "Tournament Matches - Page " + page);

        int currentMatchIndex = 0;
        int roundNum = 0;
        for (List<Match> round : rounds) {
            for (Match match : round) {
                if (currentMatchIndex >= startIndex && currentMatchIndex < endIndex) {
                    ItemStack matchItem = new ItemStack(Material.DIAMOND_SWORD, roundNum);
                    ItemMeta meta = matchItem.getItemMeta();
                    meta.setDisplayName(ChatColor.GOLD + "Match ID: " + match.getID());
                    List<String> lore = new ArrayList<>();

                    Team team1 = match.getTeam1();
                    Team team2 = match.getTeam2();

                    if (team1 != null && team2 != null) {
                        lore.add(ChatColor.BLUE + team1.getName() + ChatColor.YELLOW + " vs " + ChatColor.RED + team2.getName());
                        lore.add(ChatColor.DARK_PURPLE + "Arena: " + ChatColor.LIGHT_PURPLE + arenaManager.getNameOfArena(match.getArena()));
                        lore.add(" ");
                        lore.add(ChatColor.DARK_AQUA + "Round: "  + ChatColor.AQUA + roundNum);
                    } else {
                        lore.add(ChatColor.WHITE + "Incomplete Match");
                    }

                    meta.setLore(lore);
                    matchItem.setItemMeta(meta);
                    gui.addItem(matchItem);
                }
                currentMatchIndex++;
            }
            roundNum++;
        }

        player.openInventory(gui);
    }
    private void sendMessageToTeam(Team team, String message)
    {
    	for (Player player : team.getAllPlayers())
    	{
    		player.sendMessage(message);
    	}
    }

}
