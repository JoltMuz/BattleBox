package io.github.JoltMuz.BattleBox;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class Listeners implements Listener {

    private final TeamManager teamManager;
    private final MatchManager matchManager;
    private final ArenaManager	arenaManager;

    public Listeners(TeamManager teamManager, MatchManager matchManager, ArenaManager arenaManager) {
        this.teamManager = teamManager;
        this.matchManager = matchManager;
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Team team = teamManager.getTeamByPlayer(event.getPlayer());
        if (team != null && TeamCommands.teamChatToggled.containsKey(event.getPlayer()) && TeamCommands.teamChatToggled.get(event.getPlayer())) {
            event.setCancelled(true);
           
            String playerName = event.getPlayer().getName();
            String message = event.getMessage();
            TeamCommands.sendMessageToTeam(team, ChatColor.GOLD + "TEAM 〉 " + ChatColor.YELLOW + playerName + ChatColor.DARK_GRAY + " 〉 " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player target = (Player) event.getEntity();

            if (arePlayersOnSameTeam(attacker, target)) {
                event.setCancelled(true);
            }
        }
        if (event.getDamager() instanceof Arrow && event.getEntity() instanceof Player) {
            Arrow arrow = (Arrow) event.getDamager();
            Player shooter = (Player) arrow.getShooter();
            Player target = (Player) event.getEntity();
            
            if (arePlayersOnSameTeam(shooter, target)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location placedLocation = event.getBlock().getLocation();
        Match match = matchManager.getOngoingMatchByPlayer(player);

        if (match != null) {
            Arena arena = match.getArena();
            Iterable<Location> capturePoints = arena.getCapturePoints();
            Location roundedPlacedLoc = new Location(placedLocation.getWorld(), Math.floor(placedLocation.getX()), Math.floor(placedLocation.getY()), Math.floor(placedLocation.getZ()));

            for (Location capturePoint : capturePoints) {
                if (roundedPlacedLoc.equals(capturePoint)) {
                    Team capturingTeam = match.getTeam1().contains(player) ? match.getTeam1() : match.getTeam2();
                    match.capturePoint(roundedPlacedLoc, capturingTeam);
                    int capturedPointCount = match.getCapturedPointCount(capturingTeam);
                    for (Player p : match.getAllPlayers()) {
                        p.getWorld().playSound(placedLocation, Sound.NOTE_PLING, (float) 2.0, (float) capturedPointCount * 2 / 10);
                    }
                    // Check if the capturing team has won
                    if (hasTeamWon(match, capturingTeam)) {
                        matchManager.endMatch(arena, capturingTeam);
                    }
                    break; // No need to continue checking other capture points
                }
                // Preventing them to place above capture area
                if (roundedPlacedLoc.equals(capturePoint.clone().add(0,1,0))) 
                {
                	event.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location brokenLocation = event.getBlock().getLocation();
        Match match = matchManager.getOngoingMatchByPlayer(player);

        if (match != null) {
            Arena arena = match.getArena();
            Iterable<Location> capturePoints = arena.getCapturePoints();

            for (Location capturePoint : capturePoints) {
                Location roundedBrokenLoc = new Location(brokenLocation.getWorld(), Math.floor(brokenLocation.getX()), Math.floor(brokenLocation.getY()), Math.floor(brokenLocation.getZ()));
                if (roundedBrokenLoc.equals(capturePoint)) {
                	event.getBlock().setType(Material.AIR);
                    match.releasePoint(roundedBrokenLoc);
                    event.setCancelled(true);
                    break; // No need to continue checking other capture points
                }
            }
        }
    }
    
    private Map<UUID, Boolean> canJump = new HashMap<>();
    
    @EventHandler
    public void slimeJump(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation().clone().subtract(0, 1, 0);
        Block block = location.getBlock();
        if (block.getType() == Material.SLIME_BLOCK && Commands.isSlimeJump())
        {
            UUID playerUUID = player.getUniqueId();
            if (!canJump.containsKey(playerUUID) || canJump.get(playerUUID)) {
                player.setVelocity(new Vector(0, Commands.getSlimeJumpValue(), 0));
                canJump.put(playerUUID, false);
            }
        } else 
        {
            canJump.put(player.getUniqueId(), true);
        }
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
    	String name = event.getInventory().getName();
        if (name.contains("Arenas")
        		|| name.contains("Matches")
        		|| name.contains("Team Rankings"))
        {
        	event.setCancelled(true);
        }
        if (name.contains("On-going Matches"))
        {
        	HumanEntity human = event.getWhoClicked();
        	
        	if (human instanceof Player && teamManager.getTeamByPlayer((Player) human) != null && !matchManager.isTeamAvailable(teamManager.getTeamByPlayer((Player) human)))
        	{
        		human.sendMessage(Commands.getPrefix() + "Must not spectate during your match.");
        		return;
        	}
        	
        	ItemStack item = event.getCurrentItem();

            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();

                if (lore != null && !lore.isEmpty()) {
                    // Extract the arena information from the lore
                    String arenaInfoLine = lore.get(1); // Assuming it's the second line of lore

                    // Parse the arena name from the lore line
                    String arenaName = ChatColor.stripColor(arenaInfoLine).split(": ")[1].trim();

                    // Now, you have the arenaName associated with the clicked item
                    Arena arena = arenaManager.getArenaByName(arenaName);

                    if (arena == null) {
                        // You have the arena, you can do whatever you need with it here
                        human.sendMessage(Commands.getPrefix() + "Arena not found.");
                        return;
                    }
                    human.sendMessage(Commands.getPrefix() + "Teleporting to arena: " + ChatColor.LIGHT_PURPLE + arenaName);
                    human.teleport(arena.getCapturePointsMid().clone().add(new Vector(0,Commands.getSpecHeight(),0)));
                }
            }
        }
    }
    @EventHandler
    public void respawner(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final Match match = matchManager.getOngoingMatchByPlayer(player);

        if (match != null) {
            final Team team = teamManager.getTeamByPlayer(player);
            TitleSender.sendTitle(player, ChatColor.RED.toString() +  "You Died", ChatColor.YELLOW.toString() + "Respawning in 2 seconds...", 10, 20, 10);
            player.setHealth(20);
            playLineOfEffects(player);
            player.getWorld().playEffect(player.getLocation(), Effect.EXTINGUISH, 1,3);
            player.setGameMode(GameMode.SPECTATOR);

            Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                @Override
                public void run() {
                    player.setGameMode(GameMode.SURVIVAL);
                    if (match.isInProgress())
                    {
                    	player.teleport(match.getSpawnPoint(team));
                    	player.setHealth(20);
                    }
                }
            }, 40L);
        }
    }
    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Team team = teamManager.getTeamByPlayer(player);

        if (team != null) {
            if (team.getLeader().equals(player)) {
                if (!team.getMembers().isEmpty()) {
                    Player newLeader = team.getMembers().get(0);
                    teamManager.promoteToLeader(player, newLeader, team);
                    team.removeMember(player);
                    broadcastMessageToTeam(team,ChatColor.GOLD + "TEAM 〉 " + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has disconnected. " +
                            ChatColor.YELLOW + newLeader.getName() + ChatColor.GRAY + " is the new team leader.");
                } else {
                    teamManager.removeTeam(player);
                    broadcastMessageToTeam(team, ChatColor.GOLD + "TEAM 〉 " + ChatColor.YELLOW + player.getName() + ChatColor.GRAY +" has disconnected. The team has been disbanded.");
                }
            } else {
                team.removeMember(player);
                broadcastMessageToTeam(team, ChatColor.GOLD + "TEAM 〉 " + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has left the team by disconnecting.");
            }
        }

        Match match = matchManager.getOngoingMatchByPlayer(player);
        if (match != null) {
        	player.setWalkSpeed((float) 0.2);
        	player.getInventory().clear();
            player.setHealth(20);
            player.getInventory().setArmorContents(null);
            NameTagColorManager.removeColor(player);
            broadcastLeaveMessageToMatchPlayers(match,team,player);
        }
    }

    private void broadcastMessageToTeam(Team team, String message) {
        for (Player member : team.getAllPlayers()) {
            member.sendMessage(message);
        }
    }

    private void broadcastLeaveMessageToMatchPlayers(Match match, Team team, Player p) {
    	ChatColor color = ChatColor.WHITE;
    	if (match.getTeam1() == team)
    	{
    		color = ChatColor.BLUE;
    	}
    	else if (match.getTeam2() == team)
    	{
    		color = ChatColor.RED;
    	}
        for (Player player : match.getAllPlayers()) {
            player.sendMessage(Commands.getPrefix() + color + p.getName() + ChatColor.GRAY + " has disconnected." );
        }
    }
    private void playLineOfEffects(Player player) {
    	Effect effect = Effect.LAVA_POP;
        Location location = player.getLocation().clone();
        World world = location.getWorld();

        for (double i = 0; i < 2; i=i+0.2) {

            world.playEffect(location.add(0, i, 0), effect, 1,5);
        }
    }

    private boolean hasTeamWon(Match match, Team team) {
        for (Location capturePoint : match.getArena().getCapturePoints()) {
            if (!match.isPointCaptured(capturePoint) || match.getCapturingTeam(capturePoint) != team) {
                return false;
            }
        }
        return true;
    }
    private boolean arePlayersOnSameTeam(Player player1, Player player2) {
        Team team1 = teamManager.getTeamByPlayer(player1);
        Team team2 = teamManager.getTeamByPlayer(player2);

        return team1 != null && team2 != null && team1.equals(team2);
    }
    
}
