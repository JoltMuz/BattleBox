package io.github.JoltMuz.BattleBox;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class TeamCommands implements CommandExecutor {

    private TeamManager teamManager;
    private Map<Player, Player> pendingInvitations;
    static Map<Player, Boolean> teamChatToggled;

    public TeamCommands(TeamManager teamManager) {
    	this.teamManager = teamManager;
        pendingInvitations = new HashMap<>();
        teamChatToggled = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;
        Team playerTeam = teamManager.getTeamByPlayer(player);

        if (args.length == 0) {
        	handleHelpCommand(player, playerTeam, args, label);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
        	case "name":
        		handleNameCommand(player, playerTeam, args, label);
                break;
            case "invite":
                handleInviteCommand(player, playerTeam, args, label);
                break;
            case "kick":
                handleKickCommand(player, playerTeam, args, label);
                break;
            case "promote":
                handlePromoteCommand(player, playerTeam, args, label);
                break;
            case "disband":
                handleDisbandCommand(player, playerTeam);
                break;
            case "leave":
                handleLeaveCommand(player, playerTeam, label);
                break;
            case "info":
            case "list":
                handleListCommand(player, playerTeam, args);
                break;
            case "top":
            case "listall":
                handleTopCommand(player);
                break;
            case "chat":
                handleChatCommand(player, playerTeam, args);
                break;
            case "help":
                handleHelpCommand(player, playerTeam, args, label);
                break;
            case "join":
            case "accept":
                handleAcceptCommand(player, playerTeam, args, label);
                break;
            default:
                sender.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Unknown command. Use '/" + label + " help' for assistance.");
                break;
        }

        return true;
    }

    private void handleHelpCommand(Player player, Team playerTeam, String[] args, String label) 
    {
    	String[] message = {
    	    ChatColor.GOLD + "   │  " + ChatColor.BOLD + "" + ChatColor.GOLD + "" + ChatColor.BOLD + "" + "Team" + ChatColor.RESET + " " + "The Team Help Page\n" +
    	    ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/" + label + " invite [username] ‣ " + ChatColor.WHITE + "Invite a player to your team.\n" +
    	    ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/" + label + " name [name] ‣ " + ChatColor.WHITE + "Change your team's name.\n" +
    	    ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/" + label + " chat [message] ‣ " + ChatColor.WHITE + "Send a message in team chat.\n" +
    	    ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/" + label + " list <player>‣ " + ChatColor.WHITE + "Display team information.\n" +
    	    ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/" + label + " leave ‣ " + ChatColor.WHITE + "Leave your team.\n" +
    	    ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/" + label + " kick [username] ‣ " + ChatColor.WHITE + "Kick a player from your team.\n" +
    	    ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/" + label + " disband [username] ‣ " + ChatColor.WHITE + "Disband your team.\n" +
    	    ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/" + label + " promote [username] ‣ " + ChatColor.WHITE + "Promote a player to team leader.\n" +
    	    ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/" + label + " top ‣ " + ChatColor.WHITE + "Show teams leaderboard.\n" +
    	    ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/" + label + " help ‣ " + ChatColor.WHITE + "List all commands."
    	};
    	player.sendMessage(message);
    }
    
    private void handleInviteCommand(Player player, Team playerTeam, String[] args, String label) {
        if (playerTeam == null) {
            teamManager.createTeam(player);
            playerTeam = teamManager.getTeamByLeader(player);
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You have created a new team.");
        }

        if (!playerTeam.getLeader().equals(player)) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Only the team's leader can invite others.");
            return;
        }

        if (playerTeam.getSize() == teamManager.getMaxSize()){
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You cannot invite more, max team size is " + teamManager.getMaxSize());
            return;
        }
        if (Tournament.getCurrentTournament() != null)
        {
        	player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You cannot invite during an on-going tournament.");
        	return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Usage: /" + label + " invite [player]");
            return;
        }

        String playerToInviteName = args[1];
        Player invitedPlayer = Bukkit.getPlayer(playerToInviteName);

        if (invitedPlayer == null) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Player " + ChatColor.YELLOW + playerToInviteName + ChatColor.GRAY + " not found.");
            return;
        }

        if (invitedPlayer.equals(player)) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You cannot invite yourself to your team.");
            return;
        }

        if (playerTeam.getMembers().contains(invitedPlayer)) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.YELLOW + invitedPlayer.getName() + ChatColor.GRAY + " is already in your team.");
            return;
        }

        if (pendingInvitations.containsKey(invitedPlayer) && pendingInvitations.get(invitedPlayer).equals(player)) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You have already sent an invitation to " + ChatColor.YELLOW + invitedPlayer.getName() + ChatColor.GRAY + ".");
            return;
        }

        StringBuilder invitationMessage = new StringBuilder();
        invitationMessage.append(ChatColor.GOLD).append("TEAM 〉 ")
        				.append(ChatColor.GRAY).append("You have been invited to the team by ")
        				.append(ChatColor.YELLOW).append(player.getName())
                        .append(ChatColor.GRAY).append(". ")
                        .append(ChatColor.GREEN).append(ChatColor.BOLD).append("Click here ")
                        .append(ChatColor.GREEN).append("to join!");

        BaseComponent[] invitationComponent = new ComponentBuilder(invitationMessage.toString())
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team accept " + player.getName()))
                .create();

        invitedPlayer.spigot().sendMessage(invitationComponent);
        sendMessageToTeam(playerTeam, ChatColor.GOLD + "TEAM 〉 " + ChatColor.YELLOW + invitedPlayer.getName() + ChatColor.GRAY + " has been invited to the team by " + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + ".");
        pendingInvitations.put(invitedPlayer, player);
    }
    
    private void handleNameCommand(Player player, Team playerTeam, String[] args, String label) {
        if (playerTeam == null) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You are not in a team.");
            return;
        }

        if (!playerTeam.getLeader().equals(player)) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Only the team's leader can set the Team's Name.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Usage: /" + label + " name [name]");
            return;
        }
        String name = args[1];
        if (teamManager.getTeamByName(name) != null)
        {
        	 player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "That name has already been taken!");
        	 return;
        }
        playerTeam.setName(name);
        sendMessageToTeam(playerTeam, ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "The team's name has been set: " + ChatColor.YELLOW + name);
    }
    
    private void handleKickCommand(Player player, Team playerTeam, String[] args, String label) {
        if (playerTeam == null) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You are not in a team.");
            return;
        }

        if (!playerTeam.getLeader().equals(player)) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Only the team's leader can kick members.");
            return;
        }
        
        if (Tournament.getCurrentTournament() != null)
        {
        	player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You cannot kick during an on-going tournament.");
        	return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Usage: /" + label + " kick [player]");
            return;
        }

        String playerToKickName = args[1];
        Player playerToKick = Bukkit.getPlayer(playerToKickName);

        if (playerToKick == null) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.YELLOW + "Player " + playerToKickName + ChatColor.GRAY + " not found.");
            return;
        }

        if (playerToKick.equals(player)) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You cannot kick yourself from your team. Try disbanding instead.");
            return;
        }

        Team kickedPlayerTeam = teamManager.getTeamByPlayer(playerToKick);
        if (kickedPlayerTeam == null || !kickedPlayerTeam.equals(playerTeam)) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Player " + ChatColor.YELLOW + playerToKickName + ChatColor.GRAY + " is not in your team.");
            return;
        }

        sendMessageToTeam(playerTeam, ChatColor.GOLD + "TEAM 〉 " + ChatColor.RED + "─ " + ChatColor.YELLOW + playerToKickName + ChatColor.GRAY + " has been kicked out from the team by " + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + ".");
        teamManager.removeFromTeam(playerToKick, playerTeam);
    }
    
    private void handlePromoteCommand(Player player, Team playerTeam, String[] args, String label) {
        if (playerTeam == null) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You are not in a team.");
            return;
        }

        if (!playerTeam.getLeader().equals(player)) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Only the team's leader can promote others.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Usage: /" + label + " [player]");
            return;
        }

        String playerToPromoteName = args[1];
        Player playerToPromote = Bukkit.getPlayer(playerToPromoteName);

        if (playerToPromote == null) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Player " + ChatColor.GRAY + playerToPromoteName + ChatColor.GRAY + " not found.");
            return;
        }

        if (playerToPromote.equals(player)) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You cannot promote yourself, you are the leader.");
            return;
        }

        Team promotedPlayerTeam = teamManager.getTeamByPlayer(playerToPromote);
        if (promotedPlayerTeam == null || !promotedPlayerTeam.equals(playerTeam)) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Player " + ChatColor.YELLOW + playerToPromoteName + ChatColor.GRAY + " is not in your team.");
            return;
        }

        teamManager.promoteToLeader(player, playerToPromote, playerTeam);
        sendMessageToTeam(playerTeam, ChatColor.GOLD + "TEAM 〉 " + ChatColor.YELLOW + playerToPromoteName + ChatColor.GRAY + " has been promoted to leader by " + player.getName() + ChatColor.GRAY + ".");
    }

    private void handleDisbandCommand(Player player, Team playerTeam) {
        if (playerTeam == null) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You are not in a team.");
            return;
        }

        if (!playerTeam.getLeader().equals(player)) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Only the team's leader can disband the team.");
            return;
        }
        if (Tournament.getCurrentTournament() != null)
        {
        	player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You cannot disband during an on-going tournament.");
        	return;
        }

        teamManager.removeTeam(player);
        sendMessageToTeam(playerTeam, ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "The team has been disbanded by " + ChatColor.YELLOW + player.getName());
    }

    private void handleLeaveCommand(Player player, Team playerTeam, String label) {
        if (playerTeam == null) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You are not in a team.");
            return;
        }

        if (playerTeam.getLeader().equals(player)) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You cannot leave the team as the leader. Use /" + label + " disband instead.");
            return;
        }

        teamManager.removeFromTeam(player, playerTeam);
        player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You have left the team.");
        sendMessageToTeam(playerTeam, ChatColor.GOLD + "TEAM 〉 " + ChatColor.RED + "─ " + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has left the Team.");
    }

    private void handleListCommand(Player player, Team playerTeam, String[] args) 
    {
    	if (args.length == 1)
    	{
    		if (playerTeam == null) 
            {
                player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You are not in a team.");
                return;
            }
            
            player.sendMessage(ChatColor.GOLD + "TEAM │ " + ChatColor.GRAY + "Your Team: " + ChatColor.YELLOW + playerTeam.getName() + ChatColor.GRAY + " [" + ChatColor.YELLOW + playerTeam.getScore() + ChatColor.GRAY + "]");
            player.sendMessage(ChatColor.GOLD + "       │ " + ChatColor.GRAY + "Leader: " + ChatColor.YELLOW + playerTeam.getLeader().getName());
            player.sendMessage(ChatColor.GOLD + "       │ " + ChatColor.GRAY + "Members (" + playerTeam.getMembers().size() + ") : ");
            for (Player p: playerTeam.getMembers())
            {
            	player.sendMessage(ChatColor.GOLD + "         - " + ChatColor.YELLOW + p.getName());
            }
    	}
    	else if (args.length > 1)
    	{
    		Player listed = Bukkit.getPlayer(args[1]);
    		if (listed != null)
    		{
    			Team listedTeam = teamManager.getTeamByPlayer(listed);

        		if (listedTeam == null) 
                {
                    player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "That player is not in a team.");
                    return;
                }
                
                player.sendMessage(ChatColor.GOLD + "TEAM │ " + ChatColor.YELLOW + listedTeam.getName() + ChatColor.GRAY + " [" + ChatColor.YELLOW + listedTeam.getScore() + ChatColor.GRAY + "]");
                player.sendMessage(ChatColor.GOLD + "       │ " + ChatColor.GRAY + "Leader: " + ChatColor.YELLOW + listedTeam.getLeader().getName());
                player.sendMessage(ChatColor.GOLD + "       │ " + ChatColor.GRAY + "Members (" + listedTeam.getMembers().size() + ") : ");
                for (Player p: listedTeam.getMembers())
                {
                	player.sendMessage(ChatColor.GOLD + "         - " + ChatColor.YELLOW + p.getName());
                }
    		}
    		else
    		{
    			Team listedTeam = teamManager.getTeamByName(args[1]);

        		if (listedTeam == null) 
                {
                    player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "That team/player does not exist.");
                    return;
                }
                
                player.sendMessage(ChatColor.GOLD + "TEAM │ " + ChatColor.YELLOW + listedTeam.getName() + ChatColor.GRAY + " [" + ChatColor.YELLOW + listedTeam.getScore() + ChatColor.GRAY + "]");
                player.sendMessage(ChatColor.GOLD + "       │ " + ChatColor.GRAY + "Leader: " + ChatColor.YELLOW + listedTeam.getLeader().getName());
                player.sendMessage(ChatColor.GOLD + "       │ " + ChatColor.GRAY + "Members (" + listedTeam.getMembers().size() + ") : ");
                for (Player p: listedTeam.getMembers())
                {
                	player.sendMessage(ChatColor.GOLD + "         - " + ChatColor.YELLOW + p.getName());
                }
    		}
    		
    		
    	}
        
    }

    private void handleChatCommand(Player player, Team playerTeam, String[] args) {
        if (playerTeam == null) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You are not in a team.");
            return;
        }
        if (args.length == 1)
        {
        	toggleTeamChat(player);
        	if (teamChatToggled.get(player))
        	{
        		player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Team Chat Enabled.");
            	return;
        	}
        	else
        	{
        		player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Team Chat Disabled.");
        		return;
        	}
        	
        }
        
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                messageBuilder.append(" ");
            }
            messageBuilder.append(args[i]);
        }
        String message = messageBuilder.toString();
        
        sendMessageToTeam(playerTeam, ChatColor.GOLD + "TEAM 〉 " + ChatColor.YELLOW + player.getName() + ChatColor.DARK_GRAY + " 〉 " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', message));
    }

    private void handleAcceptCommand(Player player, Team playerTeam, String[] args, String label){
        if (playerTeam != null) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You are already in a team.");
            return;
        }
        if (Tournament.getCurrentTournament() != null)
        {
        	player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You cannot join during an on-going tournament.");
        	return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Usage: /" + label + " accept [player]");
            return;
        }

        Player inviter = Bukkit.getPlayer(args[1]);
        if (inviter != null) {
            if (!pendingInvitations.containsKey(player)) {
                player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "You do not have any pending invitations.");
                return;
            }
            
            if (!pendingInvitations.get(player).equals(inviter)) {
                player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "That player has not invited you to their team.");
                return;
            }
            
            Team inviterTeam = teamManager.getTeamByPlayer(inviter);
            if (inviterTeam != null) {
                if (inviterTeam.getSize() == teamManager.getMaxSize()) {
                    player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "That team is now full, you cannot join it.");
                    return;
                }
                
                teamManager.addToTeam(player, inviterTeam);
                sendMessageToTeam(teamManager.getTeamByPlayer(player), ChatColor.GOLD + "TEAM 〉 " + ChatColor.GREEN + "✚ " + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " joined the party.");
                pendingInvitations.remove(player);
            } else {
                player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "The inviting player is not in a team.");
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "TEAM 〉 " + ChatColor.GRAY + "Inviting player not found.");
        }
    }
    
    static void sendMessageToTeam(Team team, String message) {
	    team.getLeader().sendMessage(message);
	    for (Player p : team.getMembers()) {
	        p.sendMessage(message);
	    }
    }

    private void handleTopCommand(Player player) {
        Collection<Team> teamsCollection = teamManager.getAllTeams();
        List<Team> teams = new ArrayList<>(teamsCollection);

        Collections.sort(teams, new Comparator<Team>() {
            @Override
            public int compare(Team team1, Team team2) {
                return Integer.compare(team2.getScore(), team1.getScore());
            }
        });

        int numRows = (int) Math.ceil(teams.size() / 9.0);
        Inventory inventory = Bukkit.createInventory(null, numRows * 9, "Team Rankings");

        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            ItemStack head = createTeamHead(team);
            inventory.setItem(i, head);
        }

        player.openInventory(inventory);
    }
    
	private ItemStack createTeamHead(Team team) {
	    ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
	    SkullMeta meta = (SkullMeta) head.getItemMeta();
	
	    if (meta != null) {
	        meta.setOwner(team.getLeader().getName());
	        meta.setDisplayName(ChatColor.GOLD + team.getName() + ChatColor.GRAY + " (" + ChatColor.YELLOW + team.getScore() + ChatColor.GRAY + ")");
	        List<String> lore = new ArrayList<>();
	        lore.add(ChatColor.GRAY + "Leader: " + ChatColor.YELLOW + team.getLeader().getName());
	        lore.add(ChatColor.GRAY + "Members (" + ChatColor.YELLOW + team.getMembers().size() + ChatColor.GRAY + "):");
	        for (Player member : team.getMembers()) {
	            lore.add(ChatColor.YELLOW + "  " + member.getName());
	        }
	        lore.add(ChatColor.GRAY + "Score: " + ChatColor.YELLOW + team.getScore());
	        meta.setLore(lore);
	
	        head.setItemMeta(meta);
	    }
	
	    return head;
	}

	private void toggleTeamChat(Player player)
	{
		if (!teamChatToggled.containsKey(player))
		{
			teamChatToggled.put(player, true);
			return;
		}
		else if (teamChatToggled.get(player))
		{
			teamChatToggled.put(player, false);
			return;
		}
		else
		{
			teamChatToggled.put(player, true);
			return;
		}
	}
}
